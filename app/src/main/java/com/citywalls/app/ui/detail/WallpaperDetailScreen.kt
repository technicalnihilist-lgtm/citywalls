package com.citywalls.app.ui.detail

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.citywalls.app.domain.WallpaperTarget
import com.citywalls.app.ui.common.AppVmFactory
import com.citywalls.app.ui.components.ErrorState
import com.citywalls.app.ui.components.GlassSurface
import com.citywalls.app.ui.components.InfoRow
import com.citywalls.app.ui.components.PillLabel
import com.citywalls.app.ui.theme.Champagne
import com.citywalls.app.ui.theme.GlassBorder
import com.citywalls.app.ui.theme.TextPrimary
import com.citywalls.app.ui.theme.TextSecondary
import com.citywalls.app.util.ShareUtils
import java.io.File

@Composable
fun WallpaperDetailScreen(
    wallpaperId: String,
    onBack: () -> Unit,
    viewModel: WallpaperDetailViewModel = viewModel(
        factory = AppVmFactory { container ->
            WallpaperDetailViewModel(
                repository = container.wallpaperRepository,
                downloader = container.downloader,
                applier = container.applier,
                settingsRepository = container.settingsRepository,
                wallpaperId = wallpaperId
            )
        }
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { viewModel.download() }

    fun startDownload() {
        val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            viewModel.download()
        }
    }

    LaunchedEffect(state.message) {
        val message = state.message
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val wallpaper = state.wallpaper

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Champagne, strokeWidth = 1.5.dp)
                }
            }

            wallpaper == null -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopBar(
                        onBack = onBack,
                        isFavorite = false,
                        onFavorite = {},
                        onShare = {}
                    )
                    ErrorState(
                        message = state.error ?: "This wallpaper is unavailable.",
                        onRetry = viewModel::load
                    )
                }
            }

            else -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(state.localPath?.let { File(it) } ?: wallpaper.previewUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = wallpaper.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.55f),
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.55f)
                                )
                            )
                        )
                )

                TopBar(
                    onBack = onBack,
                    isFavorite = state.isFavorite,
                    onFavorite = viewModel::toggleFavorite,
                    onShare = {
                        ShareUtils.shareWallpaper(context, wallpaper, viewModel.localFile())
                    }
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .heightIn(max = 470.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    GlassSurface(shape = RoundedCornerShape(26.dp), borderAlpha = 0.22f) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = wallpaper.title,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = TextPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = wallpaper.resolutionLabel + "  ·  " + wallpaper.categoryLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                PillLabel(text = wallpaper.licenseName)
                            }

                            Spacer(Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ActionButton(
                                    label = "Home",
                                    modifier = Modifier.weight(1f),
                                    enabled = !state.isBusy,
                                    onClick = { viewModel.applyWallpaper(WallpaperTarget.HOME) }
                                )
                                ActionButton(
                                    label = "Lock",
                                    modifier = Modifier.weight(1f),
                                    enabled = !state.isBusy,
                                    onClick = { viewModel.applyWallpaper(WallpaperTarget.LOCK) }
                                )
                                ActionButton(
                                    label = "Both",
                                    modifier = Modifier.weight(1f),
                                    enabled = !state.isBusy,
                                    onClick = { viewModel.applyWallpaper(WallpaperTarget.BOTH) }
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ActionButton(
                                    label = if (state.localPath != null) "Saved" else "Download",
                                    modifier = Modifier.weight(1f),
                                    enabled = !state.isBusy,
                                    onClick = { startDownload() }
                                )
                                ActionButton(
                                    label = "Share",
                                    modifier = Modifier.weight(1f),
                                    enabled = true,
                                    onClick = {
                                        ShareUtils.shareWallpaper(context, wallpaper, viewModel.localFile())
                                    }
                                )
                                ActionButton(
                                    label = if (state.isFavorite) "Saved" else "Favorite",
                                    modifier = Modifier.weight(1f),
                                    enabled = true,
                                    onClick = viewModel::toggleFavorite
                                )
                            }

                            if (state.isBusy) {
                                Spacer(Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Champagne,
                                        strokeWidth = 1.5.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = (state.busyLabel ?: "Working") + "…",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            InfoRow(label = "Photographer", value = wallpaper.artist)
                            InfoRow(
                                label = "License",
                                value = wallpaper.licenseName,
                                valueColor = Champagne,
                                onClick = { ShareUtils.openUrl(context, wallpaper.licenseUrl) }
                            )
                            InfoRow(
                                label = "Source page",
                                value = "Wikimedia Commons",
                                valueColor = Champagne,
                                onClick = { ShareUtils.openUrl(context, wallpaper.sourcePageUrl) }
                            )
                            if (wallpaper.description.isNotBlank()) {
                                InfoRow(label = "About", value = wallpaper.description)
                            }
                            InfoRow(label = "Modification", value = wallpaper.modificationNote)
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "Attribution: " + wallpaper.attribution,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Cropped to your screen when applied. Share-alike licences stay in force for shared copies.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

@Composable
private fun TopBar(
    onBack: () -> Unit,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.38f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = onShare,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.38f))
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share",
                tint = TextPrimary,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onFavorite,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.38f))
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) Champagne else TextPrimary,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(14.dp),
        borderAlpha = 0.18f,
        onClick = if (enabled) onClick else null
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) TextPrimary else GlassBorder
            )
        }
    }
}
