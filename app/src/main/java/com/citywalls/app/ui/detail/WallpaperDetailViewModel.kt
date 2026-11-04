package com.citywalls.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citywalls.app.data.prefs.DownloadQuality
import com.citywalls.app.data.prefs.SettingsRepository
import com.citywalls.app.data.repository.WallpaperRepository
import com.citywalls.app.domain.Wallpaper
import com.citywalls.app.domain.WallpaperTarget
import com.citywalls.app.util.NetworkStatus
import com.citywalls.app.util.WallpaperApplier
import com.citywalls.app.util.WallpaperDownloader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class WallpaperDetailViewModel(
    private val repository: WallpaperRepository,
    private val downloader: WallpaperDownloader,
    private val applier: WallpaperApplier,
    private val settingsRepository: SettingsRepository,
    private val networkStatus: NetworkStatus,
    private val wallpaperId: String
) : ViewModel() {

    data class UiState(
        val wallpaper: Wallpaper? = null,
        val isLoading: Boolean = true,
        val error: String? = null,
        val isFavorite: Boolean = false,
        val localPath: String? = null,
        val isBusy: Boolean = false,
        val busyLabel: String? = null,
        val message: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val wallpaper = repository.findWallpaper(wallpaperId)
            val favorite = repository.isFavorite(wallpaperId).first()
            val local = repository.localPathFor(wallpaperId)
            _state.update {
                it.copy(
                    wallpaper = wallpaper,
                    isLoading = false,
                    isFavorite = favorite,
                    localPath = local,
                    error = if (wallpaper == null) {
                        "This wallpaper is no longer available. It may have been removed from Wikimedia Commons."
                    } else {
                        null
                    }
                )
            }
        }
    }

    fun toggleFavorite() {
        val wallpaper = _state.value.wallpaper ?: return
        viewModelScope.launch {
            val saved = repository.toggleFavorite(wallpaper)
            _state.update {
                it.copy(
                    isFavorite = saved,
                    message = if (saved) "Added to favorites" else "Removed from favorites"
                )
            }
        }
    }

    fun download() {
        val wallpaper = _state.value.wallpaper ?: return
        if (_state.value.isBusy) return
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, busyLabel = "Downloading", message = null) }
            val settings = settingsRepository.settings.first()

            // Honour the "download on Wi-Fi only" preference before touching the network.
            if (settings.wifiOnlyDownloads && networkStatus.isMetered()) {
                _state.update {
                    it.copy(
                        isBusy = false,
                        busyLabel = null,
                        message = "Wi-Fi only downloads are on. Connect to Wi-Fi, or turn that option off in Settings."
                    )
                }
                return@launch
            }

            val original = settings.downloadQuality == DownloadQuality.ORIGINAL
            downloader.download(wallpaper, original).fold(
                onSuccess = { saved ->
                    repository.saveDownload(wallpaper, saved.file.absolutePath, saved.sizeBytes)
                    _state.update {
                        it.copy(
                            isBusy = false,
                            busyLabel = null,
                            localPath = saved.file.absolutePath,
                            message = if (saved.exportedToGallery) {
                                "Saved to your gallery under Pictures/CityWalls"
                            } else {
                                "Saved inside CityWalls downloads"
                            }
                        )
                    }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isBusy = false,
                            busyLabel = null,
                            message = throwable.message ?: "Download failed"
                        )
                    }
                }
            )
        }
    }

    fun applyWallpaper(target: WallpaperTarget) {
        val wallpaper = _state.value.wallpaper ?: return
        if (_state.value.isBusy) return
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, busyLabel = "Applying", message = null) }
            val quality = settingsRepository.settings.first().imageQuality
            applier.apply(
                wallpaper = wallpaper,
                localPath = _state.value.localPath,
                target = target,
                maxWidth = quality.applyWidth
            ).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isBusy = false,
                            busyLabel = null,
                            message = "Set as " + target.label.toLowerCase()
                        )
                    }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isBusy = false,
                            busyLabel = null,
                            message = throwable.message ?: "Could not set the wallpaper"
                        )
                    }
                }
            )
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    fun localFile(): File? = _state.value.localPath?.let { File(it) }
}
