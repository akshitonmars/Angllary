package com.angllary.app.ui

import android.content.Intent
import android.view.MotionEvent
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.media3.common.MediaItem as PlayerMediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.angllary.app.R
import com.angllary.app.model.MediaItem
import com.angllary.app.ui.theme.LocalExtendedColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

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
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = initialIndex.coerceIn(media.indices)
    ) { media.size }
    val currentItem = media.getOrNull(pagerState.currentPage) ?: return

    var chromeVisible by remember { mutableStateOf(true) }
    var detailsVisible by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var viewerVisible by remember { mutableStateOf(false) }
    var closeRequested by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewerVisible = true }

    fun closeViewer() {
        if (closeRequested) return
        closeRequested = true
        viewerVisible = false
        scope.launch {
            delay(190)
            onDismiss()
        }
    }

    BackHandler(onBack = ::closeViewer)

    Dialog(
        onDismissRequest = ::closeViewer,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = viewerVisible,
            enter = fadeIn(tween(210)) + scaleIn(initialScale = 0.94f, animationSpec = spring(dampingRatio = 0.88f, stiffness = 340f)),
            exit = fadeOut(tween(160)) + scaleOut(targetScale = 0.975f, animationSpec = spring(dampingRatio = 0.96f, stiffness = 420f)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(Modifier.fillMaxSize().background(ext.viewerBackground)) {
                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1,
                    userScrollEnabled = true
                ) { page ->
                    val item = media[page]
                    if (item.isVideo) {
                        VideoContent(
                            item = item,
                            onSwipePrevious = {
                                if (pagerState.currentPage > 0) {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                                }
                            },
                            onSwipeNext = {
                                if (pagerState.currentPage < media.lastIndex) {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                                }
                            }
                        )
                    } else {
                        ImageContent(item) {
                            chromeVisible = !chromeVisible
                            if (!chromeVisible) detailsVisible = false
                        }
                    }
                }

                AnimatedVisibility(
                    visible = chromeVisible,
                    enter = fadeIn(tween(220)) + slideInVertically(tween(260)) { -it / 4 },
                    exit = fadeOut(tween(150)) + slideOutVertically(tween(210)) { -it / 4 },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .zIndex(2f)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50.dp),
                        color = ext.viewerSurface.copy(alpha = 0.96f),
                        contentColor = ext.viewerOnSurface,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        shadowElevation = 10.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(62.dp).padding(horizontal = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = ::closeViewer, modifier = Modifier.size(48.dp)) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = stringResource(R.string.back),
                                    tint = ext.viewerOnSurface
                                )
                            }
                            Column(
                                Modifier.weight(1f).padding(horizontal = 5.dp),
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    formatViewerDate(currentItem.displayDateMillis),
                                    color = ext.viewerOnSurface,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1
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
                                shape = CircleShape,
                                color = ext.viewerBackground.copy(alpha = 0.78f)
                            ) {
                                Text(
                                    "${pagerState.currentPage + 1} / ${media.size}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    color = ext.viewerOnSurface,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Box {
                                IconButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        menuExpanded = true
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.MoreVert,
                                        contentDescription = stringResource(R.string.more_options),
                                        tint = ext.viewerOnSurface
                                    )
                                }
                                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.add_to_album)) },
                                        onClick = {
                                            menuExpanded = false
                                            onAddToAlbum(currentItem)
                                        }
                                    )
                                    if (onRemoveFromAlbum != null) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.remove_from_album)) },
                                            onClick = {
                                                menuExpanded = false
                                                onRemoveFromAlbum(currentItem)
                                            }
                                        )
                                    }
                                    if (!currentItem.isLocked) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.copy_to_locked_folder)) },
                                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                                            onClick = {
                                                menuExpanded = false
                                                onLockCopy(currentItem)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = detailsVisible && chromeVisible,
                    enter = fadeIn(tween(160)) + slideInVertically(tween(210)) { it / 4 },
                    exit = fadeOut(tween(130)) + slideOutVertically(tween(170)) { it / 4 },
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 94.dp)
                        .zIndex(1f)
                ) {
                    Surface(shape = RoundedCornerShape(20.dp), color = ext.viewerSurface.copy(alpha = 0.97f)) {
                        Column(
                            Modifier.padding(horizontal = 18.dp, vertical = 15.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                stringResource(R.string.viewer_details),
                                color = ext.viewerOnSurface,
                                style = MaterialTheme.typography.titleMedium
                            )
                            MetadataLine(stringResource(R.string.viewer_file), currentItem.displayName, ext)
                            MetadataLine(stringResource(R.string.viewer_date), formatViewerDate(currentItem.displayDateMillis), ext)
                            if (currentItem.width > 0 && currentItem.height > 0) {
                                MetadataLine(stringResource(R.string.viewer_resolution), "\${currentItem.width} × \${currentItem.height}", ext)
                            }
                            if (currentItem.sizeBytes > 0) MetadataLine(stringResource(R.string.viewer_size), formatBytes(currentItem.sizeBytes), ext)
                            if (currentItem.durationMillis > 0) MetadataLine(stringResource(R.string.viewer_duration), formatDuration(currentItem.durationMillis), ext)
                            if (currentItem.relativePath.isNotBlank()) MetadataLine(stringResource(R.string.viewer_folder), currentItem.relativePath, ext)
                            if (currentItem.mimeType.isNotBlank()) MetadataLine(stringResource(R.string.viewer_type), currentItem.mimeType, ext)
                        }
                    }
                }

                AnimatedVisibility(
                    visible = chromeVisible,
                    enter = fadeIn(tween(220)) + slideInVertically(tween(270)) { it / 3 },
                    exit = fadeOut(tween(150)) + slideOutVertically(tween(210)) { it / 3 },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                        .zIndex(2f)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50.dp),
                        color = ext.viewerSurface.copy(alpha = 0.97f),
                        contentColor = ext.viewerOnSurface,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        shadowElevation = 14.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(76.dp).padding(horizontal = 5.dp, vertical = 3.dp),
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
                            ViewerAction(
                                if (isFavorite(currentItem)) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                                stringResource(R.string.favorite),
                                active = isFavorite(currentItem)
                            ) { onToggleFavorite(currentItem) }
                            ViewerAction(Icons.Outlined.Info, stringResource(R.string.details)) {
                                detailsVisible = !detailsVisible
                            }
                            if (!currentItem.isLocked) {
                                ViewerAction(Icons.Outlined.Edit, stringResource(R.string.edit)) {
                                    if (currentItem.isVideo) onTrimVideo(currentItem) else onEditPhoto(currentItem)
                                }
                                ViewerAction(Icons.Outlined.DeleteOutline, stringResource(R.string.move_to_trash)) {
                                    onTrash(currentItem)
                                }
                            }
                        }
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
        modifier = Modifier.fillMaxSize()
            .transformable(state = transformState, canPan = { scale > 1.01f })
            .scale(scale)
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap
            ),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun VideoContent(
    item: MediaItem,
    onSwipePrevious: () -> Unit,
    onSwipeNext: () -> Unit
) {
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
                var downX = 0f
                var downY = 0f
                setOnTouchListener { target, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            downX = event.x
                            downY = event.y
                            false
                        }
                        MotionEvent.ACTION_UP -> {
                            val deltaX = event.x - downX
                            val deltaY = event.y - downY
                            val centralPlaybackArea = downY in (target.height * 0.10f)..(target.height * 0.90f)
                            val horizontalSwipe = centralPlaybackArea &&
                                abs(deltaX) > target.width * 0.14f &&
                                abs(deltaX) > abs(deltaY) * 1.25f
                            if (horizontalSwipe) {
                                if (deltaX < 0f) onSwipeNext() else onSwipePrevious()
                            }
                            horizontalSwipe
                        }
                        MotionEvent.ACTION_CANCEL -> false
                        else -> false
                    }
                }
                useController = true
                controllerAutoShow = true
                this.player = player
            }
        },
        update = { it.player = player }
    )
}

