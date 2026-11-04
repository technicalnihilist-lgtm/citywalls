package com.citywalls.app.data.remote

import com.citywalls.app.data.remote.dto.CommonsMetaValue
import com.citywalls.app.data.remote.dto.CommonsPage
import com.citywalls.app.domain.Wallpaper
import com.citywalls.app.util.HtmlText

object CommonsMapper {

    private const val MIN_HEIGHT = 800

    fun toWallpaper(
        page: CommonsPage,
        categoryKey: String,
        categoryLabel: String,
        thumbWidth: Int,
        minSourceWidth: Int
    ): Wallpaper? {
        val info = page.imageInfo?.firstOrNull() ?: return null
        val originalUrl = info.url ?: return null

        val mime = info.mime.orEmpty().lowercase()
        if (!mime.startsWith("image/")) return null
        if (mime.contains("svg") || mime.contains("tiff") || mime.contains("gif")) return null
        if (info.width < minSourceWidth || info.height < MIN_HEIGHT) return null

        val meta = info.extMetadata.orEmpty()
        val categories = page.categories.orEmpty().map { it.title }

        val licenseName = meta.text("LicenseShortName").ifBlank { meta.text("License") }
        val licenseCode = meta.text("License")

        val check = LicensePolicy.evaluate(
            licenseName = licenseName,
            licenseCode = licenseCode,
            usageTerms = meta.text("UsageTerms"),
            restrictions = meta.text("Restrictions"),
            categories = categories
        )
        if (!check.allowed) return null

        val previewUrl = info.thumbUrl ?: CommonsUrls.scaled(originalUrl, thumbWidth)
        val thumbUrl = CommonsUrls.scaled(previewUrl, 480)

        val artist = HtmlText.clean(meta.text("Artist"))
            .ifBlank { HtmlText.clean(meta.text("Credit")) }
            .ifBlank { "Unknown photographer" }

        val title = cleanTitle(page.title)
        val attribution = HtmlText.clean(meta.text("Attribution")).ifBlank { title + " by " + artist }
        val description = HtmlText.clean(meta.text("ImageDescription")).take(280)

        return Wallpaper(
            id = page.pageId.toString(),
            title = title,
            thumbUrl = thumbUrl,
            previewUrl = previewUrl,
            fullUrl = originalUrl,
            width = info.width,
            height = info.height,
            artist = artist,
            description = description,
            licenseName = licenseName.ifBlank { "Public domain" },
            licenseUrl = meta.text("LicenseUrl")
                .ifBlank { LicensePolicy.licenseUrlFor(licenseCode, licenseName) },
            sourcePageUrl = info.descriptionUrl.orEmpty(),
            attribution = attribution,
            categoryKey = categoryKey,
            categoryLabel = categoryLabel
        )
    }

    private fun cleanTitle(raw: String): String {
        val withoutPrefix = HtmlText.clean(raw).removePrefix("File:")
        val withoutExtension = if (withoutPrefix.contains('.')) {
            withoutPrefix.substringBeforeLast('.')
        } else {
            withoutPrefix
        }
        return withoutExtension.replace('_', ' ').trim().ifBlank { "City wallpaper" }
    }

    private fun Map<String, CommonsMetaValue>.text(key: String): String {
        val value = this[key]?.value ?: return ""
        return when (value) {
            is String -> value
            is Number -> value.toString()
            is Boolean -> value.toString()
            else -> ""
        }
    }
}
