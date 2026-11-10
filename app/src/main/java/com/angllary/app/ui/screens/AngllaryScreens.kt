package com.angllary.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.angllary.app.R
import com.angllary.app.model.MediaItem
import com.angllary.app.ui.components.MediaGrid

@Composable
fun GalleryScreen(
    media: List<MediaItem>,
    hasPermission: Boolean,
    isLoading: Boolean,
    minCellSize: Dp,
    onRequestPermission: () -> Unit,
    onDateChanged: (MediaItem) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyStaggeredGridState()
    Box(modifier = modifier.fillMaxSize()) {
        when {
            !hasPermission -> PermissionState(onClick = onRequestPermission)
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
            media.isEmpty() -> EmptyLibraryState()
            else -> MediaGrid(
                media = media,
                minCellSize = minCellSize,
                state = gridState,
                onDateChanged = onDateChanged,
                onMediaClick = onMediaClick
            )
        }
    }
}

@Composable
private fun PermissionState(onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
            Icon(
                imageVector = Icons.Outlined.PhotoLibrary,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(20.dp).size(38.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.library_permission_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.library_permission_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(22.dp))
        Button(onClick = onClick, shape = CircleShape) {
            Icon(Icons.Outlined.Collections, contentDescription = null)
            Text(stringResource(R.string.allow_photo_access), modifier = Modifier.padding(start = 8.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(R.string.local_first_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyLibraryState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.PhotoLibrary,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(54.dp)
        )
        Spacer(Modifier.height(18.dp))
        Text(stringResource(R.string.empty_library_title), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(7.dp))
        Text(
            text = stringResource(R.string.empty_library_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private data class AlbumGroup(val name: String, val items: List<MediaItem>)

@Composable
fun AlbumsScreen(
    media: List<MediaItem>,
    hasPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val groups = media.filter { it.albumName.isNotBlank() }
        .groupBy { it.albumName }
        .map { (name, items) -> AlbumGroup(name, items) }
        .sortedByDescending { it.items.size }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Albums", style = MaterialTheme.typography.headlineMedium)
                Text(
                    if (hasPermission) "Your folders, together in one place."
                    else "Allow media access to discover albums on this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
                    ) {
                        Icon(
                            Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(11.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(stringResource(R.string.smart_albums), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.smart_albums_pending),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        item {
            Text(stringResource(R.string.device_albums), style = MaterialTheme.typography.titleLarge)
        }
        if (!hasPermission || groups.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.no_albums),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    groups.take(2).forEach { group ->
                        FeaturedAlbumCard(group = group, modifier = Modifier.weight(1f))
                    }
                }
            }
            items(groups.drop(2), key = { it.name }) { group ->
                CompactAlbumRow(group)
            }
        }
    }
}

@Composable
private fun FeaturedAlbumCard(group: AlbumGroup, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().height(150.dp)
        ) {
            AsyncImage(
                model = group.items.firstOrNull()?.uri,
                contentDescription = group.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(Modifier.height(9.dp))
        Text(group.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
        Text(
            stringResource(R.string.photos_count, group.items.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompactAlbumRow(group: AlbumGroup) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.height(58.dp).weight(0.22f)
        ) {
            AsyncImage(
                model = group.items.firstOrNull()?.uri,
                contentDescription = group.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Column(
            modifier = Modifier.weight(0.78f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(group.name, style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.photos_count, group.items.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Outlined.Folder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
