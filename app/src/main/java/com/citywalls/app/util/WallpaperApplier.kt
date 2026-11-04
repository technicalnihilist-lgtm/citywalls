package com.citywalls.app.util

import android.content.Context
import com.citywalls.app.domain.Wallpaper
import com.citywalls.app.domain.WallpaperTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.File

/**
 * Loads the best available bitmap (local download first, network otherwise),
 * crops it to the current screen and hands it to the system wallpaper manager.
 */
class WallpaperApplier(
    private val context: Context,
    private val client: OkHttpClient
) {

    suspend fun apply(
        wallpaper: Wallpaper,
        localPath: String?,
        target: WallpaperTarget,
        maxWidth: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = if (localPath != null && File(localPath).exists()) {
                BitmapUtils.decodeFile(File(localPath), maxWidth)
            } else {
                BitmapUtils.downloadAndDecode(client, wallpaper.fullUrl, maxWidth)
            }
            val metrics = context.resources.displayMetrics
            val cropped = BitmapUtils.centerCrop(bitmap, metrics.widthPixels, metrics.heightPixels)
            WallpaperSetter.apply(context, cropped, target)
            cropped.recycle()
        }
    }
}
