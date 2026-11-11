package com.angllary.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.angllary.app.R
import com.angllary.app.model.MediaItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

enum class MainTab { ALL, ALBUMS }

@Composable
fun AngllaryTopBar(
    dateLabel: String,
    onSort: () -> Unit,
    onGrid: () -> Unit,
    onFavorites: () -> Unit,
    onLockedFolder: () -> Unit,
    onTrash: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(colors.background.copy(alpha = 0.97f), colors.background.copy(alpha = 0.72f), Color.Transparent)
                )
            )
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .height(48.dp)
    ) {
        Surface(
            modifier = Modifier.align(Alignment.CenterStart),
            shape = CircleShape,
            color = colors.surface
        ) {
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
            )
        }

        Surface(
            modifier = Modifier.align(Alignment.Center),
            shape = CircleShape,
            color = colors.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.Today, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Text(dateLabel, style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
            }
        }

        Box(Modifier.align(Alignment.CenterEnd)) {
            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Outlined.MoreHoriz, contentDescription = stringResource(R.string.open_menu), tint = colors.onSurface)
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.sort)) },
                    leadingIcon = { Icon(Icons.Outlined.Sort, contentDescription = null) },
                    onClick = { menuExpanded = false; onSort() }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.grid)) },
                    leadingIcon = { Icon(Icons.Outlined.GridView, contentDescription = null) },
                    onClick = { menuExpanded = false; onGrid() }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.favorites)) },
                    leadingIcon = { Icon(Icons.Outlined.Star, contentDescription = null) },
                    onClick = { menuExpanded = false; onFavorites() }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.locked_folder)) },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                    onClick = { menuExpanded = false; onLockedFolder() }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.recently_deleted)) },
                    leadingIcon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
                    onClick = { menuExpanded = false; onTrash() }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings)) },
                    leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                    onClick = { menuExpanded = false; onSettings() }
                )
            }
        }
    }
}

@Composable
fun AngllaryBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canvas = MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, canvas.copy(alpha = 0.72f), canvas.copy(alpha = 0.96f))
                )
            )
            .padding(start = 20.dp, end = 20.dp, top = 9.dp, bottom = 8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = onSearch,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(46.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.search), tint = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(Modifier.weight(1f))
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TabPill(selectedTab == MainTab.ALL, Icons.Outlined.Image, stringResource(R.string.tab_all)) {
                        onTabSelected(MainTab.ALL)
                    }
                    TabPill(selectedTab == MainTab.ALBUMS, Icons.Outlined.Collections, stringResource(R.string.tab_albums)) {
                        onTabSelected(MainTab.ALBUMS)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabPill(selected: Boolean, icon: ImageVector, label: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val background by animateColorAsState(
        if (selected) scheme.onSurface else Color.Transparent,
        animationSpec = spring(stiffness = 520f),
        label = "tabSurface"
    )
    val foreground by animateColorAsState(
        if (selected) scheme.surface else scheme.onSurfaceVariant,
        animationSpec = spring(stiffness = 520f),
        label = "tabContent"
    )
    Surface(onClick = onClick, shape = CircleShape, color = background) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = foreground, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = foreground)
        }
    }
}

@Composable
fun MediaGrid(
    media: List<MediaItem>,
    favoriteUris: Set<String>,
    minCellSize: Dp,
    state: LazyStaggeredGridState,
    onDateChanged: (MediaItem) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(media, state) {
        snapshotFlow { state.layoutInfo.visibleItemsInfo.minByOrNull { it.index }?.index }
            .distinctUntilChanged()
            .collectLatest { index -> index?.let { media.getOrNull(it)?.let(onDateChanged) } }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = minCellSize),
        state = state,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 5.dp, end = 5.dp, top = 5.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalItemSpacing = 4.dp
    ) {
        items(media, key = { it.uri.toString() }) { item ->
            val ratio = if (item.width > 0 && item.height > 0) {
                (item.width.toFloat() / item.height.toFloat()).coerceIn(0.62f, 1.62f)
            } else if (item.isVideo) 1.12f else 0.9f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onMediaClick(item) }
            ) {
                MediaThumbnail(
                    item = item,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (item.isVideo) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(7.dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.56f)
                    ) {
                        Icon(
                            Icons.Outlined.VideoLibrary,
                            contentDescription = stringResource(R.string.video),
                            tint = Color(0xFFF0EFEA),
                            modifier = Modifier.padding(5.dp).size(14.dp)
                        )
                    }
                }

                if (favoriteUris.contains(item.uri.toString())) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(7.dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.48f)
                    ) {
                        Icon(
                            Icons.Outlined.Star,
                            contentDescription = stringResource(R.string.favorited),
                            tint = Color(0xFFF0EFEA),
                            modifier = Modifier.padding(5.dp).size(13.dp)
                        )
                    }
                }
            }
        }
    }
}
