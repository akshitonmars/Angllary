package com.angllary.app.ui.components

import android.view.HapticFeedbackConstants
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.angllary.app.R
import com.angllary.app.model.MediaItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

enum class MainTab { ALL, ALBUMS }

@Composable
fun rememberHapticClick(onClick: () -> Unit): () -> Unit {
    val view = LocalView.current
    return remember(view, onClick) {
        {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onClick()
        }
    }
}

@Composable
fun AngllaryTopBar(
    dateLabel: String,
    showDate: Boolean,
    isScrolled: Boolean,
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
    val haptic = rememberHapticClick { menuExpanded = true }
    val sortClick = rememberHapticClick(onSort)
    val gridClick = rememberHapticClick(onGrid)
    val favoritesClick = rememberHapticClick(onFavorites)
    val lockedClick = rememberHapticClick(onLockedFolder)
    val trashClick = rememberHapticClick(onTrash)
    val settingsClick = rememberHapticClick(onSettings)
    val sideMargin by animateDpAsState(if (isScrolled) 13.dp else 0.dp, spring(stiffness = 420f, dampingRatio = 0.86f), label = "headerMargin")
    val topMargin by animateDpAsState(if (isScrolled) 7.dp else 0.dp, spring(stiffness = 420f, dampingRatio = 0.86f), label = "headerTopMargin")
    val rowHeight by animateDpAsState(if (isScrolled) 58.dp else 66.dp, spring(stiffness = 420f, dampingRatio = 0.86f), label = "headerHeight")
    val topRadius by animateDpAsState(if (isScrolled) 28.dp else 0.dp, spring(stiffness = 420f, dampingRatio = 0.86f), label = "headerTopRadius")
    val bottomRadius by animateDpAsState(if (isScrolled) 28.dp else 27.dp, spring(stiffness = 420f, dampingRatio = 0.86f), label = "headerBottomRadius")
    val elevation by animateDpAsState(if (isScrolled) 13.dp else 3.dp, spring(stiffness = 420f, dampingRatio = 0.86f), label = "headerElevation")
    val menuRotation by animateFloatAsState(
        targetValue = if (menuExpanded) 90f else 0f,
        animationSpec = spring(stiffness = 520f, dampingRatio = 0.78f),
        label = "menuIconRotation"
    )

    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = sideMargin).padding(top = topMargin),
        shape = RoundedCornerShape(
            topStart = topRadius,
            topEnd = topRadius,
            bottomStart = bottomRadius,
            bottomEnd = bottomRadius
        ),
        color = colors.surface,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = if (isScrolled) 0.85f else 0.38f)),
        tonalElevation = if (isScrolled) 3.dp else 1.dp,
        shadowElevation = elevation
    ) {
        Column {
            Spacer(Modifier.windowInsetsTopHeight(androidx.compose.foundation.layout.WindowInsets.statusBars))
            Row(
                modifier = Modifier.fillMaxWidth().height(rowHeight).padding(horizontal = if (isScrolled) 14.dp else 19.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(11.dp),
                    color = Color.Black,
                    modifier = Modifier.size(35.dp)
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = stringResource(R.string.app_name),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(3.dp)
                    )
                }
                Spacer(Modifier.width(9.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                    maxLines = 1
                )

                Spacer(Modifier.weight(1f))

                if (showDate) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = colors.surfaceVariant,
                        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.55f))
                    ) {
                        Text(
                            text = dateLabel,
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onSurface,
                            maxLines = 1
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }

                Box {
                    Surface(
                        onClick = haptic,
                        shape = RoundedCornerShape(50.dp),
                        color = colors.surfaceVariant,
                        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.55f)),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.MoreHoriz,
                                contentDescription = stringResource(R.string.open_menu),
                                tint = colors.onSurface,
                                modifier = Modifier.size(23.dp).graphicsLayer { rotationZ = menuRotation }
                            )
                        }
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort)) },
                            leadingIcon = { Icon(Icons.Outlined.Sort, contentDescription = null) },
                            onClick = { menuExpanded = false; sortClick() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.grid)) },
                            leadingIcon = { Icon(Icons.Outlined.GridView, contentDescription = null) },
                            onClick = { menuExpanded = false; gridClick() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.favorites)) },
                            leadingIcon = { Icon(Icons.Outlined.Star, contentDescription = null) },
                            onClick = { menuExpanded = false; favoritesClick() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.locked_folder)) },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                            onClick = { menuExpanded = false; lockedClick() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.recently_deleted)) },
                            leadingIcon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
                            onClick = { menuExpanded = false; trashClick() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings)) },
                            leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                            onClick = { menuExpanded = false; settingsClick() }
                        )
                    }
                }
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
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(min = 284.dp, max = 332.dp),
            shape = RoundedCornerShape(50.dp),
            color = scheme.surfaceContainerHigh,
            contentColor = scheme.onSurface,
            border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.95f)),
            shadowElevation = 14.dp
        ) {
            Row(
                modifier = Modifier.height(58.dp).padding(5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                DockButton(
                    icon = Icons.Outlined.Search,
                    label = stringResource(R.string.search),
                    onClick = onSearch,
                    modifier = Modifier.size(48.dp)
                )
                Box(
                    Modifier.width(1.dp).height(24.dp)
                        .background(scheme.outlineVariant.copy(alpha = 0.75f))
                )
                DockTab(
                    selected = selectedTab == MainTab.ALL,
                    icon = Icons.Outlined.Image,
                    label = stringResource(R.string.tab_all),
                    onClick = { onTabSelected(MainTab.ALL) },
                    modifier = Modifier.weight(1f)
                )
                DockTab(
                    selected = selectedTab == MainTab.ALBUMS,
                    icon = Icons.Outlined.Collections,
                    label = stringResource(R.string.tab_albums),
                    onClick = { onTabSelected(MainTab.ALBUMS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DockButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(stiffness = 650f, dampingRatio = 0.72f),
        label = "dockButtonScale"
    )
    val click = rememberHapticClick(onClick)
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(18.dp))
            .clickable(interactionSource = source, indication = null, onClick = click),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(21.dp))
    }
}

@Composable
private fun DockTab(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = 650f, dampingRatio = 0.76f),
        label = "dockTabScale"
    )
    val background by animateColorAsState(
        targetValue = if (selected) scheme.onSurface else Color.Transparent,
        animationSpec = spring(stiffness = 520f, dampingRatio = 0.82f),
        label = "dockTabBackground"
    )
    val foreground by animateColorAsState(
        targetValue = if (selected) scheme.surface else scheme.onSurfaceVariant,
        animationSpec = spring(stiffness = 520f, dampingRatio = 0.82f),
        label = "dockTabForeground"
    )
    Box(
        modifier = modifier
            .height(48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
             .clip(RoundedCornerShape(50.dp))
            .background(background)
            .clickable(
                interactionSource = source,
                indication = null,
                onClick = rememberHapticClick(onClick)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = foreground, modifier = Modifier.size(17.dp))
            Text(label, color = foreground, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaGrid(
    media: List<MediaItem>,
    favoriteUris: Set<String>,
    minCellSize: Dp,
    state: LazyStaggeredGridState,
    onDateChanged: (MediaItem) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    onFavoriteToggle: (MediaItem) -> Unit = {},
    onSelectionActiveChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localView = LocalView.current
    var focusedItem by remember { mutableStateOf<MediaItem?>(null) }
    var focusedMenuExpanded by remember { mutableStateOf(false) }
    val blurRadius by animateDpAsState(
        targetValue = if (focusedItem != null) 17.dp else 0.dp,
        animationSpec = spring(stiffness = 360f, dampingRatio = 0.88f),
        label = "selectionBackgroundBlur"
    )
    LaunchedEffect(media, state) {
        snapshotFlow { state.layoutInfo.visibleItemsInfo.minByOrNull { it.index }?.index }
            .distinctUntilChanged()
            .collectLatest { index ->
                index?.let { media.getOrNull(it)?.let(onDateChanged) }
            }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = minCellSize),
        state = state,
        modifier = modifier.fillMaxSize().blur(blurRadius),
        contentPadding = PaddingValues(start = 5.dp, end = 5.dp, top = 4.dp, bottom = 124.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalItemSpacing = 4.dp
    ) {
        items(
            items = media,
            key = { it.uri.toString() },
            contentType = { if (it.isVideo) "video" else "photo" }
        ) { item ->
            val ratio = if (item.width > 0 && item.height > 0) {
                (item.width.toFloat() / item.height.toFloat()).coerceIn(0.58f, 1.72f)
            } else if (item.isVideo) 1.14f else 0.9f

            val source = remember(item.uri) { MutableInteractionSource() }
            val pressed by source.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (pressed) 0.975f else 1f,
                animationSpec = spring(stiffness = 680f, dampingRatio = 0.78f),
                label = "mediaTilePress"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .clip(RoundedCornerShape(13.dp))
                    .combinedClickable(
                        interactionSource = source,
                        indication = null,
                        onClick = rememberHapticClick { onMediaClick(item) },
                        onLongClick = {
                            localView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            focusedItem = item
                            focusedMenuExpanded = false
                            onSelectionActiveChange(true)
                        }
                    )
            ) {
                MediaThumbnail(
                    item = item,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

                if (item.isVideo) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomStart).padding(7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.62f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.VideoLibrary,
                                    contentDescription = stringResource(R.string.video),
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                if (item.durationMillis > 0L) {
                                    Text(
                                        formatDuration(item.durationMillis),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }

                if (favoriteUris.contains(item.uri.toString()) || favoriteUris.contains("content://media/external/file/${item.id}")) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(7.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.58f)
                    ) {
                        Icon(
                            Icons.Outlined.Star,
                            contentDescription = stringResource(R.string.favorited),
                            tint = Color.White,
                            modifier = Modifier.padding(5.dp).size(13.dp)
                        )
                    }
                }
            }
        }
    }

    focusedItem?.let { selected ->
        val ratio = if (selected.width > 0 && selected.height > 0) {
            (selected.width.toFloat() / selected.height.toFloat()).coerceIn(0.62f, 1.55f)
        } else if (selected.isVideo) 1.2f else 0.92f
        val focusScale by animateFloatAsState(
            targetValue = 1f,
            animationSpec = spring(stiffness = 300f, dampingRatio = 0.78f),
            label = "selectedMediaScale"
        )
        Box(
            modifier = Modifier.fillMaxSize().zIndex(10f)
                .background(Color.Black.copy(alpha = 0.62f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusedItem = null
                    focusedMenuExpanded = false
                    onSelectionActiveChange(false)
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.86f).graphicsLayer {
                    scaleX = focusScale
                    scaleY = focusScale
                }.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {},
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(ratio)
                        .clip(RoundedCornerShape(23.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    MediaThumbnail(
                        item = selected,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (selected.isVideo) {
                        Surface(
                            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                            shape = RoundedCornerShape(9.dp),
                            color = Color.Black.copy(alpha = 0.74f)
                        ) {
                            Text(
                                formatDuration(selected.durationMillis),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).size(40.dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.62f)
                    ) {
                        IconButton(onClick = {
                            focusedItem = null
                            focusedMenuExpanded = false
                            onSelectionActiveChange(false)
                        }) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.close), tint = Color.White)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 20.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 17.dp, end = 7.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(selected.displayName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                selected.albumName.ifBlank { selected.relativePath.ifBlank { if (selected.isVideo) "Video" else "Photo" } },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = rememberHapticClick { onMediaClick(selected); focusedItem = null; onSelectionActiveChange(false) }) {
                            Icon(Icons.Outlined.OpenInFull, contentDescription = stringResource(R.string.open))
                        }
                        Box {
                            IconButton(onClick = rememberHapticClick { focusedMenuExpanded = true }) {
                                Icon(Icons.Outlined.MoreHoriz, contentDescription = stringResource(R.string.more_options))
                            }
                            DropdownMenu(
                                expanded = focusedMenuExpanded,
                                onDismissRequest = { focusedMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.open)) },
                                    onClick = {
                                        focusedMenuExpanded = false
                                        focusedItem = null
                                        onSelectionActiveChange(false)
                                        onMediaClick(selected)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.share)) },
                                    onClick = {
                                        focusedMenuExpanded = false
                                        val send = Intent(Intent.ACTION_SEND).apply {
                                            type = selected.mimeType.ifBlank { if (selected.isVideo) "video/*" else "image/*" }
                                            putExtra(Intent.EXTRA_STREAM, selected.uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        runCatching { context.startActivity(Intent.createChooser(send, null)) }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (favoriteUris.contains(selected.uri.toString())) stringResource(R.string.remove_favorite) else stringResource(R.string.favorite)) },
                                    onClick = {
                                        focusedMenuExpanded = false
                                        onFavoriteToggle(selected)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMillis: Long): String {
    val seconds = (durationMillis / 1000L).coerceAtLeast(0L)
    return if (seconds >= 3600L) {
        "%d:%02d:%02d".format(seconds / 3600L, (seconds % 3600L) / 60L, seconds % 60L)
    } else {
        "%d:%02d".format(seconds / 60L, seconds % 60L)
    }
}
