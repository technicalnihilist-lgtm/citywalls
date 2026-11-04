package com.citywalls.app

import android.app.Activity
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Boots the real Application and MainActivity on the JVM so startup crashes
 * (dependency graph, Room, DataStore, Compose composition, view models) surface
 * as a stack trace instead of a black screen on a device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
class AppLaunchTest {

    @Test
    fun applicationStartsAndBuildsContainer() {
        val app = ApplicationProvider.getApplicationContext<CityWallsApplication>()
        app.onCreate()
        assertNotNull("AppContainer must be created in Application.onCreate", app.container)
        assertNotNull(app.container.wallpaperRepository)
        assertNotNull(app.container.settingsRepository)
    }

    @Test
    fun mainActivityLaunchesAndComposes() {
        val controller = Robolectric.buildActivity(MainActivity::class.java)
        val activity: Activity = controller.setup().get()
        assertNotNull(activity)
        assertNotNull("MainActivity must compose a content view", activity.window.decorView)
    }
}
