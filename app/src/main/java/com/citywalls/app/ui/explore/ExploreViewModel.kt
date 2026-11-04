package com.citywalls.app.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citywalls.app.data.repository.WallpaperRepository
import com.citywalls.app.domain.Catalog
import com.citywalls.app.domain.SearchSort
import com.citywalls.app.ui.common.WallpaperPaginator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExploreUiState(
    val query: String = "",
    val activeQuery: String = "",
    val showResults: Boolean = false,
    val selectedGroupKey: String = Catalog.ALL_GROUP
)

class ExploreViewModel(private val repository: WallpaperRepository) : ViewModel() {

    val paginator = WallpaperPaginator(repository, viewModelScope)

    private val _ui = MutableStateFlow(ExploreUiState())
    val ui: StateFlow<ExploreUiState> = _ui.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(value: String) {
        _ui.update { it.copy(query = value) }
        searchJob?.cancel()

        val trimmed = value.trim()
        if (trimmed.isBlank()) {
            _ui.update { it.copy(showResults = false, activeQuery = "") }
            return
        }

        searchJob = viewModelScope.launch {
            delay(450)
            _ui.update { it.copy(showResults = true, activeQuery = trimmed) }
            paginator.start(
                query = trimmed,
                categoryKey = "search",
                categoryLabel = "Search"
            )
        }
    }

    fun onSelectGroup(groupKey: String) {
        _ui.update { it.copy(selectedGroupKey = groupKey) }
    }

    fun onSelectSort(sort: SearchSort) {
        paginator.changeSort(sort)
    }

    fun loadMore() {
        paginator.loadMore()
    }

    fun retry() {
        paginator.retry()
    }
}
