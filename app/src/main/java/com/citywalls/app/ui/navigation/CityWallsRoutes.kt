package com.citywalls.app.ui.navigation

object CityWallsRoutes {
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val FAVORITES = "favorites"
    const val DOWNLOADS = "downloads"
    const val SETTINGS = "settings"

    const val CITY = "city/{cityKey}"
    const val WALLPAPER = "wallpaper/{wallpaperId}"

    const val CREDITS = "credits"
    const val PRIVACY = "privacy"
    const val ABOUT = "about"

    fun city(key: String): String = "city/" + key

    fun wallpaper(id: String): String = "wallpaper/" + id

    val bottomBarRoutes: Set<String> = setOf(HOME, EXPLORE, FAVORITES, DOWNLOADS, SETTINGS)
}
