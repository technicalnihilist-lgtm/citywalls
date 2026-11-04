package com.citywalls.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import kotlin.math.max

object BitmapUtils {

    private const val USER_AGENT = "CityWalls/1.0 (Android wallpaper app)"

    fun decodeFile(file: File, maxWidth: Int): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize(bounds.outWidth, maxWidth) }
        return BitmapFactory.decodeFile(file.absolutePath, options)
            ?: error("This image could not be decoded")
    }

    fun downloadAndDecode(client: OkHttpClient, url: String, maxWidth: Int): Bitmap {
        val request = Request.Builder().url(url).header("User-Agent", USER_AGENT).build()
        val bytes = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Image download failed (HTTP " + response.code + ")")
            val body = response.body ?: error("Empty image response")
            body.bytes()
        }
        return decodeBytes(bytes, maxWidth)
    }

    fun decodeBytes(bytes: ByteArray, maxWidth: Int): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize(bounds.outWidth, maxWidth) }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: error("This image could not be decoded")
    }

    private fun sampleSize(sourceWidth: Int, maxWidth: Int): Int {
        if (sourceWidth <= 0 || maxWidth <= 0) return 1
        var sample = 1
        while (sourceWidth / (sample * 2) >= maxWidth) {
            sample *= 2
        }
        return sample
    }

    /** Centre-crop to the exact screen aspect so wallpapers are never letterboxed. */
    fun centerCrop(source: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        if (targetWidth <= 0 || targetHeight <= 0) return source
        val sourceRatio = source.width.toFloat() / source.height.toFloat()
        val targetRatio = targetWidth.toFloat() / targetHeight.toFloat()
        val cropWidth: Int
        val cropHeight: Int
        if (sourceRatio > targetRatio) {
            cropHeight = source.height
            cropWidth = (source.height * targetRatio).toInt().coerceIn(1, source.width)
        } else {
            cropWidth = source.width
            cropHeight = (source.width / targetRatio).toInt().coerceIn(1, source.height)
        }
        val x = max(0, (source.width - cropWidth) / 2)
        val y = max(0, (source.height - cropHeight) / 2)
        val cropped = Bitmap.createBitmap(source, x, y, cropWidth, cropHeight)
        return if (cropped.width == targetWidth && cropped.height == targetHeight) {
            cropped
        } else {
            Bitmap.createScaledBitmap(cropped, targetWidth, targetHeight, true)
        }
    }
}
