package com.citywalls.app.ui.legal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.citywalls.app.ui.components.BackHeader
import com.citywalls.app.ui.components.GlassSurface
import com.citywalls.app.ui.theme.Champagne
import com.citywalls.app.ui.theme.TextPrimary
import com.citywalls.app.ui.theme.TextSecondary
import com.citywalls.app.util.ShareUtils

@Composable
fun CreditsScreen(onBack: () -> Unit) {
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
                title = "Image credits",
                subtitle = "Where every wallpaper comes from, and under what terms",
                onBack = onBack
            )
        }

        item {
            LegalCard(title = "SOURCE") {
                LegalBody(
                    "CityWalls shows photography hosted on Wikimedia Commons. Nothing is bundled inside the app: " +
                        "each shelf is fetched live from the Commons API (commons.wikimedia.org/w/api.php) when you open it."
                )
                Spacer(Modifier.height(8.dp))
                LegalLink(
                    label = "Open Wikimedia Commons",
                    onClick = { ShareUtils.openUrl(context, "https://commons.wikimedia.org/") }
                )
            }
        }

        item {
            LegalCard(title = "LICENSE POLICY") {
                LegalBody(
                    "Every single file is checked before it is displayed. An image is shown only when its metadata " +
                        "positively identifies one of these licenses:"
                )
                Spacer(Modifier.height(8.dp))
                LegalBullet("Public domain and CC0 — no conditions")
                LegalBullet("CC BY — attribution required")
                LegalBullet("CC BY-SA — attribution and share-alike required")
                Spacer(Modifier.height(8.dp))
                LegalBody(
                    "Everything else is dropped: non-commercial (NC) and no-derivatives (ND) variants, fair use and " +
                        "other non-free files, files with additional restrictions, and files sitting in Commons maintenance " +
                        "categories such as freedom-of-panorama warnings, deletion requests or missing permission."
                )
                Spacer(Modifier.height(8.dp))
                LegalBody(
                    "An image is never assumed to be free because it appears on Commons. If a collection has no " +
                        "license-safe results, CityWalls shows an empty state instead of an unlicensed photograph."
                )
            }
        }

        item {
            LegalCard(title = "ATTRIBUTION SHOWN IN APP") {
                LegalBody(
                    "Each wallpaper detail screen lists the image title, photographer or creator, resolution, the license " +
                        "name with a link to the license deed, the original Commons source page, and any required attribution " +
                        "text supplied by the uploader."
                )
            }
        }

        item {
            LegalCard(title = "MODIFICATIONS") {
                LegalBody(
                    "CityWalls serves resized previews and crops the image to your screen when you apply it as wallpaper " +
                        "or download it. That is a modification, which CC BY and CC BY-SA both permit. If you reshare a " +
                        "share-alike image, the same license continues to apply to your copy, and the photographer must " +
                        "still be credited."
                )
            }
        }

        item {
            LegalCard(title = "ARCHITECTURAL WORKS") {
                LegalBody(
                    "Photographs of buildings can carry an extra layer of copyright in the building itself. Commons flags " +
                        "problem cases with freedom-of-panorama templates, and CityWalls excludes those files. When in doubt " +
                        "about a specific country, check the source page before commercial use."
                )
            }
        }

        item {
            LegalCard(title = "LICENSE DEEDS") {
                LegalLink("CC0 1.0 Universal", { ShareUtils.openUrl(context, "https://creativecommons.org/publicdomain/zero/1.0/") })
                LegalLink("CC BY 4.0", { ShareUtils.openUrl(context, "https://creativecommons.org/licenses/by/4.0/") })
                LegalLink("CC BY-SA 4.0", { ShareUtils.openUrl(context, "https://creativecommons.org/licenses/by-sa/4.0/") })
                LegalLink("Commons licensing overview", { ShareUtils.openUrl(context, "https://commons.wikimedia.org/wiki/Commons:Licensing") })
                LegalLink("Commons reuse guide", { ShareUtils.openUrl(context, "https://commons.wikimedia.org/wiki/Commons:Reusing_content_outside_Wikimedia") })
            }
        }

        item {
            LegalCard(title = "WHAT CITYWALLS NEVER DOES") {
                LegalBullet("No watermarks are removed from any image")
                LegalBullet("No copyrighted stock photographs are used without permission")
                LegalBullet("No images are scraped from wallpaper websites")
                LegalBullet("No image is shown when its license is unknown or unclear")
            }
        }
    }
}

@Composable
fun LegalCard(title: String, content: @Composable () -> Unit) {
    GlassSurface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Champagne
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun LegalBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
    )
}

@Composable
fun LegalBullet(text: String) {
    Text(
        text = "—  " + text,
        style = MaterialTheme.typography.bodyMedium,
        color = TextPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    )
}

@Composable
fun LegalLink(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = Champagne,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp)
    )
}
