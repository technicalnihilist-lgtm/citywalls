package com.citywalls.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.size
import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.citywalls.app.R
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.citywalls.app.ui.city.CityDetailScreen
import com.citywalls.app.ui.detail.WallpaperDetailScreen
import com.citywalls.app.ui.downloads.DownloadsScreen
import com.citywalls.app.ui.explore.ExploreScreen
import com.citywalls.app.ui.favorites.FavoritesScreen
import com.citywalls.app.ui.home.HomeScreen
import com.citywalls.app.ui.legal.AboutScreen
import com.citywalls.app.ui.legal.CreditsScreen
import com.citywalls.app.ui.legal.PrivacyPolicyScreen
import com.citywalls.app.ui.settings.SettingsScreen
import com.citywalls.app.ui.theme.Champagne
import com.citywalls.app.ui.theme.TextSecondary

@Composable
fun CityWallsNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = CityWallsRoutes.HOME,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(180)) },
        popEnterTransition = { fadeIn(animationSpec = tween(220)) },
        popExitTransition = { fadeOut(animationSpec = tween(180)) }
    ) {
        composable(CityWallsRoutes.HOME) {
            HomeScreen(
                onOpenWallpaper = { id -> navController.navigate(CityWallsRoutes.wallpaper(id)) },
                onOpenCity = { key -> navController.navigate(CityWallsRoutes.city(key)) },
                onOpenExplore = { navController.navigate(CityWallsRoutes.EXPLORE) },
                onOpenCredits = { navController.navigate(CityWallsRoutes.CREDITS) }
            )
        }

        composable(CityWallsRoutes.EXPLORE) {
            ExploreScreen(
                onOpenCity = { key -> navController.navigate(CityWallsRoutes.city(key)) },
                onOpenWallpaper = { id -> navController.navigate(CityWallsRoutes.wallpaper(id)) }
            )
        }

        composable(CityWallsRoutes.FAVORITES) {
            FavoritesScreen(
                onOpenWallpaper = { id -> navController.navigate(CityWallsRoutes.wallpaper(id)) }
            )
        }

        composable(CityWallsRoutes.DOWNLOADS) {
            DownloadsScreen(
                onOpenWallpaper = { id -> navController.navigate(CityWallsRoutes.wallpaper(id)) }
            )
        }

        composable(CityWallsRoutes.SETTINGS) {
            SettingsScreen(
                onOpenCredits = { navController.navigate(CityWallsRoutes.CREDITS) },
                onOpenPrivacy = { navController.navigate(CityWallsRoutes.PRIVACY) },
                onOpenAbout = { navController.navigate(CityWallsRoutes.ABOUT) }
            )
        }

        composable(CityWallsRoutes.CITY) { backStackEntry ->
            val cityKey = backStackEntry.arguments?.getString("cityKey").orEmpty()
            CityDetailScreen(
                cityKey = cityKey,
                onBack = { navController.popBackStack() },
                onOpenWallpaper = { id -> navController.navigate(CityWallsRoutes.wallpaper(id)) },
                onOpenCity = { key -> navController.navigate(CityWallsRoutes.city(key)) }
            )
        }

        composable(CityWallsRoutes.WALLPAPER) { backStackEntry ->
            val wallpaperId = backStackEntry.arguments?.getString("wallpaperId").orEmpty()
            WallpaperDetailScreen(
                wallpaperId = wallpaperId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(CityWallsRoutes.CREDITS) {
            CreditsScreen(onBack = { navController.popBackStack() })
        }

        composable(CityWallsRoutes.PRIVACY) {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }

        composable(CityWallsRoutes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}

private data class BottomItem(
    val route: String,
    val label: String,
    @DrawableRes val icon: Int
)

private val bottomItems = listOf(
    BottomItem(CityWallsRoutes.HOME, "Home", R.drawable.ic_home),
    BottomItem(CityWallsRoutes.EXPLORE, "Explore", R.drawable.ic_explore),
    BottomItem(CityWallsRoutes.FAVORITES, "Saved", R.drawable.ic_favorite),
    BottomItem(CityWallsRoutes.DOWNLOADS, "Downloads", R.drawable.ic_download),
    BottomItem(CityWallsRoutes.SETTINGS, "Settings", R.drawable.ic_settings)
)

@Composable
fun CityWallsBottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar(
        containerColor = Color.Black.copy(alpha = 0.95f),
        contentColor = Champagne,
        tonalElevation = 0.dp
    ) {
        bottomItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Champagne,
                    selectedTextColor = Champagne,
                    indicatorColor = Color.White.copy(alpha = 0.06f),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}

