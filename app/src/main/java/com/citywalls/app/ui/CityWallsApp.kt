package com.citywalls.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.citywalls.app.data.prefs.AppSettings
import com.citywalls.app.ui.common.LocalAppContainer
import com.citywalls.app.ui.navigation.CityWallsBottomBar
import com.citywalls.app.ui.navigation.CityWallsNavHost
import com.citywalls.app.ui.navigation.CityWallsRoutes
import com.citywalls.app.ui.theme.CityWallsTheme

@Composable
fun CityWallsApp() {
    val container = LocalAppContainer.current
    val settings by container.settingsRepository.settings.collectAsStateWithLifecycle(
        initialValue = AppSettings()
    )

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute != null && CityWallsRoutes.bottomBarRoutes.contains(currentRoute)

    CityWallsTheme(pureBlack = settings.pureBlack) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    CityWallsBottomBar(
                        navController = navController,
                        currentRoute = currentRoute
                    )
                }
            }
        ) { innerPadding ->
            CityWallsNavHost(
                navController = navController,
                modifier = if (showBottomBar) {
                    Modifier.padding(innerPadding)
                } else {
                    Modifier
                }
            )
        }
    }
}
