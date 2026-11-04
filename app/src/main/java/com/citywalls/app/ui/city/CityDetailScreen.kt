package com.citywalls.app.ui.city

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.citywalls.app.domain.Catalog
import com.citywalls.app.domain.CatalogEntry
import com.citywalls.app.ui.common.AppVmFactory
import com.citywalls.app.ui.components.EmptyState
import com.citywalls.app.ui.components.ErrorState
import com.citywalls.app.ui.components.GlassChip
import com.citywalls.app.ui.components.LoadingFooter
import com.citywalls.app.ui.components.LoadingGrid
import com.citywalls.app.ui.components.WallpaperCard
import com.citywalls.app.ui.theme.Champagne
import com.citywalls.app.ui.theme.TextPrimary
import com.citywalls.app.ui.theme.TextSecondary
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun CityDetailScreen(
    cityKey: String,
    onBack: () -> Unit,
    onOpenWallpaper: (String) -> Unit,
    onOpenCity: (String) -> Unit,
    viewModel: CityDetailViewModel = viewModel(
        factory = AppVmFactory { container ->
            CityDetailViewModel(container.wallpaperRepository, cityKey)
        }
    )
) {
    val entry = remember(cityKey) { Catalog.find(cityKey) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val feed by viewModel.paginator.state.collectAsStateWithLifecycle()

    if (entry == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            BackRow(onBack = onBack)
            ErrorState(
                message = "This collection does not exist in the CityWalls catalog.",
                onRetry = onBack
            )
        }
        return
    }

    val gridState = rememberLazyGridState()
    LaunchedEffect(gridState, feed.items.size, feed.hasMore) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .filter { last ->
                feed.hasMore && feed.items.isNotEmpty() && last >= feed.items.size - 2
            }
            .collect { viewModel.paginator.loadMore() }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 30.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            CityHeader(entry = entry, headerUrl = state.headerUrl, onBack = onBack)
        }

        if (state.related.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                RelatedRow(related = state.related, onOpenCity = onOpenCity)
            }
        }

        when {
            feed.isLoading && feed.items.isEmpty() -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LoadingGrid(rows = 2)
                }
            }

            feed.error != null && feed.items.isEmpty() -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ErrorState(
                        message = feed.error ?: "Could not load wallpapers",
                        onRetry = viewModel.paginator::retry
                    )
                }
            }

            feed.items.isEmpty() -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        title = "No licensed wallpapers for " + entry.name,
                        message = "Nothing in this collection passed the license check right now. Rather than show an unlicensed photo, CityWalls leaves the shelf empty and keeps checking.",
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            else -> {
                items(feed.items, key = { it.id }) { wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        onClick = { onOpenWallpaper(wallpaper.id) }
                    )
                }
                if (feed.isLoadingMore) {
                    item(span = { GridItemSpan(maxLineSpan) }) { LoadingFooter() }
                }
            }
        }
    }
}

@Composable
private fun BackRow(onBack: () -> Unit) {
    Row(modifier = Modifier.padding(6.dp)) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CityHeader(entry: CatalogEntry, headerUrl: String?, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        if (headerUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(headerUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = entry.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.10f), Color.Transparent)
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black
                        )
                    )
                )
        )

        Column(modifier = Modifier.align(Alignment.TopStart)) {
            BackRow(onBack = onBack)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = entry.blurb,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun RelatedRow(related: List<CatalogEntry>, onOpenCity: (String) -> Unit) {
    Column {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RELATED COLLECTIONS",
                style = MaterialTheme.typography.labelSmall,
                color = Champagne
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(related, key = { it.key }) { item ->
                GlassChip(
                    label = item.name,
                    selected = false,
                    onClick = { onOpenCity(item.key) }
                )
            }
        }
    }
}
