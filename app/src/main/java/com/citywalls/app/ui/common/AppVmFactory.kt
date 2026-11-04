package com.citywalls.app.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.citywalls.app.CityWallsApplication
import com.citywalls.app.di.AppContainer

/**
 * Tiny factory that wires a ViewModel from the app's dependency container,
 * so screens never construct repositories themselves.
 */
class AppVmFactory<VM : ViewModel>(
    private val create: (AppContainer) -> VM
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = checkNotNull(
            extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
        ) as CityWallsApplication
        val viewModel = create(application.container)
        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
