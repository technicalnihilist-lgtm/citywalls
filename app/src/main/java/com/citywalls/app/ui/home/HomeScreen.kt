package com.citywalls.app.ui.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.citywalls.app.domain.Catalog
import com.citywalls.app.domain.CatalogGroup
import com.citywalls.app.domain.Wallpaper
import com.citywalls.app.ui.common.AppVmFactory
import com.citywalls.app.ui.components.GlassChip
import com.citywalls.app.ui.components.GlassSurface
import com.citywalls.app.ui.components.LoadingGrid
import com.citywalls.app.ui.components.PillLabel
import com.citywalls.app.ui.components.SectionHeader
import com.citywalls.app.ui.components.ShimmerBox
import com.citywalls.app.ui.components.ErrorState
import com.citywalls.app.ui.components.WallpaperRows
import com.citywalls.app.ui.theme.Champagne
import com.citywalls.app.ui.theme.TextPrimary
import com.citywalls.app.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onOpenWallpaper: (String) -> Unit,
    onOpenCity: (String) -> Unit,
    onOpenExplore: () -> Unit,
    onOpenCredits: () -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = AppVmFactory { container -> HomeViewModel(container.wallpaperRepository) }
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            BrandBlock(
                isLoading = state.isLoading,
                onRefresh = viewModel::refresh,
                onSearchClick = onOpenExplore
            )
        }

        val error = state.error
        if (error != null && state.featured.isEmpty()) {
            item { ErrorState(message = error, onRetry = viewModel::refresh) }
        }

        if (state.isLoading && state.featured.isEmpty()) {
            item { FeaturedPlaceholder() }
            item { LoadingGrid(rows = 2) }
        } else {
            if (state.featured.isNotEmpty()) {
                item { FeaturedCarousel(items = state.featured, onOpen = onOpenWallpaper) }
            }
            if (state.trendingCities.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Trending cities",
                        subtitle = "Where the light is good right now"
                    )
                }
                item { TrendingCitiesRow(cities = state.trendingCities, onOpenCity = onOpenCity) }
            }
            if (state.popular.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Popular wallpapers",
                        subtitle = "Most requested views in the collection"
                    )
                }
                item { WallpaperRows(items = state.popular, onOpen = onOpenWallpaper) }
            }
            if (state.recent.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Recently added",
                        subtitle = "Newest uploads from Wikimedia Commons"
                    )
                }
                item { WallpaperRows(items = state.recent, onOpen = onOpenWallpaper) }
            }
        }

        item {
            SectionHeader(
                title = "Explore categories",
                subtitle = "Cities, architecture, streets and landmarks"
            )
        }
        item { CategoryChipRow(onOpenExplore = onOpenExplore) }
        item { CreditsNote(onOpenCredits = onOpenCredits) }
    }
}

@Composable
private fun BrandBlock(
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onSearchClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CityWalls",
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "CITIES  ·  ARCHITECTURE  ·  LANDMARKS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Champagne
                )
            }
            IconButton(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Refresh feed",
                    tint = if (isLoading) TextSecondary else Champagne,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            onClick = onSearchClick
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Search a city, landmark or style",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FeaturedPlaceholder() {
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(420.dp),
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun FeaturedCarousel(items: List<Wallpaper>, onOpen: (String) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 14.dp
        ) { page ->
            FeaturedCard(wallpaper = items[page], onClick = { onOpen(items[page].id) })
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(items.size) { index ->
                val selected = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = if (selected) 20.dp else 6.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(if (selected) Champagne else Color.White.copy(alpha = 0.22f))
                )
            }
        }
    }
}

@Composable
private fun FeaturedCard(wallpaper: Wallpaper, onClick: () -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.04f))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(wallpaper.previewUrl)
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
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.92f)
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
        ) {
            PillLabel(text = wallpaper.categoryLabel)
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text = wallpaper.title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = wallpaper.artist + "  ·  " + wallpaper.licenseName,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TrendingCitiesRow(cities: List<TrendingCity>, onOpenCity: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cities, key = { it.entry.key }) { city ->
            TrendingCityCard(city = city, onClick = { onOpenCity(city.entry.key) })
        }
    }
}

@Composable
private fun TrendingCityCard(city: TrendingCity, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .width(150.dp)
            .height(200.dp)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
    ) {
        val header = city.headerUrl
        if (header != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(header)
                    .crossfade(true)
                    .build(),
                contentDescription = city.entry.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.02f))
                        )
                    )
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        ) {
            Text(
                text = city.entry.name,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = city.entry.blurb,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CategoryChipRow(onOpenExplore: () -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(CatalogGroup.entries.toList(), key = { it.key }) { group ->
            GlassChip(
                label = group.title,
                selected = false,
                onClick = onOpenExplore
            )
        }
    }
}

@Composable
private fun CreditsNote(onOpenCredits: () -> Unit) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        onClick = onOpenCredits
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Every image is licensed",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "CityWalls pulls photography live from Wikimedia Commons and shows only public domain, CC0, CC BY and CC BY-SA files, each with its photographer and source page.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "VIEW IMAGE CREDITS",
                style = MaterialTheme.typography.labelLarge,
                color = Champagne
            )
        }
    }
}
