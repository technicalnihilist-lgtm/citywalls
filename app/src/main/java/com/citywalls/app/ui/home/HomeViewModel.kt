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

            // 1. Featured shelf first, so the screen fills as soon as one request lands.
            val featuredResult = repository.search(
                query = "city skyline night",
                offset = 0,
                sort = SearchSort.POPULAR,
                categoryKey = "featured",
                categoryLabel = "Featured"
            )
            val featured = featuredResult.getOrNull()?.items.orEmpty().take(8)
            _state.update { it.copy(featured = featured, isLoading = featured.isEmpty()) }

            // 2. The remaining shelves in parallel.
            coroutineScope {
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
                val popularItems = popular.await().getOrNull()?.items.orEmpty().take(6)
                val recentItems = recent.await().getOrNull()?.items.orEmpty().take(6)
                _state.update {
                    it.copy(popular = popularItems, recent = recentItems, isLoading = false)
                }
            }

            // 3. City headers are the slowest, so they fill in last.
            val cities = Catalog.featuredCities().take(6)
            val headers = coroutineScope {
                cities.map { entry -> async { TrendingCity(entry, repository.headerFor(entry)) } }.awaitAll()
            }

            _state.update { current ->
                current.copy(
                    trendingCities = headers,
                    isLoading = false,
                    error = if (current.featured.isEmpty() && current.popular.isEmpty()) {
                        "Could not reach Wikimedia Commons. Check your connection, then tap refresh."
                    } else {
                        null
                    }
                )
            }
        }
    }

}
