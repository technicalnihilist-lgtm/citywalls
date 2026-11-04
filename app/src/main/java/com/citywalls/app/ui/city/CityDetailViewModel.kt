package com.citywalls.app.ui.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citywalls.app.data.repository.WallpaperRepository
import com.citywalls.app.domain.Catalog
import com.citywalls.app.domain.CatalogEntry
import com.citywalls.app.ui.common.WallpaperPaginator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CityDetailState(
    val entry: CatalogEntry? = null,
    val headerUrl: String? = null,
    val isLoadingHeader: Boolean = false,
    val related: List<CatalogEntry> = emptyList()
)

class CityDetailViewModel(
    repository: WallpaperRepository,
    cityKey: String
) : ViewModel() {

    val entry: CatalogEntry? = Catalog.find(cityKey)

    val paginator = WallpaperPaginator(repository, viewModelScope)

    private val repositoryRef = repository

    private val _state = MutableStateFlow(CityDetailState(entry = entry))
    val state: StateFlow<CityDetailState> = _state.asStateFlow()

    init {
        val current = entry
        if (current != null) {
            paginator.start(
                query = current.searchQuery,
                categoryKey = current.key,
                categoryLabel = current.name
            )
            loadHeader(current)
        }
    }

    private fun loadHeader(current: CatalogEntry) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingHeader = true) }
            val url = repositoryRef.headerFor(current)
            _state.update {
                it.copy(
                    headerUrl = url,
                    isLoadingHeader = false,
                    related = Catalog.relatedFor(current)
                )
            }
        }
    }
}
