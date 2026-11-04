package com.citywalls.app.data.remote

/**
 * Commons thumbnail URLs follow /thumb/a/ab/File.jpg/<width>px-File.jpg,
 * so a lighter grid thumbnail can be derived from the preview URL without
 * spending a second API request per image.
 */
object CommonsUrls {

    private val thumbPattern = Regex("/(\\d+)px-([^/]+)$")

    fun scaled(url: String, width: Int): String {
        if (!url.contains("/thumb/")) return url
        val match = thumbPattern.find(url) ?: return url
        val currentWidth = match.groupValues[1].toIntOrNull() ?: return url
        if (currentWidth <= width) return url
        val fileName = match.groupValues[2]
        val prefix = url.substring(0, match.range.first)
        return prefix + "/" + width + "px-" + fileName
    }
}
