package com.citywalls.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.citywalls.app.ui.common.WallpaperPaginator
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Shared paginated wallpaper grid with loading, error and empty states.
 * Requests the next page when the user nears the end of the list.
 */
@Composable
fun WallpaperGrid(
    state: WallpaperPaginator.State,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState()
) {
    LaunchedEffect(gridState, state.items.size, state.hasMore) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .filter { lastVisible ->
                state.hasMore && state.items.isNotEmpty() && lastVisible >= state.items.size - 4
            }
            .collect { onLoadMore() }
    }

    val error = state.error
    when {
        state.isLoading && state.items.isEmpty() -> {
            LoadingGrid(modifier = modifier, rows = 3)
        }

        error != null && state.items.isEmpty() -> {
            ErrorState(
                message = error,
                onRetry = onRetry,
                modifier = modifier
            )
        }

        state.items.isEmpty() -> {
            EmptyState(
                title = "No licensed wallpapers here yet",
                message = "Every image must pass our license check before it appears. Nothing matched this search with a reusable license, so the shelf stays empty rather than showing unlicensed photos.",
                modifier = modifier
            )
        }

        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.id }) { wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        onClick = { onOpen(wallpaper.id) }
                    )
                }
                if (state.isLoadingMore) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LoadingFooter()
                    }
                }
            }
        }
    }
}
