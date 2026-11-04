package com.citywalls.app.ui.explore

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.citywalls.app.domain.Catalog
import com.citywalls.app.domain.CatalogEntry
import com.citywalls.app.domain.CatalogGroup
import com.citywalls.app.domain.SearchSort
import com.citywalls.app.ui.common.AppVmFactory
import com.citywalls.app.ui.components.GlassChip
import com.citywalls.app.ui.components.GlassSurface
import com.citywalls.app.ui.components.SearchField
import com.citywalls.app.ui.components.WallpaperGrid
import com.citywalls.app.ui.theme.Champagne
import com.citywalls.app.ui.theme.TextPrimary
import com.citywalls.app.ui.theme.TextSecondary

@Composable
fun ExploreScreen(
    onOpenCity: (String) -> Unit,
    onOpenWallpaper: (String) -> Unit,
    viewModel: ExploreViewModel = viewModel(
        factory = AppVmFactory { container -> ExploreViewModel(container.wallpaperRepository) }
    )
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val feed by viewModel.paginator.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Explore",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Live from Wikimedia Commons, license-checked per image",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(14.dp))
            SearchField(query = ui.query, onQueryChange = viewModel::onQueryChange)
        }

        Spacer(Modifier.height(12.dp))
        GroupChipRow(selectedKey = ui.selectedGroupKey, onSelect = viewModel::onSelectGroup)
        Spacer(Modifier.height(10.dp))

        if (ui.showResults) {
            SortChipRow(selected = feed.sort, onSelect = viewModel::onSelectSort)
            Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "RESULTS FOR " + ui.activeQuery.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            WallpaperGrid(
                state = feed,
                onLoadMore = viewModel::loadMore,
                onRetry = viewModel::retry,
                onOpen = onOpenWallpaper,
                modifier = Modifier.weight(1f)
            )
        } else {
            CatalogEntryGrid(
                entries = Catalog.forGroup(ui.selectedGroupKey),
                onOpenCity = onOpenCity,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GroupChipRow(selectedKey: String, onSelect: (String) -> Unit) {
    val groups = listOf(
        Catalog.ALL_GROUP to "All",
        CatalogGroup.CITIES.key to CatalogGroup.CITIES.title,
        CatalogGroup.ARCHITECTURE.key to CatalogGroup.ARCHITECTURE.title,
        CatalogGroup.PHOTOGRAPHY.key to CatalogGroup.PHOTOGRAPHY.title,
        CatalogGroup.LANDMARKS.key to CatalogGroup.LANDMARKS.title
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(groups, key = { it.first }) { group ->
            GlassChip(
                label = group.second,
                selected = selectedKey == group.first,
                onClick = { onSelect(group.first) }
            )
        }
    }
}

@Composable
private fun SortChipRow(selected: SearchSort, onSelect: (SearchSort) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(SearchSort.entries.toList(), key = { it.name }) { sort ->
            GlassChip(
                label = sort.label,
                selected = selected == sort,
                onClick = { onSelect(sort) }
            )
        }
    }
}

@Composable
private fun CatalogEntryGrid(
    entries: List<CatalogEntry>,
    onOpenCity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(entries, key = { it.key }) { entry ->
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp),
                shape = RoundedCornerShape(18.dp),
                onClick = { onOpenCity(entry.key) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = entry.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = entry.blurb,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "OPEN",
                        style = MaterialTheme.typography.labelSmall,
                        color = Champagne
                    )
                }
            }
        }
    }
}
