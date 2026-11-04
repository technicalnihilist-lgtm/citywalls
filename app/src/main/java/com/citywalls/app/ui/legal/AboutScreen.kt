package com.citywalls.app.ui.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.citywalls.app.ui.components.BackHeader
import com.citywalls.app.ui.theme.Champagne
import com.citywalls.app.ui.theme.TextPrimary
import com.citywalls.app.ui.theme.TextSecondary
import com.citywalls.app.util.ShareUtils

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            BackHeader(
                title = "CityWalls",
                subtitle = "A quiet library of cities, architecture and landmarks",
                onBack = onBack
            )
        }
        item {
            LegalCard(title = "WHAT IT IS") {
                LegalBody(
                    "CityWalls is a premium wallpaper app for city photography: twenty world cities, four architecture " +
                        "collections, twelve street-photography moods and ten landmarks. Every shelf is live, and every " +
                        "image arrives with its photographer, license and source page attached."
                )
            }
        }
        item {
            LegalCard(title = "BUILT WITH") {
                LegalBullet("Kotlin and Jetpack Compose with Material 3")
                LegalBullet("MVVM with a repository layer and manual dependency injection")
                LegalBullet("Room for favorites and download metadata")
                LegalBullet("Coil for image loading, with memory and 300 MB disk cache")
                LegalBullet("Retrofit and OkHttp for the Wikimedia Commons API")
                LegalBullet("Android WallpaperManager for home, lock and both")
            }
        }
        item {
            LegalCard(title = "DATA") {
                LegalBody(
                    "All imagery is fetched online from Wikimedia Commons. The app ships no photographs of its own, " +
                        "so what you see is always the current state of the collection."
                )
                Spacer(Modifier.height(8.dp))
                LegalLink(
                    label = "Open the Commons API documentation",
                    onClick = {
                        ShareUtils.openUrl(context, "https://commons.wikimedia.org/w/api.php")
                    }
                )
            }
        }
        item {
            LegalCard(title = "VERSION") {
                Text(
                    text = "1.0.0  ·  first release",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        item {
            LegalCard(title = "CONTACT") {
                LegalBody(
                    "Replace the contact address in AppContainer.USER_AGENT with your own before publishing, so " +
                        "Wikimedia can reach you about API usage."
                )
            }
        }
    }
}
