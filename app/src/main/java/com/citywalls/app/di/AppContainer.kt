package com.citywalls.app.di

import android.content.Context
import com.citywalls.app.data.local.AppDatabase
import com.citywalls.app.data.prefs.SettingsRepository
import com.citywalls.app.data.remote.CommonsApi
import com.citywalls.app.data.remote.CommonsParamsInterceptor
import com.citywalls.app.data.repository.WallpaperRepository
import com.citywalls.app.util.WallpaperApplier
import com.citywalls.app.util.WallpaperDownloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Hand-rolled dependency graph: one instance per process, created by the
 * Application class. Keeps the build simple (no annotation processor for DI)
 * while staying fully testable and modular.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val httpCache = Cache(File(appContext.cacheDir, "http-cache"), 40L * 1024L * 1024L)

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .cache(httpCache)
        .addInterceptor(CommonsParamsInterceptor())
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://commons.wikimedia.org/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val commonsApi: CommonsApi = retrofit.create(CommonsApi::class.java)

    private val database: AppDatabase = AppDatabase.get(appContext)

    val settingsRepository: SettingsRepository = SettingsRepository(appContext)

    val wallpaperRepository: WallpaperRepository = WallpaperRepository(
        api = commonsApi,
        favoriteDao = database.favoriteDao(),
        downloadDao = database.downloadDao(),
        settingsRepository = settingsRepository
    )

    val imageClient: OkHttpClient = okHttpClient

    val downloader: WallpaperDownloader = WallpaperDownloader(appContext, okHttpClient)

    val applier: WallpaperApplier = WallpaperApplier(appContext, okHttpClient)

    companion object {
        /**
         * Wikimedia asks every API client for a descriptive User-Agent.
         * Replace the contact address before publishing this app.
         */
        const val USER_AGENT = "CityWalls/1.0 (Android wallpaper app; contact: developer@example.com)"
    }
}
