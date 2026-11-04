package com.citywalls.app.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.citywalls.app.ui.common.AppVmFactory
import com.citywalls.app.ui.components.EmptyState
import com.citywalls.app.ui.components.WallpaperCard
import com.citywalls.app.ui.theme.TextPrimary
import com.citywalls.app.ui.theme.TextSecondary

@Composable
fun FavoritesScreen(
    onOpenWallpaper: (String) -> Unit,
    viewModel: FavoritesViewModel = viewModel(
        factory = AppVmFactory { container -> FavoritesViewModel(container.wallpaperRepository) }
    )
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Favorites",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (favorites.isEmpty()) {
                    "Your private shortlist lives here"
                } else {
                    favorites.size.toString() + " wallpapers kept"
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(12.dp))
        }

        if (favorites.isEmpty()) {
            EmptyState(
                title = "No favorites yet",
                message = "Tap the heart on any wallpaper and it will wait for you here, complete with its photographer and license."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites, key = { it.id }) { wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        onClick = { onOpenWallpaper(wallpaper.id) },
                        isFavorite = true,
                        onFavoriteClick = { viewModel.remove(wallpaper.id) }
                    )
                }
            }
        }
    }
}
