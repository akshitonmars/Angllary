package com.angllary.app.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem as PlayerMediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.angllary.app.R
import com.angllary.app.model.MediaItem
import com.angllary.app.ui.theme.LocalExtendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MediaViewer(
    media: List<MediaItem>,
    initialIndex: Int,
    isFavorite: (MediaItem) -> Boolean,
    onToggleFavorite: (MediaItem) -> Unit,
    onTrash: (MediaItem) -> Unit,
    onEditPhoto: (MediaItem) -> Unit,
    onTrimVideo: (MediaItem) -> Unit,
    onLockCopy: (MediaItem) -> Unit,
    onAddToAlbum: (MediaItem) -> Unit,
    onRemoveFromAlbum: ((MediaItem) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    if (media.isEmpty()) return
    val ext = LocalExtendedColors.current
    val context = LocalContext.current
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(initialPage = initialIndex.coerceIn(media.indices)) { media.size }
    val currentItem = media.getOrNull(pagerState.currentPage) ?: return
    var chromeVisible by remember { mutableStateOf(true) }
    var detailsVisible by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    BackHandler(onBack = onDismiss)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(ext.viewerBackground)) {
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                val item = media[page]
                if (item.isVideo) VideoContent(item) else ImageContent(item) {
                    chromeVisible = !chromeVisible
                    if (!chromeVisible) detailsVisible = false
                }
            }

            AnimatedVisibility(
                visible = chromeVisible,
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 },
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.72f), Color.Black.copy(alpha = 0.24f), Color.Transparent)))
                        .statusBarsPadding().padding(horizontal = 8.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back), tint = ext.viewerOnSurface)
                    }
                    Column(Modifier.weight(1f).padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(formatViewerDate(currentItem.displayDateMillis), color = ext.viewerOnSurface, style = MaterialTheme.typography.titleSmall)
                        Text(currentItem.displayName, color = ext.viewerMuted, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Surface(shape = CircleShape, color = ext.viewerSurface.copy(alpha = 0.72f)) {
                        Text(
                            "${pagerState.currentPage + 1} / ${media.size}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = ext.viewerOnSurface,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.more_options), tint = ext.viewerOnSurface)
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.add_to_album)) },
                                onClick = { menuExpanded = false; onAddToAlbum(currentItem) }
                            )
                            if (onRemoveFromAlbum != null) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.remove_from_album)) },
                                    onClick = { menuExpanded = false; onRemoveFromAlbum(currentItem) }
                                )
                            }
                            if (!currentItem.isLocked) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.copy_to_locked_folder)) },
                                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                                    onClick = { menuExpanded = false; onLockCopy(currentItem) }
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = detailsVisible && chromeVisible,
                enter = fadeIn() + slideInVertically { it / 3 },
                exit = fadeOut() + slideOutVertically { it / 3 },
                modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 14.dp).padding(bottom = 92.dp)
            ) {
                Surface(shape = RoundedCornerShape(22.dp), color = ext.viewerSurface.copy(alpha = 0.96f)) {
                    Column(Modifier.padding(horizontal = 18.dp, vertical = 15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(stringResource(R.string.viewer_details), color = ext.viewerOnSurface, style = MaterialTheme.typography.titleMedium)
                        MetadataLine(stringResource(R.string.viewer_file), currentItem.displayName, ext)
                        MetadataLine(stringResource(R.string.viewer_date), formatViewerDate(currentItem.displayDateMillis), ext)
                        if (currentItem.width > 0 && currentItem.height > 0) MetadataLine(stringResource(R.string.viewer_resolution), "${currentItem.width} × ${currentItem.height}", ext)
                        if (currentItem.sizeBytes > 0) MetadataLine(stringResource(R.string.viewer_size), formatBytes(currentItem.sizeBytes), ext)
                        if (currentItem.durationMillis > 0) MetadataLine(stringResource(R.string.viewer_duration), formatDuration(currentItem.durationMillis), ext)
                        if (currentItem.relativePath.isNotBlank()) MetadataLine(stringResource(R.string.viewer_folder), currentItem.relativePath, ext)
                        if (currentItem.mimeType.isNotBlank()) MetadataLine(stringResource(R.string.viewer_type), currentItem.mimeType, ext)
                    }
                }
            }

            AnimatedVisibility(
                visible = chromeVisible,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.52f), Color.Black.copy(alpha = 0.86f))))
                        .navigationBarsPadding().padding(horizontal = 15.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ViewerAction(Icons.Outlined.Share, stringResource(R.string.share)) {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = currentItem.mimeType.ifBlank { if (currentItem.isVideo) "video/*" else "image/*" }
                            putExtra(Intent.EXTRA_STREAM, currentItem.uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, null))
                    }
                    ViewerAction(if (isFavorite(currentItem)) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, stringResource(R.string.favorite)) {
                        onToggleFavorite(currentItem)
                    }
                    ViewerAction(Icons.Outlined.Info, stringResource(R.string.details)) { detailsVisible = !detailsVisible }
                    if (!currentItem.isLocked) {
                        ViewerAction(Icons.Outlined.Edit, stringResource(R.string.edit)) {
                            if (currentItem.isVideo) onTrimVideo(currentItem) else onEditPhoto(currentItem)
                        }
                        ViewerAction(Icons.Outlined.DeleteOutline, stringResource(R.string.move_to_trash)) { onTrash(currentItem) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageContent(item: MediaItem, onTap: () -> Unit) {
    var scale by remember(item.uri) { mutableFloatStateOf(1f) }
    var offset by remember(item.uri) { mutableStateOf(Offset.Zero) }
    val transformState = androidx.compose.foundation.gestures.rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset = if (scale <= 1f) Offset.Zero else offset + panChange
    }
    AsyncImage(
        model = item.uri,
        contentDescription = item.displayName,
        modifier = Modifier.fillMaxSize().transformable(transformState).graphicsLayer {
            scaleX = scale
            scaleY = scale
            translationX = offset.x
            translationY = offset.y
        }.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onTap),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun VideoContent(item: MediaItem) {
    val context = LocalContext.current
    val player = remember(item.uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(PlayerMediaItem.fromUri(item.uri))
            prepare()
            playWhenReady = false
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                useController = true
                controllerAutoShow = true
                this.player = player
            }
        },
        update = { it.player = player }
    )
}

@Composable
private fun ViewerAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    val ext = LocalExtendedColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        IconButton(onClick = onClick, modifier = Modifier.size(42.dp)) {
            Icon(icon, contentDescription = label, tint = ext.viewerOnSurface, modifier = Modifier.size(21.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = ext.viewerMuted)
    }
}

@Composable
private fun MetadataLine(label: String, value: String, ext: com.angllary.app.ui.theme.ExtendedColors) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, modifier = Modifier.weight(0.31f), color = ext.viewerMuted, style = MaterialTheme.typography.bodySmall)
        Text(value, modifier = Modifier.weight(0.69f), color = ext.viewerOnSurface, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

private fun formatViewerDate(timestamp: Long): String =
    SimpleDateFormat("d MMM yyyy · HH:mm", Locale.getDefault()).format(Date(timestamp))

private fun formatBytes(bytes: Long): String =
    if (bytes < 1024L * 1024L) "${bytes / 1024L} KB" else String.format(Locale.getDefault(), "%.1f MB", bytes / (1024f * 1024f))

private fun formatDuration(ms: Long): String {
    val total = ms / 1000
    return String.format(Locale.getDefault(), "%d:%02d", total / 60, total % 60)
}