@Composable
private fun ViewerAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean = false,
    onClick: () -> Unit
) {
    val ext = LocalExtendedColors.current
    val view = LocalView.current
    val scale by animateFloatAsState(
        targetValue = if (active) 1.08f else 1f,
        animationSpec = spring(stiffness = 560f, dampingRatio = 0.62f),
        label = "viewerActionScale"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        IconButton(
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            },
            modifier = Modifier.size(44.dp).scale(scale)
        ) {
            Icon(icon, contentDescription = label, tint = ext.viewerOnSurface, modifier = Modifier.size(21.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = ext.viewerMuted)
    }
}

@Composable
private fun MetadataLine(
    label: String,
    value: String,
    ext: com.angllary.app.ui.theme.ExtendedColors
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, modifier = Modifier.weight(0.31f), color = ext.viewerMuted, style = MaterialTheme.typography.bodySmall)
        Text(value, modifier = Modifier.weight(0.69f), color = ext.viewerOnSurface, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

private fun formatViewerDate(timestamp: Long): String =
    SimpleDateFormat("d MMM yyyy · HH:mm", Locale.getDefault()).format(Date(timestamp))

private fun formatBytes(bytes: Long): String =
    if (bytes < 1024L * 1024L) "\${bytes / 1024L} KB"
    else String.format(Locale.getDefault(), "%.1f MB", bytes / (1024f * 1024f))

private fun formatDuration(ms: Long): String {
    val total = ms / 1000L
    return if (total >= 3600L) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", total / 3600L, (total % 3600L) / 60L, total % 60L)
    } else {
        String.format(Locale.getDefault(), "%d:%02d", total / 60L, total % 60L)
    }
}
