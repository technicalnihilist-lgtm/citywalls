package com.citywalls.app.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.citywalls.app.domain.Wallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class WallpaperDownloader(
    private val context: Context,
    private val client: OkHttpClient
) {

    data class SavedFile(
        val file: File,
        val sizeBytes: Long,
        val mimeType: String,
        val exportedToGallery: Boolean
    )

    private val directory: File
        get() {
            val base = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
            val dir = File(base, "CityWalls")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun existingFile(wallpaper: Wallpaper): File? {
        val dir = directory
        return dir.listFiles()?.firstOrNull { it.name.startsWith("citywalls_" + wallpaper.id + ".") }
    }

    suspend fun download(wallpaper: Wallpaper, original: Boolean): Result<SavedFile> =
        withContext(Dispatchers.IO) {
            runCatching {
                existingFile(wallpaper)?.let { file ->
                    if (file.length() > 1024) {
                        return@runCatching SavedFile(file, file.length(), mimeTypeOf(file.name), false)
                    }
                }

                val url = if (original) wallpaper.fullUrl else wallpaper.previewUrl
                val extension = extensionOf(url)
                val file = File(directory, "citywalls_" + wallpaper.id + "." + extension)

                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) error("Download failed (HTTP " + response.code + ")")
                    val body = response.body ?: error("Empty download response")
                    file.outputStream().use { output -> body.byteStream().copyTo(output) }
                }

                if (file.length() < 1024) {
                    file.delete()
                    error("The downloaded file was incomplete, please retry")
                }

                val exported = runCatching { exportToGallery(file, wallpaper) }.getOrDefault(false)
                SavedFile(file, file.length(), mimeTypeOf(file.name), exported)
            }
        }

    fun delete(wallpaperId: String): Boolean {
        val dir = directory
        var deleted = false
        dir.listFiles()?.forEach { file ->
            if (file.name.startsWith("citywalls_" + wallpaperId + ".")) {
                deleted = file.delete() || deleted
            }
        }
        return deleted
    }

    private fun exportToGallery(file: File, wallpaper: Wallpaper): Boolean {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.TITLE, wallpaper.title)
            put(MediaStore.Images.Media.DESCRIPTION, wallpaper.attributionLine)
            put(MediaStore.Images.Media.ARTIST, wallpaper.artist)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/CityWalls"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        resolver.openOutputStream(uri)?.use { output ->
            file.inputStream().use { input -> input.copyTo(output) }
        } ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val done = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
            resolver.update(uri, done, null, null)
        }
        return true
    }

    private fun extensionOf(url: String): String {
        val cleaned = url.substringBefore('?')
        val candidate = cleaned.substringAfterLast('.', "jpg").lowercase()
        return when (candidate) {
            "jpg", "jpeg" -> "jpg"
            "png" -> "png"
            "webp" -> "webp"
            else -> "jpg"
        }
    }

    private fun mimeTypeOf(name: String): String = when {
        name.endsWith(".png", true) -> "image/png"
        name.endsWith(".webp", true) -> "image/webp"
        else -> "image/jpeg"
    }

    companion object {
        private const val USER_AGENT = "CityWalls/1.0 (Android wallpaper app)"
    }
}
