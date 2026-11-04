package com.citywalls.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citywalls.app.data.prefs.AppSettings
import com.citywalls.app.data.prefs.DownloadQuality
import com.citywalls.app.data.prefs.SettingsRepository
import com.citywalls.app.util.Formatters
import com.citywalls.app.util.ImageQuality
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private val _cacheSize = MutableStateFlow("Checking")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        refreshCacheSize()
    }

    fun refreshCacheSize() {
        viewModelScope.launch {
            _cacheSize.value = Formatters.bytes(settingsRepository.cacheSizeBytes())
        }
    }

    fun setImageQuality(quality: ImageQuality) {
        viewModelScope.launch { settingsRepository.setImageQuality(quality) }
    }

    fun setDownloadQuality(quality: DownloadQuality) {
        viewModelScope.launch { settingsRepository.setDownloadQuality(quality) }
    }

    fun setPureBlack(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setPureBlack(enabled) }
    }

    fun clearCache() {
        viewModelScope.launch {
            settingsRepository.clearCaches()
            refreshCacheSize()
            _message.value = "Image cache cleared"
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
