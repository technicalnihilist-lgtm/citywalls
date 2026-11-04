package com.citywalls.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import coil.imageLoader
import com.citywalls.app.util.ImageQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "citywalls_settings")

enum class DownloadQuality(val key: String, val label: String, val description: String) {
    PREVIEW("preview", "Optimised", "Large preview size, quicker downloads"),
    ORIGINAL("original", "Original", "Full resolution file from Wikimedia Commons");

    companion object {
        fun fromKey(key: String?): DownloadQuality = entries.firstOrNull { it.key == key } ?: PREVIEW
    }
}

data class AppSettings(
    val imageQuality: ImageQuality = ImageQuality.BALANCED,
    val downloadQuality: DownloadQuality = DownloadQuality.PREVIEW,
    val pureBlack: Boolean = true,
    val wifiOnlyDownloads: Boolean = false
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val imageQuality = stringPreferencesKey("image_quality")
        val downloadQuality = stringPreferencesKey("download_quality")
        val pureBlack = booleanPreferencesKey("pure_black")
        val wifiOnly = booleanPreferencesKey("wifi_only_downloads")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        AppSettings(
            imageQuality = ImageQuality.fromKey(preferences[Keys.imageQuality]),
            downloadQuality = DownloadQuality.fromKey(preferences[Keys.downloadQuality]),
            pureBlack = preferences[Keys.pureBlack] ?: true,
            wifiOnlyDownloads = preferences[Keys.wifiOnly] ?: false
        )
    }

    suspend fun setImageQuality(quality: ImageQuality) {
        context.settingsDataStore.edit { it[Keys.imageQuality] = quality.key }
    }

    suspend fun setDownloadQuality(quality: DownloadQuality) {
        context.settingsDataStore.edit { it[Keys.downloadQuality] = quality.key }
    }

    suspend fun setPureBlack(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.pureBlack] = enabled }
    }

    suspend fun setWifiOnlyDownloads(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.wifiOnly] = enabled }
    }

    suspend fun cacheSizeBytes(): Long = withContext(Dispatchers.IO) {
        val dirs = listOf(
            File(context.cacheDir, "image_cache"),
            File(context.cacheDir, "http-cache")
        )
        dirs.sumOf { dirSize(it) }
    }

    suspend fun clearCaches() = withContext(Dispatchers.IO) {
        runCatching { context.imageLoader.memoryCache?.clear() }
        runCatching { context.imageLoader.diskCache?.clear() }
        listOf(
            File(context.cacheDir, "image_cache"),
            File(context.cacheDir, "http-cache")
        ).forEach { dir ->
            if (dir.exists()) dir.listFiles()?.forEach { child -> child.deleteRecursively() }
        }
    }

    private fun dirSize(dir: File): Long {
        if (!dir.exists()) return 0L
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }
}
