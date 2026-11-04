package com.citywalls.app.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citywalls.app.data.repository.WallpaperRepository
import com.citywalls.app.domain.Wallpaper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: WallpaperRepository) : ViewModel() {

    val favorites: StateFlow<List<Wallpaper>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun remove(id: String) {
        viewModelScope.launch { repository.removeFavorite(id) }
    }
}
