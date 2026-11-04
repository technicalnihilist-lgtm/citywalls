package com.citywalls.app.ui.common

import com.citywalls.app.data.repository.WallpaperRepository
import com.citywalls.app.domain.SearchSort
import com.citywalls.app.domain.Wallpaper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Offset pagination over a Commons search, with duplicate suppression across
 * pages, retry support and a hard guard against concurrent requests.
 */
class WallpaperPaginator(
    private val repository: WallpaperRepository,
    private val scope: CoroutineScope
) {

    data class State(
        val items: List<Wallpaper> = emptyList(),
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val error: String? = null,
        val hasMore: Boolean = true,
        val sort: SearchSort = SearchSort.POPULAR
    ) {
        val isEmpty: Boolean
            get() = items.isEmpty() && !isLoading && error == null
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var query: String = ""
    private var categoryKey: String = ""
    private var categoryLabel: String = ""
    private var sort: SearchSort = SearchSort.POPULAR
    private var nextOffset: Int = 0
    private var job: Job? = null
    private val seenIds = HashSet<String>()

    fun start(query: String, categoryKey: String, categoryLabel: String, sort: SearchSort = SearchSort.POPULAR) {
        this.query = query
        this.categoryKey = categoryKey
        this.categoryLabel = categoryLabel
        this.sort = sort
        firstPage()
    }

    fun changeSort(newSort: SearchSort) {
        if (newSort == sort) return
        sort = newSort
        _state.update { it.copy(sort = newSort) }
        firstPage()
    }

    fun retry() {
        if (_state.value.items.isEmpty()) {
            firstPage()
        } else {
            loadMore()
        }
    }

    fun loadMore() {
        val current = _state.value
        if (current.isLoading || current.isLoadingMore || !current.hasMore) return
        fetch(append = true)
    }

    private fun firstPage() {
        if (query.isBlank()) {
            _state.value = State(sort = sort, hasMore = false)
            return
        }
        job?.cancel()
        seenIds.clear()
        nextOffset = 0
        _state.value = State(isLoading = true, hasMore = true, sort = sort)
        fetch(append = false)
    }

    private fun fetch(append: Boolean) {
        job = scope.launch {
            if (append) {
                _state.update { it.copy(isLoadingMore = true, error = null) }
            } else {
                _state.update { it.copy(isLoading = true, error = null) }
            }

            repository.search(
                query = query,
                offset = nextOffset,
                sort = sort,
                categoryKey = categoryKey,
                categoryLabel = categoryLabel
            ).fold(
                onSuccess = { page ->
                    val fresh = page.items.filter { seenIds.add(it.id) }
                    nextOffset = page.nextOffset ?: 0
                    _state.update { current ->
                        current.copy(
                            items = if (append) current.items + fresh else fresh,
                            isLoading = false,
                            isLoadingMore = false,
                            hasMore = page.hasMore,
                            error = null,
                            sort = sort
                        )
                    }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = throwable.message ?: "Could not load wallpapers"
                        )
                    }
                }
            )
        }
    }
}
