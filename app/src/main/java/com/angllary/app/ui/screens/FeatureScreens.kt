package com.angllary.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.angllary.app.R
import com.angllary.app.data.LockedEntry
import com.angllary.app.data.LockedFolderStore
import com.angllary.app.model.CustomAlbum
import com.angllary.app.model.MediaItem
import com.angllary.app.ui.components.MediaThumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class DeviceAlbumPreview(
    val id: String,
    val name: String,
    val media: List<MediaItem>
)

@Composable
fun GalleryScreen(
    media: List<MediaItem>,
    favoriteUris: Set<String>,
    hasPermission: Boolean,
    isLoading: Boolean,
    minCellSize: androidx.compose.ui.unit.Dp,
    onRequestPermission: () -> Unit,
    onDateChanged: (MediaItem) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState()
    Box(modifier.fillMaxSize()) {
        when {
            !hasPermission -> PermissionState(onRequestPermission)
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            media.isEmpty() -> EmptyState(
                title = stringResource(R.string.empty_library_title),
                body = stringResource(R.string.empty_library_body),
                icon = { Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(28.dp)) }
            )
            else -> com.angllary.app.ui.components.MediaGrid(
                media = media,
                favoriteUris = favoriteUris,
                minCellSize = minCellSize,
                state = gridState,
                onDateChanged = onDateChanged,
                onMediaClick = onMediaClick
            )
        }
    }
}

