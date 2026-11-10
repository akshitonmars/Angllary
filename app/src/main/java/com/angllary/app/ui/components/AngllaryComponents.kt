package com.angllary.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.angllary.app.R
import com.angllary.app.model.MediaItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

enum class MainTab { ALL, ALBUMS }

/* ── Top Bar ─────────────────────────────────────────────────── */

@Composable
fun AngllaryTopBar(
    dateLabel: String,
    onSort: () -> Unit,
    onGrid: () -> Unit,
    onLockedFolder: () -> Unit,
    onTrash: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App title
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            // Date chip
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        dateLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Overflow menu
            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = "Open menu",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
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
}

/* ── Bottom Bar ──────────────────────────────────────────────── */

@Composable
fun AngllaryBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search button
            Surface(
                onClick = onSearch,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(44.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = stringResource(R.string.search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Tab switcher
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavTab(
                        selected = selectedTab == MainTab.ALL,
                        icon = Icons.Outlined.Image,
                        label = stringResource(R.string.tab_all),
                        onClick = { onTabSelected(MainTab.ALL) }
                    )
                    NavTab(
                        selected = selectedTab == MainTab.ALBUMS,
                        icon = Icons.Outlined.Collections,
                        label = stringResource(R.string.tab_albums),
                        onClick = { onTabSelected(MainTab.ALBUMS) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NavTab(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tabBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tabContent"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(17.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = contentColor)
        }
    }
}

/* ── Media Grid ──────────────────────────────────────────────── */

@Composable
fun MediaGrid(
    media: List<MediaItem>,
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
        contentPadding = PaddingValues(horizontal = 3.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalItemSpacing = 2.dp
    ) {
        items(items = media, key = { it.id }) { item ->
            val ratio = if (item.width > 0 && item.height > 0)
                (item.width.toFloat() / item.height.toFloat()).coerceIn(0.6f, 1.6f)
            else 1f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onMediaClick(item) }
            ) {
                AsyncImage(
                    model = item.uri,
                    contentDescription = item.displayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Video badge
                if (item.isVideo) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(5.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Video",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
