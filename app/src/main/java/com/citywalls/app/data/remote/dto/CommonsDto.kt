package com.citywalls.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CommonsSearchResponse(
    @SerializedName("batchcomplete") val batchComplete: Boolean? = null,
    @SerializedName("continue") val continueToken: CommonsContinue? = null,
    @SerializedName("query") val query: CommonsQuery? = null
)

data class CommonsContinue(
    @SerializedName("gsroffset") val gsrOffset: Int? = null,
    @SerializedName("continue") val continueValue: String? = null
)

data class CommonsQuery(
    @SerializedName("pages") val pages: List<CommonsPage>? = null
)

data class CommonsPage(
    @SerializedName("pageid") val pageId: Long = 0L,
    @SerializedName("title") val title: String = "",
    @SerializedName("imageinfo") val imageInfo: List<CommonsImageInfo>? = null,
    @SerializedName("categories") val categories: List<CommonsCategory>? = null
)

data class CommonsCategory(
    @SerializedName("title") val title: String = ""
)

data class CommonsImageInfo(
    @SerializedName("url") val url: String? = null,
    @SerializedName("descriptionurl") val descriptionUrl: String? = null,
    @SerializedName("thumburl") val thumbUrl: String? = null,
    @SerializedName("thumbwidth") val thumbWidth: Int = 0,
    @SerializedName("thumbheight") val thumbHeight: Int = 0,
    @SerializedName("width") val width: Int = 0,
    @SerializedName("height") val height: Int = 0,
    @SerializedName("mime") val mime: String? = null,
    @SerializedName("extmetadata") val extMetadata: Map<String, CommonsMetaValue>? = null
)

data class CommonsMetaValue(
    @SerializedName("value") val value: Any? = null,
    @SerializedName("source") val source: String? = null
)
