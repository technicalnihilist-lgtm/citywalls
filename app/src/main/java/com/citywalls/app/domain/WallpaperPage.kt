package com.citywalls.app.domain

data class WallpaperPage(
    val items: List<Wallpaper>,
    val nextOffset: Int?,
    val hasMore: Boolean
)

data class DownloadedWallpaper(
    val wallpaper: Wallpaper,
    val localPath: String,
    val sizeBytes: Long,
    val downloadedAt: Long
)

enum class SearchSort(val label: String, val apiValue: String) {
    POPULAR("Popular", "relevance"),
    NEWEST("Newest", "create_timestamp_desc")
}

enum class WallpaperTarget(val label: String, val shortLabel: String) {
    HOME("Home screen", "Home"),
    LOCK("Lock screen", "Lock"),
    BOTH("Home and lock screen", "Both")
}
