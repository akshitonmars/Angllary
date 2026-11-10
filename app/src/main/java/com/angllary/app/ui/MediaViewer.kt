package com.angllary.app.ui

import android.content.Intent
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.angllary.app.R
import com.angllary.app.model.MediaItem
import com.angllary.app.ui.theme.LocalExtendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MediaViewer(media: List<MediaItem>, initialIndex: Int, onDismiss: () -> Unit) {
    if (media.isEmpty()) return

    val ext = LocalExtendedColors.current
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = initialIndex.coerceIn(media.indices)) { media.size }
    val currentItem = media.getOrNull(pagerState.currentPage) ?: return
    var chromeVisible by remember { mutableStateOf(true) }
    var detailsVisible by remember { mutableStateOf(false) }

    BackHandler(onBack = onDismiss)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(ext.viewerBackground)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    chromeVisible = !chromeVisible
                    if (!chromeVisible) detailsVisible = false
                }
        ) {
            // ── Pager ──────────────────────────────────────────
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                val item = media[page]
                if (item.isVideo) {
                    VideoContent(item)
                } else {
                    ImageContent(item)
                }
            }

            // ── Top chrome ─────────────────────────────────────
            AnimatedVisibility(
                visible = chromeVisible,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Close",
                            tint = ext.viewerOnSurface
                        )
                    }
                    Column(
                        Modifier.weight(1f).padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            formatViewerDate(currentItem.dateModifiedMillis),
                            color = ext.viewerOnSurface,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            currentItem.displayName,
                            color = ext.viewerMuted,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ext.viewerSurface.copy(alpha = 0.6f)
                    ) {
                        Text(
                            "${pagerState.currentPage + 1} / ${media.size}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = ext.viewerOnSurface,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
            }

            // ── Details panel ──────────────────────────────────
            AnimatedVisibility(
                visible = detailsVisible && chromeVisible,
                enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
                        slideInVertically(spring(stiffness = Spring.StiffnessMedium)) { it / 3 },
                exit = fadeOut() + slideOutVertically { it / 3 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 90.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ext.viewerSurface.copy(alpha = 0.92f),
                    tonalElevation = 2.dp
                ) {
                    Column(
                        Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.viewer_details),
                            color = ext.viewerOnSurface,
                            style = MaterialTheme.typography.titleMedium
                        )
                        MetadataLine(stringResource(R.string.viewer_file), currentItem.displayName, ext)
                        MetadataLine(stringResource(R.string.viewer_date), formatViewerDate(currentItem.dateModifiedMillis), ext)
                        if (currentItem.width > 0 && currentItem.height > 0) {
                            MetadataLine(stringResource(R.string.viewer_resolution), "${currentItem.width} × ${currentItem.height}", ext)
                        }
                        if (currentItem.albumName.isNotBlank()) {
                            MetadataLine(stringResource(R.string.viewer_album), currentItem.albumName, ext)
                        }
                        if (currentItem.mimeType.isNotBlank()) {
                            MetadataLine(stringResource(R.string.viewer_type), currentItem.mimeType, ext)
                        }
                    }
                }
            }

            // ── Bottom chrome ──────────────────────────────────
            AnimatedVisibility(
                visible = chromeVisible,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomAction(Icons.Outlined.Share, "Share") {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = currentItem.mimeType.ifBlank {
                                if (currentItem.isVideo) "video/*" else "image/*"
                            }
                            putExtra(Intent.EXTRA_STREAM, currentItem.uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, null))
                    }
                    BottomAction(Icons.Outlined.Edit, "Edit") { /* Coming soon */ }
                    BottomAction(Icons.Outlined.Info, "Details") {
                        detailsVisible = !detailsVisible
                    }
                    BottomAction(Icons.Outlined.Delete, "Delete") { /* Coming soon */ }
                }
            }
        }
    }
}

@Composable
private fun ImageContent(item: MediaItem) {
    var scale by remember(item.id) { mutableFloatStateOf(1f) }
    var offset by remember(item.id) { mutableStateOf(Offset.Zero) }
    val transformState: TransformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset = if (scale <= 1f) Offset.Zero else offset + panChange
    }
    AsyncImage(
        model = item.uri,
        contentDescription = item.displayName,
        modifier = Modifier
            .fillMaxSize()
            .transformable(transformState)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            },
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun VideoContent(item: MediaItem) {
    var videoView by remember(item.id) { mutableStateOf<VideoView?>(null) }
    DisposableEffect(item.id) { onDispose { videoView?.stopPlayback() } }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { viewContext ->
            VideoView(viewContext).apply {
                videoView = this
                setMediaController(MediaController(viewContext).also { it.setAnchorView(this) })
                setVideoURI(item.uri)
                setOnPreparedListener { player -> player.start() }
                tag = item.uri
            }
        },
        update = { view ->
            if (view.tag != item.uri) {
                view.tag = item.uri
                view.setVideoURI(item.uri)
                view.setOnPreparedListener { player -> player.start() }
            }
        }
    )
}

@Composable
private fun BottomAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val ext = LocalExtendedColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
            Icon(icon, contentDescription = label, tint = ext.viewerOnSurface, modifier = Modifier.size(22.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = ext.viewerMuted
        )
    }
}

@Composable
private fun MetadataLine(label: String, value: String, ext: com.angllary.app.ui.theme.ExtendedColors) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, modifier = Modifier.weight(0.32f), color = ext.viewerMuted, style = MaterialTheme.typography.bodySmall)
        Text(value, modifier = Modifier.weight(0.68f), color = ext.viewerOnSurface, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

private fun formatViewerDate(timestamp: Long): String =
    SimpleDateFormat("d MMM yyyy · HH:mm", Locale.getDefault()).format(Date(timestamp))
