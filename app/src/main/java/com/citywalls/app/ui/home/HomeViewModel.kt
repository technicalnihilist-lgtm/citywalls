package com.citywalls.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citywalls.app.data.repository.WallpaperRepository
import com.citywalls.app.domain.Catalog
import com.citywalls.app.domain.CatalogEntry
import com.citywalls.app.domain.SearchSort
import com.citywalls.app.domain.Wallpaper
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrendingCity(
    val entry: CatalogEntry,
    val headerUrl: String?
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val featured: List<Wallpaper> = emptyList(),
    val popular: List<Wallpaper> = emptyList(),
    val recent: List<Wallpaper> = emptyList(),
    val trendingCities: List<TrendingCity> = emptyList(),
    val error: String? = null
)

class HomeViewModel(private val repository: WallpaperRepository) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var job: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        job?.cancel()
        job = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val (featuredResult, popularResult, recentResult) = coroutineScope {
                val featured = async {
                    repository.search(
                        query = "city skyline night",
                        offset = 0,
                        sort = SearchSort.POPULAR,
                        categoryKey = "featured",
                        categoryLabel = "Featured"
                    )
                }
                val popular = async {
                    repository.search(
                        query = "famous landmark city",
                        offset = 0,
                        sort = SearchSort.POPULAR,
                        categoryKey = "popular",
                        categoryLabel = "Popular"
                    )
                }
                val recent = async {
                    repository.search(
                        query = "city architecture",
                        offset = 0,
                        sort = SearchSort.NEWEST,
                        categoryKey = "recent",
                        categoryLabel = "Recently added"
                    )
                }
                Triple(featured.await(), popular.await(), recent.await())
            }

            val featuredItems = featuredResult.getOrNull()?.items.orEmpty().take(8)
            val popularItems = popularResult.getOrNull()?.items.orEmpty().take(6)
            val recentItems = recentResult.getOrNull()?.items.orEmpty().take(6)

            val cityEntries = Catalog.featuredCities().take(6)
            val headers = coroutineScope {
                cityEntries.map { entry ->
                    async { TrendingCity(entry, repository.headerFor(entry)) }
                }.awaitAll()
            }

            val failure = featuredResult.exceptionOrNull()
                ?: popularResult.exceptionOrNull()
                ?: recentResult.exceptionOrNull()

            _state.value = HomeUiState(
                isLoading = false,
                featured = featuredItems,
                popular = popularItems,
                recent = recentItems,
                trendingCities = headers,
                error = if (featuredItems.isEmpty() && popularItems.isEmpty()) {
                    failure?.message
                        ?: "No licensed wallpapers came back this time. Every image is checked against its Wikimedia Commons license before display."
                } else {
                    null
                }
            )
        }
    }
}
