package com.citywalls.app.ui.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.citywalls.app.ui.components.BackHeader

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            BackHeader(
                title = "Privacy policy",
                subtitle = "Short, because the app collects very little",
                onBack = onBack
            )
        }
        item {
            LegalCard(title = "WHAT WE STORE") {
                LegalBody(
                    "Favorites and download records are stored only in a local database on your device. " +
                        "They are never uploaded to a CityWalls server, because there is no CityWalls server."
                )
            }
        }
        item {
            LegalCard(title = "NO ACCOUNTS, NO TRACKING") {
                LegalBody(
                    "CityWalls has no sign-in, no advertising SDK, no analytics SDK and no crash reporting SDK. " +
                        "Nothing you tap inside the app is profiled."
                )
            }
        }
        item {
            LegalCard(title = "NETWORK REQUESTS") {
                LegalBody(
                    "To show wallpapers the app requests image searches and image files from commons.wikimedia.org " +
                        "and upload.wikimedia.org. Those requests include a descriptive app User-Agent, as Wikimedia's " +
                        "API policy requires, and are subject to the Wikimedia Foundation privacy policy."
                )
            }
        }
        item {
            LegalCard(title = "PERMISSIONS") {
                LegalBody(
                    "Internet and network state are used for loading wallpapers. Setting a wallpaper uses the system " +
                        "wallpaper service. Storage permission is requested only on Android 9 and older, and only at the " +
                        "moment you download an image to the gallery."
                )
            }
        }
        item {
            LegalCard(title = "REMOVING YOUR DATA") {
                LegalBody(
                    "Deleting a download removes the file and its record. Clearing app storage in Android settings " +
                        "removes favorites and settings completely. Cache can be cleared inside Settings at any time."
                )
            }
        }
    }
}