@Composable
fun AlbumsScreen(
    media: List<MediaItem>,
    customAlbums: List<CustomAlbum>,
    hasPermission: Boolean,
    onCreateAlbum: () -> Unit,
    onOpenDeviceAlbum: (String, String) -> Unit,
    onOpenCustomAlbum: (String, String) -> Unit,
    onEditCustomAlbum: (CustomAlbum) -> Unit,
    modifier: Modifier = Modifier
) {
    val groups = remember(media) {
        media.filter { it.albumName.isNotBlank() }
            .groupBy { it.bucketId.ifBlank { it.albumName } }
            .map { (id, items) -> DeviceAlbumPreview(id, items.first().albumName.ifBlank { "Camera" }, items) }
            .sortedByDescending { it.media.size }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(stringResource(R.string.tab_albums), style = MaterialTheme.typography.headlineMedium)
                    Text(
                        stringResource(R.string.albums_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Surface(onClick = onCreateAlbum, shape = CircleShape, color = MaterialTheme.colorScheme.onSurface) {
                    Row(
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(16.dp))
                        Text(stringResource(R.string.new_album), color = MaterialTheme.colorScheme.surface, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        if (customAlbums.isNotEmpty()) {
            item {
                Text(stringResource(R.string.your_albums), style = MaterialTheme.typography.titleMedium)
            }
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().height((customAlbums.size / 2 + customAlbums.size % 2) * 216.dp),
                    userScrollEnabled = false,
                    contentPadding = PaddingValues(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    gridItems(customAlbums, key = { it.id }) { album ->
                        val albumMedia = media.filter { it.uri.toString() in album.mediaUris }
                        AlbumCard(
                            title = album.name,
                            count = albumMedia.size,
                            cover = albumMedia.firstOrNull(),
                            onClick = { onOpenCustomAlbum(album.id, album.name) },
                            trailing = {
                                var expanded by remember(album.id) { mutableStateOf(false) }
                                Box {
                                    IconButton(onClick = { expanded = true }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.album_options), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                    }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.manage_album)) },
                                            onClick = { expanded = false; onEditCustomAlbum(album) }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        item {
            Text(stringResource(R.string.device_albums), style = MaterialTheme.typography.titleMedium)
        }

        if (!hasPermission || groups.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.no_albums),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().height((groups.size / 2 + groups.size % 2) * 216.dp),
                    userScrollEnabled = false,
                    contentPadding = PaddingValues(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    gridItems(groups, key = { it.id }) { album ->
                        AlbumCard(
                            title = album.name,
                            count = album.media.size,
                            cover = album.media.firstOrNull(),
                            onClick = { onOpenDeviceAlbum(album.id, album.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumCard(
    title: String,
    count: Int,
    cover: MediaItem?,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1.08f)
                .clip(RoundedCornerShape(17.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick)
        ) {
            if (cover != null) {
                MediaThumbnail(item = cover, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(stringResource(R.string.items_count, count), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            trailing?.invoke()
        }
    }
}

@Composable
fun SearchOverlay(
    media: List<MediaItem>,
    favoriteUris: Set<String>,
    onOpen: (MediaItem) -> Unit,
    onClose: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("all") }
    val normalized = query.trim()
    val results = remember(media, favoriteUris, normalized, filter) {
        media.asSequence()
            .filter { item ->
                when (filter) {
                    "photos" -> !item.isVideo
                    "videos" -> item.isVideo
                    "favorites" -> item.uri.toString() in favoriteUris
                    else -> true
                }
            }
            .filter { item ->
                normalized.isBlank() ||
                    item.displayName.contains(normalized, true) ||
                    item.albumName.contains(normalized, true) ||
                    item.relativePath.contains(normalized, true) ||
                    item.mimeType.contains(normalized, true) ||
                    formatSearchDate(item.displayDateMillis).contains(normalized, true)
            }
            .take(300)
            .toList()
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(max = 680.dp),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.search_photos)) },
                        shape = CircleShape
                    )
                    TextButton(onClick = onClose) { Text(stringResource(R.string.close)) }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)) {
                    listOf("all" to stringResource(R.string.all), "photos" to stringResource(R.string.photos), "videos" to stringResource(R.string.videos), "favorites" to stringResource(R.string.favorites)).forEach { (key, label) ->
                        FilterChip(selected = filter == key, onClick = { filter = key }, label = { Text(label) })
                    }
                }

                if (normalized.isBlank()) {
                    Text(
                        stringResource(R.string.search_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                }

                if (results.isEmpty()) {
                    EmptyState(
                        title = stringResource(R.string.no_search_results),
                        body = stringResource(R.string.no_search_results_body),
                        icon = { Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(26.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(results, key = { it.uri.toString() }) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                    .clickable { onOpen(item) }.padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MediaThumbnail(
                                    item = item,
                                    modifier = Modifier.size(58.dp).clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(item.displayName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        listOf(item.albumName, formatSearchDate(item.displayDateMillis), if (item.isVideo) stringResource(R.string.video) else stringResource(R.string.photo)).filter { it.isNotBlank() }.joinToString(" · "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrashScreen(
    media: List<MediaItem>,
    onRestore: (MediaItem) -> Unit,
    onDeleteForever: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(stringResource(R.string.recently_deleted), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.trash_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (media.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.trash_empty_title),
                body = stringResource(R.string.trash_empty_body),
                icon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(28.dp)) },
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(media, key = { it.uri.toString() }) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface)
                            .padding(9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        MediaThumbnail(item = item, modifier = Modifier.size(62.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(item.displayName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(formatSearchDate(item.displayDateMillis), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { onRestore(item) }, modifier = Modifier.size(38.dp)) {
                                Icon(Icons.Outlined.Restore, contentDescription = stringResource(R.string.restore), tint = MaterialTheme.colorScheme.onSurface)
                            }
                            IconButton(onClick = { onDeleteForever(item) }, modifier = Modifier.size(38.dp)) {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.delete_forever), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LockedFolderScreen(
    entries: List<LockedEntry>,
    unlocked: Boolean,
    onUnlock: () -> Unit,
    onImport: () -> Unit,
    onOpen: (LockedEntry) -> Unit,
    onDelete: (LockedEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(stringResource(R.string.locked_folder), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.locked_folder_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (unlocked) {
                Surface(onClick = onImport, shape = CircleShape, color = MaterialTheme.colorScheme.onSurface) {
                    Row(Modifier.padding(horizontal = 13.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(Icons.Outlined.Add, contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(16.dp))
                        Text(stringResource(R.string.add_media), color = MaterialTheme.colorScheme.surface, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        if (!unlocked) {
            EmptyState(
                title = stringResource(R.string.locked_folder_locked),
                body = stringResource(R.string.locked_folder_auth_hint),
                icon = { Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(30.dp)) },
                action = { Button(onClick = onUnlock) { Text(stringResource(R.string.unlock)) } },
                modifier = Modifier.weight(1f)
            )
        } else if (entries.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.locked_folder_empty),
                body = stringResource(R.string.locked_folder_empty_body),
                icon = { Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(30.dp)) },
                action = { Button(onClick = onImport) { Text(stringResource(R.string.add_media)) } },
                modifier = Modifier.weight(1f)
            )
        } else {
            Text(
                stringResource(R.string.locked_folder_copy_notice),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    LockedEntryRow(entry = entry, onOpen = { onOpen(entry) }, onDelete = { onDelete(entry) })
                }
            }
        }
    }
}

@Composable
private fun LockedEntryRow(entry: LockedEntry, onOpen: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onOpen).padding(9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(64.dp).clip(RoundedCornerShape(11.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (entry.mimeType.startsWith("video/")) Icons.Outlined.VideoLibrary else Icons.Outlined.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(25.dp)
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(entry.displayName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(stringResource(R.string.encrypted_on_device), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.remove))
        }
    }
}

@Composable
fun SettingsScreen(
    themeMode: String,
    gridMode: String,
    sortMode: String,
    onThemeChange: (String) -> Unit,
    onGridChange: (String) -> Unit,
    onSortChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.settings_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SettingsGroup(title = stringResource(R.string.appearance)) {
                SettingsChoice(stringResource(R.string.system_default), themeMode == "system") { onThemeChange("system") }
                SettingsChoice(stringResource(R.string.light_theme), themeMode == "light") { onThemeChange("light") }
                SettingsChoice(stringResource(R.string.dark_theme), themeMode == "dark") { onThemeChange("dark") }
            }
        }
        item {
            SettingsGroup(title = stringResource(R.string.gallery_layout)) {
                SettingsChoice(stringResource(R.string.compact_grid), gridMode == "compact") { onGridChange("compact") }
                SettingsChoice(stringResource(R.string.comfortable_grid), gridMode == "comfortable") { onGridChange("comfortable") }
                SettingsChoice(stringResource(R.string.large_grid), gridMode == "large") { onGridChange("large") }
            }
        }
        item {
            SettingsGroup(title = stringResource(R.string.sort_order)) {
                SettingsChoice(stringResource(R.string.newest_first), sortMode == "newest") { onSortChange("newest") }
                SettingsChoice(stringResource(R.string.oldest_first), sortMode == "oldest") { onSortChange("oldest") }
            }
        }
        item {
            Text(stringResource(R.string.local_privacy_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) { content() }
        }
    }
}

@Composable
private fun SettingsChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        if (selected) Icon(Icons.Outlined.Check, contentDescription = stringResource(R.string.selected), tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SimpleAlbumScreen(
    title: String,
    media: List<MediaItem>,
    favoriteUris: Set<String>,
    minCellSize: androidx.compose.ui.unit.Dp,
    onDateChanged: (MediaItem) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
        if (media.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.album_empty_title),
                body = stringResource(R.string.album_empty_body),
                icon = { Icon(Icons.Outlined.Folder, contentDescription = null, modifier = Modifier.size(28.dp)) },
                modifier = Modifier.weight(1f)
            )
        } else {
            val state = androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState()
            com.angllary.app.ui.components.MediaGrid(
                media = media,
                favoriteUris = favoriteUris,
                minCellSize = minCellSize,
                state = state,
                onDateChanged = onDateChanged,
                onMediaClick = onMediaClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PermissionState(onClick: () -> Unit) {
    EmptyState(
        title = stringResource(R.string.library_permission_title),
        body = stringResource(R.string.library_permission_body),
        icon = { Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(30.dp)) },
        action = { Button(onClick = onClick) { Text(stringResource(R.string.allow_photo_access)) } }
    )
}

@Composable
private fun EmptyState(
    title: String,
    body: String,
    icon: @Composable () -> Unit,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxSize().padding(horizontal = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(62.dp)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CompositionLocalProviderColorlessIcon(content = icon)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(7.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (action != null) {
            Spacer(Modifier.height(18.dp))
            action()
        }
    }
}

@Composable
private fun CompositionLocalProviderColorlessIcon(content: @Composable () -> Unit) {
    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
    ) { content() }
}

private fun formatSearchDate(timestamp: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(timestamp))
