package com.citywalls.app.data.repository

import com.citywalls.app.data.local.DownloadDao
import com.citywalls.app.data.local.FavoriteDao
import com.citywalls.app.data.local.toDownloadedWallpaper
import com.citywalls.app.data.local.toDownloadEntity
import com.citywalls.app.data.local.toFavoriteEntity
import com.citywalls.app.data.local.toWallpaper
import com.citywalls.app.data.prefs.SettingsRepository
import com.citywalls.app.data.remote.CommonsApi
import com.citywalls.app.data.remote.CommonsMapper
import com.citywalls.app.data.remote.CommonsUrls
import com.citywalls.app.domain.CatalogEntry
import com.citywalls.app.domain.DownloadedWallpaper
import com.citywalls.app.domain.SearchSort
import com.citywalls.app.domain.Wallpaper
import com.citywalls.app.domain.WallpaperPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Single source of truth for wallpapers. Everything displayed comes from the
 * live Wikimedia Commons API; Room only stores what the user chose to keep.
 */
class WallpaperRepository(
    private val api: CommonsApi,
    private val favoriteDao: FavoriteDao,
    private val downloadDao: DownloadDao,
    private val settingsRepository: SettingsRepository
) {

    private data class CachedPage(val page: WallpaperPage, val storedAt: Long)

    private val memoryCache = LinkedHashMap<String, Wallpaper>(128, 0.75f, true)
    private val pageCache = LinkedHashMap<String, CachedPage>(32, 0.75f, true)
    private val headerCache = LinkedHashMap<String, Pair<String?, Long>>(32, 0.75f, true)
    private val lock = Any()

    fun cached(id: String): Wallpaper? = synchronized(lock) { memoryCache[id] }

    private fun remember(items: List<Wallpaper>) = synchronized(lock) {
        items.forEach { memoryCache[it.id] = it }
        while (memoryCache.size > 240) {
            val eldest = memoryCache.keys.firstOrNull() ?: break
            memoryCache.remove(eldest)
        }
    }

    suspend fun search(
        query: String,
        offset: Int,
        sort: SearchSort,
        categoryKey: String = "",
        categoryLabel: String = ""
    ): Result<WallpaperPage> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.success(WallpaperPage(emptyList(), null, false))
        }

        val cacheKey = trimmed + "|" + offset + "|" + sort.apiValue + "|" + categoryKey
        synchronized(lock) {
            val cachedPage = pageCache[cacheKey]
            if (cachedPage != null && System.currentTimeMillis() - cachedPage.storedAt < PAGE_TTL_MS) {
                return@withContext Result.success(cachedPage.page)
            }
        }

        runCatching {
            val quality = settingsRepository.settings.first().imageQuality
            val response = api.searchImages(
                search = trimmed + " filetype:bitmap",
                namespace = 6,
                limit = PAGE_SIZE,
                offset = offset,
                sort = sort.apiValue,
                thumbWidth = quality.previewWidth
            )

            val pages = response.query?.pages.orEmpty()
            val seenUrls = HashSet<String>()
            val items = pages.mapNotNull { page ->
                val wallpaper = CommonsMapper.toWallpaper(
                    page = page,
                    categoryKey = categoryKey,
                    categoryLabel = categoryLabel,
                    thumbWidth = quality.previewWidth,
                    minSourceWidth = quality.minSourceWidth
                ) ?: return@mapNotNull null
                if (!seenUrls.add(wallpaper.fullUrl)) null else wallpaper
            }

            remember(items)

            // The API may paginate another prop first (categories), in which case the
            // continue block carries clcontinue and no gsroffset. Any continue block
            // means more pages exist, so advance by what was actually returned.
            val continueToken = response.continueToken
            val returned = pages.size
            val explicitOffset = continueToken?.gsrOffset
            val nextOffset = when {
                continueToken == null -> null
                explicitOffset != null && explicitOffset > offset -> explicitOffset
                returned > 0 -> offset + returned
                else -> null
            }
            val result = WallpaperPage(
                items = items,
                nextOffset = nextOffset,
                hasMore = nextOffset != null && items.isNotEmpty()
            )

            synchronized(lock) {
                pageCache[cacheKey] = CachedPage(result, System.currentTimeMillis())
                while (pageCache.size > 40) {
                    val eldest = pageCache.keys.firstOrNull() ?: break
                    pageCache.remove(eldest)
                }
            }

            result
        }
    }

    suspend fun headerFor(entry: CatalogEntry): String? = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val cachedHeader = headerCache[entry.key]
            if (cachedHeader != null && System.currentTimeMillis() - cachedHeader.second < PAGE_TTL_MS) {
                return@withContext cachedHeader.first
            }
        }

        val page = search(
            query = entry.headerQuery,
            offset = 0,
            sort = SearchSort.POPULAR,
            categoryKey = entry.key,
            categoryLabel = entry.name
        ).getOrNull()

        val url = page?.items?.firstOrNull()?.let { CommonsUrls.scaled(it.previewUrl, 1080) }
        synchronized(lock) { headerCache[entry.key] = url to System.currentTimeMillis() }
        url
    }

    suspend fun findWallpaper(id: String): Wallpaper? = withContext(Dispatchers.IO) {
        cached(id)
            ?: favoriteDao.find(id)?.toWallpaper()
            ?: downloadDao.find(id)?.toWallpaper()
    }

    // ---------------------------------------------------------------- favorites

    val favorites: Flow<List<Wallpaper>> = favoriteDao.observeAll().map { entities ->
        entities.map { it.toWallpaper() }
    }

    fun isFavorite(id: String): Flow<Boolean> = favoriteDao.observeIsFavorite(id).map { it > 0 }

    suspend fun toggleFavorite(wallpaper: Wallpaper): Boolean = withContext(Dispatchers.IO) {
        val alreadySaved = favoriteDao.isFavorite(wallpaper.id) > 0
        if (alreadySaved) {
            favoriteDao.delete(wallpaper.id)
            false
        } else {
            favoriteDao.insert(wallpaper.toFavoriteEntity(System.currentTimeMillis()))
            true
        }
    }

    suspend fun removeFavorite(id: String) = withContext(Dispatchers.IO) {
        favoriteDao.delete(id)
    }

    // ---------------------------------------------------------------- downloads

    val downloads: Flow<List<DownloadedWallpaper>> = downloadDao.observeAll().map { entities ->
        entities.map { it.toDownloadedWallpaper() }
    }

    suspend fun localPathFor(id: String): String? = withContext(Dispatchers.IO) {
        downloadDao.find(id)?.localPath
    }

    suspend fun saveDownload(wallpaper: Wallpaper, localPath: String, sizeBytes: Long) =
        withContext(Dispatchers.IO) {
            downloadDao.insert(
                wallpaper.toDownloadEntity(
                    localPath = localPath,
                    sizeBytes = sizeBytes,
                    downloadedAt = System.currentTimeMillis()
                )
            )
        }

    suspend fun removeDownload(id: String) = withContext(Dispatchers.IO) {
        downloadDao.delete(id)
    }

    companion object {
        const val PAGE_SIZE = 24
        private const val PAGE_TTL_MS = 15L * 60L * 1000L
    }
}
