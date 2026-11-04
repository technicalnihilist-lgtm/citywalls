package com.citywalls.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class CheckResult(
    val label: String,
    val ok: Boolean,
    val detail: String
)

/**
 * Runs the three network hops the app needs, in order, and reports exactly
 * which one fails. Used by the "Run connection check" button in Settings.
 */
class Diagnostics(private val client: OkHttpClient) {

    private val apiUrl = "https://commons.wikimedia.org/w/api.php" +
        "?action=query&format=json&formatversion=2&prop=imageinfo" +
        "&iiprop=url&generator=search&gsrnamespace=6&gsrlimit=1" +
        "&gsrsearch=Tokyo%20skyline&iiurlwidth=480"

    suspend fun run(): List<CheckResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<CheckResult>()

        val api = get(apiUrl)
        results += api.fold(
            onSuccess = { body ->
                val pages = runCatching {
                    JSONObject(body).getJSONObject("query").getJSONArray("pages").length()
                }.getOrDefault(0)
                if (pages > 0) {
                    CheckResult("Wikimedia API", true, "reachable, $pages result")
                } else {
                    CheckResult("Wikimedia API", false, "reachable but returned no results")
                }
            },
            onFailure = { CheckResult("Wikimedia API", false, it.message ?: "failed") }
        )

        val thumb = runCatching {
            JSONObject(api.getOrThrow())
                .getJSONObject("query").getJSONArray("pages")
                .getJSONObject(0).getJSONArray("imageinfo")
                .getJSONObject(0).optString("thumburl").ifBlank { "none" }
        }.getOrDefault("none")

        if (thumb == "none" || thumb.isEmpty()) {
            results += CheckResult("Image CDN", false, "no thumbnail URL returned by the API")
        } else {
            results += get(thumb).fold(
                onSuccess = { CheckResult("Image CDN", true, "image bytes received") },
                onFailure = { CheckResult("Image CDN", false, it.message ?: "failed") }
            )
        }

        results
    }

    private fun get(url: String): Result<String> = runCatching {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "CityWalls/1.0 (Android wallpaper app; contact: developer@example.com)")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP " + response.code)
            val body = response.body ?: error("empty response")
            if (url.contains("api.php")) body.string() else body.byteStream().readBytes().size.toString()
        }
    }
}
