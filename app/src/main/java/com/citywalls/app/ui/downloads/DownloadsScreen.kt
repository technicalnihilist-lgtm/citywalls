package com.citywalls.app.ui.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.citywalls.app.domain.DownloadedWallpaper
import com.citywalls.app.ui.common.AppVmFactory
import com.citywalls.app.ui.components.EmptyState
import com.citywalls.app.ui.components.GlassSurface
import com.citywalls.app.ui.theme.Champagne
import com.citywalls.app.ui.theme.SurfaceElevated
import com.citywalls.app.ui.theme.TextPrimary
import com.citywalls.app.ui.theme.TextSecondary
import com.citywalls.app.util.Formatters

@Composable
fun DownloadsScreen(
    onOpenWallpaper: (String) -> Unit,
    viewModel: DownloadsViewModel = viewModel(
        factory = AppVmFactory { container ->
            DownloadsViewModel(
                repository = container.wallpaperRepository,
                downloader = container.downloader,
                applier = container.applier,
                settingsRepository = container.settingsRepository
            )
        }
    )
) {
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val busyId by viewModel.busyId.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        val current = message
        if (current != null) {
            snackbarHostState.showSnackbar(current)
            viewModel.consumeMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Downloads",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Kept on this device and ready to apply offline",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
            }

            if (downloads.isEmpty()) {
                EmptyState(
                    title = "Nothing downloaded yet",
                    message = "Download a wallpaper from its detail screen and it stays on your phone, ready to apply even without a connection."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(downloads, key = { it.wallpaper.id }) { item ->
                        DownloadRow(
                            item = item,
                            isBusy = busyId == item.wallpaper.id,
                            onOpen = { onOpenWallpaper(item.wallpaper.id) },
                            onApply = { viewModel.applyDownloaded(item) },
                            onDelete = { viewModel.delete(item) }
                        )
                    }
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
private fun DownloadRow(
    item: DownloadedWallpaper,
    isBusy: Boolean,
    onOpen: () -> Unit,
    onApply: () -> Unit,
    onDelete: () -> Unit
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.wallpaper.thumbUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 58.dp, height = 78.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceElevated)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.wallpaper.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = item.wallpaper.resolutionLabel + "  ·  " + Formatters.bytes(item.sizeBytes),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.wallpaper.licenseName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Champagne,
                    maxLines = 1
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onApply)
                        .padding(vertical = 2.dp, horizontal = 2.dp)
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = Champagne,
                            strokeWidth = 1.5.dp
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = if (isBusy) "APPLYING" else "APPLY TO BOTH",
                        style = MaterialTheme.typography.labelSmall,
                        color = Champagne
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete download",
                    tint = TextSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
