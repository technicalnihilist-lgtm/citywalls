package com.citywalls.app.ui.common

import androidx.compose.runtime.staticCompositionLocalOf
import com.citywalls.app.di.AppContainer

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer was not provided to the composition")
}
