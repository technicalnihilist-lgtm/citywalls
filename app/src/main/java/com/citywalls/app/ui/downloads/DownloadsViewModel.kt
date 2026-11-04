package com.citywalls.app.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citywalls.app.data.prefs.SettingsRepository
import com.citywalls.app.data.repository.WallpaperRepository
import com.citywalls.app.domain.DownloadedWallpaper
import com.citywalls.app.domain.WallpaperTarget
import com.citywalls.app.util.WallpaperApplier
import com.citywalls.app.util.WallpaperDownloader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadsViewModel(
    private val repository: WallpaperRepository,
    private val downloader: WallpaperDownloader,
    private val applier: WallpaperApplier,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val downloads: StateFlow<List<DownloadedWallpaper>> = repository.downloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _busyId = MutableStateFlow<String?>(null)
    val busyId: StateFlow<String?> = _busyId.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun applyDownloaded(item: DownloadedWallpaper) {
        if (_busyId.value != null) return
        viewModelScope.launch {
            _busyId.value = item.wallpaper.id
            val quality = settingsRepository.settings.first().imageQuality
            applier.apply(
                wallpaper = item.wallpaper,
                localPath = item.localPath,
                target = WallpaperTarget.BOTH,
                maxWidth = quality.applyWidth
            ).fold(
                onSuccess = { _message.value = "Set as home and lock screen" },
                onFailure = { throwable ->
                    _message.value = throwable.message ?: "Could not set the wallpaper"
                }
            )
            _busyId.value = null
        }
    }

    fun delete(item: DownloadedWallpaper) {
        viewModelScope.launch {
            downloader.delete(item.wallpaper.id)
            repository.removeDownload(item.wallpaper.id)
            _message.value = "Removed from downloads"
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
