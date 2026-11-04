package com.citywalls.app.util

enum class ImageQuality(
    val key: String,
    val label: String,
    val description: String,
    val previewWidth: Int,
    val minSourceWidth: Int,
    val applyWidth: Int
) {
    DATA_SAVER(
        key = "data_saver",
        label = "Data saver",
        description = "Smaller thumbnails, lighter on mobile data",
        previewWidth = 1024,
        minSourceWidth = 900,
        applyWidth = 1600
    ),
    BALANCED(
        key = "balanced",
        label = "Balanced",
        description = "Crisp thumbnails, moderate data use",
        previewWidth = 1600,
        minSourceWidth = 1200,
        applyWidth = 2560
    ),
    BEST(
        key = "best",
        label = "Best available",
        description = "Largest previews, highest data use",
        previewWidth = 2400,
        minSourceWidth = 1500,
        applyWidth = 3840
    );

    companion object {
        fun fromKey(key: String?): ImageQuality = entries.firstOrNull { it.key == key } ?: BALANCED
    }
}

object Formatters {
    fun bytes(value: Long): String {
        if (value <= 0L) return "0 MB"
        val mb = value / (1024.0 * 1024.0)
        return if (mb < 1.0) {
            val kb = (value / 1024.0).toInt().coerceAtLeast(1)
            "$kb KB"
        } else {
            String.format("%.1f MB", mb)
        }
    }
}
