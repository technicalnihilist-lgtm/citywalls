package com.citywalls.app.domain

/**
 * A single wallpaper, always carrying its full provenance so the UI can show
 * title, author, source page, license name and license URL together.
 */
data class Wallpaper(
    val id: String,
    val title: String,
    val thumbUrl: String,
    val previewUrl: String,
    val fullUrl: String,
    val width: Int,
    val height: Int,
    val artist: String,
    val description: String,
    val licenseName: String,
    val licenseUrl: String,
    val sourcePageUrl: String,
    val attribution: String,
    val categoryKey: String,
    val categoryLabel: String,
    val modificationNote: String = DEFAULT_MODIFICATION_NOTE
) {

    val resolutionLabel: String
        get() = if (width > 0 && height > 0) "$width x $height px" else "Resolution unavailable"

    val attributionLine: String
        get() = buildString {
            append(title)
            append(" by ")
            append(artist)
            append(" / ")
            append(licenseName)
            if (sourcePageUrl.isNotBlank()) {
                append(" / ")
                append(sourcePageUrl)
            }
        }

    val shareText: String
        get() = "$title\nPhoto: $artist\nLicense: $licenseName\nSource: $sourcePageUrl\nShared from CityWalls"

    val isPortrait: Boolean
        get() = height >= width

    companion object {
        const val DEFAULT_MODIFICATION_NOTE =
            "Shown as a resized thumbnail; cropped to your screen size when applied or downloaded."
    }
}
