package com.citywalls.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.citywalls.app.data.prefs.DownloadQuality
import com.citywalls.app.ui.common.AppVmFactory
import com.citywalls.app.ui.components.GlassChip
import com.citywalls.app.ui.components.GlassSurface
import com.citywalls.app.ui.theme.Champagne
import com.citywalls.app.ui.theme.InkBlack
import com.citywalls.app.ui.theme.TextPrimary
import com.citywalls.app.ui.theme.TextSecondary
import com.citywalls.app.util.ImageQuality

@Composable
fun SettingsScreen(
    onOpenCredits: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenAbout: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = AppVmFactory { container ->
            SettingsViewModel(container.settingsRepository, container.diagnostics)
        }
    )
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val cacheSize by viewModel.cacheSize.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val checks by viewModel.checks.collectAsStateWithLifecycle()
    val checking by viewModel.checking.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        val current = message
        if (current != null) {
            snackbarHostState.showSnackbar(current)
            viewModel.consumeMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Appearance, quality and licensing",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            item {
                SettingsCard(title = "APPEARANCE") {
                    SwitchRow(
                        title = "Pure black canvas",
                        subtitle = "True AMOLED black instead of deep charcoal",
                        checked = settings.pureBlack,
                        onCheckedChange = viewModel::setPureBlack
                    )
                }
            }

            item {
                SettingsCard(title = "IMAGE QUALITY") {
                    Text(
                        text = "Controls the size of previews requested from Wikimedia Commons.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ImageQuality.entries.toList().forEach { quality ->
                            GlassChip(
                                label = quality.label,
                                selected = settings.imageQuality == quality,
                                onClick = { viewModel.setImageQuality(quality) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = settings.imageQuality.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Champagne
                    )
                }
            }

            item {
                SettingsCard(title = "DOWNLOAD QUALITY") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DownloadQuality.entries.toList().forEach { quality ->
                            GlassChip(
                                label = quality.label,
                                selected = settings.downloadQuality == quality,
                                onClick = { viewModel.setDownloadQuality(quality) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = settings.downloadQuality.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Downloads use whatever connection is available, mobile data included.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            item {
                SettingsCard(title = "CACHE") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Image cache",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = cacheSize + " stored on this device",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        GlassChip(
                            label = "Clear",
                            selected = true,
                            onClick = viewModel::clearCache
                        )
                    }
                }
            }

            item {
                SettingsCard(title = "CONNECTION CHECK") {
                    Text(
                        text = "Tests the two hosts CityWalls needs: the Wikimedia API and the image CDN.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    GlassChip(
                        label = if (checking) "Checking…" else "Run connection check",
                        selected = true,
                        onClick = viewModel::runConnectionCheck
                    )
                    checks?.forEach { result ->
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (result.ok) "OK" else "FAIL",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (result.ok) Champagne else Color(0xFFE09A9A)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = result.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = result.detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            item {
                SettingsCard(title = "LICENSING AND LEGAL") {
                    LinkRow(title = "Image credits and licenses", onClick = onOpenCredits)
                    LinkRow(title = "Privacy policy", onClick = onOpenPrivacy)
                    LinkRow(title = "About CityWalls", onClick = onOpenAbout)
                }
            }

            item {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    Text(
                        text = "CityWalls 1.0.0",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Photography is served live from Wikimedia Commons under public domain, CC0, CC BY and CC BY-SA licenses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
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
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = InkBlack,
                checkedTrackColor = Champagne,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = Color.White.copy(alpha = 0.12f),
                uncheckedBorderColor = Color.White.copy(alpha = 0.16f)
            )
        )
    }
}

@Composable
private fun LinkRow(title: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            borderAlpha = 0.08f,
            onClick = onClick
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "VIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = Champagne
                )
            }
        }
    }
}
