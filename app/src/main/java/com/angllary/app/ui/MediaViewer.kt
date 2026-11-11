package com.angllary.app.ui

import android.app.Activity
import android.content.Intent
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.LayoutInflater
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Replay5
import androidx.compose.material.icons.outlined.Forward10
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
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

/**
 * Full-window viewer. It is deliberately rendered in the activity's Compose
 * hierarchy rather than a Dialog window: this keeps the black canvas behind
 * the status/navigation bars and prevents the gallery underneath from peeking
 * through at the bottom edge on devices with edge-to-edge layouts.
 */
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
    val activity = context as? Activity
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(media.indices)
    ) { media.size }
    val currentItem = media.getOrNull(pagerState.currentPage) ?: return

    var detailsVisible by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var viewerVisible by remember { mutableStateOf(false) }
    var closeRequested by remember { mutableStateOf(false) }
    var imageZoomed by remember { mutableStateOf(false) }
    var chromeVisible by remember { mutableStateOf(true) }

    val toggleChrome = {
        if (detailsVisible) {
            detailsVisible = false
        } else {
            chromeVisible = !chromeVisible
        }
    }

    LaunchedEffect(Unit) { viewerVisible = true }
    LaunchedEffect(pagerState.currentPage) {
        imageZoomed = false
        detailsVisible = false
        menuExpanded = false
    }

    DisposableEffect(activity) {
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        val oldLightStatus = controller?.isAppearanceLightStatusBars ?: false
        val oldLightNavigation = controller?.isAppearanceLightNavigationBars ?: false
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            controller?.isAppearanceLightStatusBars = oldLightStatus
            controller?.isAppearanceLightNavigationBars = oldLightNavigation
        }
    }

    fun closeViewer() {
        if (closeRequested) return
        closeRequested = true
        viewerVisible = false
        scope.launch {
            delay(170)
            onDismiss()
        }
    }

    BackHandler(onBack = { if (detailsVisible) detailsVisible = false else closeViewer() })

    AnimatedVisibility(
        visible = viewerVisible,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(130)),
        modifier = Modifier
            .fillMaxSize()
            .zIndex(30f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ext.viewerBackground)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
                userScrollEnabled = !imageZoomed
            ) { page ->
                val item = media[page]
                val isActive = page == pagerState.currentPage
                if (item.isVideo) {
                    VideoContent(
                        item = item,
                        isActive = isActive,
                        onTap = toggleChrome,
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
                    ImageContent(
                        item = item,
                        onZoomChanged = { imageZoomed = it },
                        onTap = toggleChrome
                    )
                }
            }

            // Top information pill with animated show/hide on tap
            AnimatedVisibility(
                visible = chromeVisible,
                enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { -it },
                exit = fadeOut(tween(140)) + slideOutVertically(tween(180)) { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .zIndex(3f)
            ) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = ext.viewerSurface,
                    contentColor = ext.viewerOnSurface,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    shadowElevation = 12.dp
                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .padding(horizontal = 5.dp),
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
                        modifier = Modifier.weight(1f).padding(start = 4.dp, end = 8.dp),
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
                    Box {
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                menuExpanded = true
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Outlined.MoreHoriz,
                                contentDescription = stringResource(R.string.more_options),
                                tint = ext.viewerOnSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
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
                visible = detailsVisible,
                enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 12 },
                exit = fadeOut(tween(140)) + slideOutVertically(tween(180)) { it / 14 },
                modifier = Modifier.fillMaxSize().zIndex(8f)
            ) {
                Box(Modifier.fillMaxSize().background(ext.viewerBackground)) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50.dp),
                            color = ext.viewerSurface,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                            shadowElevation = 10.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(58.dp).padding(horizontal = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { detailsVisible = false }, modifier = Modifier.size(48.dp)) {
                                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back), tint = ext.viewerOnSurface)
                                }
                                Column(Modifier.weight(1f).padding(start = 5.dp)) {
                                    Text(stringResource(R.string.viewer_details), color = ext.viewerOnSurface, style = MaterialTheme.typography.titleMedium)
                                    Text(currentItem.displayName, color = ext.viewerMuted, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                currentItem.displayName,
                                color = ext.viewerOnSurface,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                color = ext.viewerSurface,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    MetadataLine(stringResource(R.string.viewer_date), formatViewerDate(currentItem.displayDateMillis), ext)
                                    if (currentItem.width > 0 && currentItem.height > 0) {
                                        MetadataLine(stringResource(R.string.viewer_resolution), "${currentItem.width} × ${currentItem.height}", ext)
                                    }
                                    if (currentItem.sizeBytes > 0) MetadataLine(stringResource(R.string.viewer_size), formatBytes(currentItem.sizeBytes), ext)
                                    if (currentItem.durationMillis > 0) MetadataLine(stringResource(R.string.viewer_duration), formatDuration(currentItem.durationMillis), ext)
                                    if (currentItem.relativePath.isNotBlank()) MetadataLine(stringResource(R.string.viewer_folder), currentItem.relativePath, ext)
                                    if (currentItem.mimeType.isNotBlank()) MetadataLine(stringResource(R.string.viewer_type), currentItem.mimeType, ext)
                                    MetadataLine(stringResource(R.string.viewer_file), currentItem.displayName, ext)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom actions pill with animated show/hide on tap
            AnimatedVisibility(
                visible = chromeVisible,
                enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it },
                exit = fadeOut(tween(140)) + slideOutVertically(tween(180)) { it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 12.dp)
                    .fillMaxWidth()
                    .zIndex(3f)
            ) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = ext.viewerSurface,
                    contentColor = ext.viewerOnSurface,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    shadowElevation = 16.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(78.dp)
                            .padding(horizontal = 4.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ViewerAction(Icons.Outlined.Share, stringResource(R.string.share)) {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = currentItem.mimeType.ifBlank {
                                    if (currentItem.isVideo) "video/*" else "image/*"
                                }
                                putExtra(Intent.EXTRA_STREAM, currentItem.uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, null))
                        }
                        ViewerAction(
                            if (isFavorite(currentItem)) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                            stringResource(R.string.favorite),
                            active = isFavorite(currentItem)
                        ) {
                            onToggleFavorite(currentItem)
                        }
                        ViewerAction(Icons.Outlined.Info, stringResource(R.string.details)) {
                            detailsVisible = !detailsVisible
                        }
                        if (!currentItem.isLocked) {
                            ViewerAction(Icons.Outlined.Edit, stringResource(R.string.edit)) {
                                if (currentItem.isVideo) onTrimVideo(currentItem) else onEditPhoto(currentItem)
                            }
                            ViewerAction(
                                Icons.Outlined.DeleteOutline,
                                stringResource(R.string.move_to_trash),
                                emphasized = true
                            ) {
                                onTrash(currentItem)
                            }
                        }
                    }
                }
            }

            if (imageZoomed) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 86.dp)
                        .zIndex(4f),
                    shape = CircleShape,
                    color = ext.viewerSurface.copy(alpha = 0.92f)
                ) {
                    Text(
                        "Zoomed · swipe to pan",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = ext.viewerMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageContent(
    item: MediaItem,
    onZoomChanged: (Boolean) -> Unit,
    onTap: () -> Unit
) {
    var scale by remember(item.uri) { mutableFloatStateOf(1f) }
    var offset by remember(item.uri) { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val nextScale = (scale * zoomChange).coerceIn(1f, 5f)
        scale = nextScale
        if (nextScale <= 1.01f) {
            offset = Offset.Zero
            onZoomChanged(false)
        } else {
            val maxOffsetX = 1200f * (nextScale - 1f)
            val maxOffsetY = 1600f * (nextScale - 1f)
            offset = Offset(
                x = (offset.x + panChange.x).coerceIn(-maxOffsetX, maxOffsetX),
                y = (offset.y + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
            )
            onZoomChanged(true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(item.uri) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = {
                        if (scale > 1.05f) {
                            scale = 1f
                            offset = Offset.Zero
                            onZoomChanged(false)
                        } else {
                            scale = 2.5f
                            onZoomChanged(true)
                        }
                    }
                )
            }
            .transformable(
                state = transformState,
                canPan = { scale > 1.01f }
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.displayName,
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .offset {
                    IntOffset(offset.x.roundToInt(), offset.y.roundToInt())
                },
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
    }
}

@Composable
private fun VideoContent(
    item: MediaItem,
    isActive: Boolean,
    onTap: () -> Unit,
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
    var isPlaying by remember(item.uri) { mutableStateOf(false) }
    var position by remember(item.uri) { mutableLongStateOf(0L) }
    var duration by remember(item.uri) { mutableLongStateOf(item.durationMillis) }

    LaunchedEffect(player, isActive) {
        if (!isActive) player.pause()
        while (isActive) {
            isPlaying = player.isPlaying
            position = player.currentPosition.coerceAtLeast(0L)
            duration = player.duration.takeIf { it > 0L } ?: item.durationMillis
            delay(250)
        }
        isPlaying = false
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                val playerView = LayoutInflater.from(viewContext)
                    .inflate(R.layout.viewer_player, null, false) as PlayerView
                playerView.apply {
                    var downX = 0f
                    var downY = 0f
                    var downTime = 0L
                    setOnTouchListener { target, event ->
                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN -> {
                                downX = event.x
                                downY = event.y
                                downTime = System.currentTimeMillis()
                                false
                            }
                            MotionEvent.ACTION_UP -> {
                                val deltaX = event.x - downX
                                val deltaY = event.y - downY
                                val elapsed = System.currentTimeMillis() - downTime
                                val horizontalSwipe =
                                    abs(deltaX) > target.width * 0.14f &&
                                        abs(deltaX) > abs(deltaY) * 1.3f
                                if (horizontalSwipe) {
                                    if (deltaX < 0f) onSwipeNext() else onSwipePrevious()
                                    true
                                } else if (elapsed < 300 && abs(deltaX) < 30 && abs(deltaY) < 30) {
                                    onTap()
                                    false
                                } else {
                                    false
                                }
                            }
                            else -> false
                        }
                    }
                    useController = false
                    this.player = player
                }
            },
            update = { it.player = player }
        )

        // Playback controls are Compose overlays, not Media3's bottom timeline.
        // The timeline sits above Angllary's action pill and cannot be covered by it.
        Surface(
            modifier = Modifier.align(Alignment.Center),
            shape = RoundedCornerShape(50.dp),
            color = Color.Black.copy(alpha = 0.58f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                IconButton(onClick = { player.seekTo((player.currentPosition - 5_000L).coerceAtLeast(0L)) }) {
                    Icon(Icons.Outlined.Replay5, contentDescription = "Rewind 5 seconds", tint = Color.White, modifier = Modifier.size(27.dp))
                }
                IconButton(
                    onClick = { if (player.isPlaying) player.pause() else player.play() },
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(
                        if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = if (isPlaying) "Pause video" else "Play video",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
                IconButton(onClick = { player.seekTo((player.currentPosition + 10_000L).coerceAtMost(duration.coerceAtLeast(0L))) }) {
                    Icon(Icons.Outlined.Forward10, contentDescription = "Forward 10 seconds", tint = Color.White, modifier = Modifier.size(27.dp))
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 118.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatDuration(position), color = Color.White, style = MaterialTheme.typography.labelSmall)
                Text(formatDuration(duration), color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
            Slider(
                value = if (duration > 0L) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f,
                onValueChange = { progress ->
                    if (duration > 0L) player.seekTo((progress * duration).toLong())
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
@Composable
private fun ViewerAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean = false,
    emphasized: Boolean = false,
    onClick: () -> Unit
) {
    val ext = LocalExtendedColors.current
    val view = LocalView.current
    val scale by animateFloatAsState(
        targetValue = if (active) 1.06f else 1f,
        animationSpec = spring(stiffness = 520f, dampingRatio = 0.78f),
        label = "viewerActionScale"
    )
    Column(
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = if (emphasized) Color.White else Color.Transparent,
            border = if (emphasized) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.09f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (emphasized) Color.Black else ext.viewerOnSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (emphasized) ext.viewerOnSurface else ext.viewerMuted,
            maxLines = 1
        )
    }
}

@Composable
private fun MetadataLine(
    label: String,
    value: String,
    ext: com.angllary.app.ui.theme.ExtendedColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            label,
            modifier = Modifier.weight(0.31f),
            color = ext.viewerMuted,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            value,
            modifier = Modifier.weight(0.69f),
            color = ext.viewerOnSurface,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatViewerDate(timestamp: Long): String =
    SimpleDateFormat("d MMM yyyy · HH:mm", Locale.getDefault()).format(Date(timestamp))

private fun formatBytes(bytes: Long): String =
    if (bytes < 1024L * 1024L) "${bytes / 1024L} KB"
    else String.format(Locale.getDefault(), "%.1f MB", bytes / (1024f * 1024f))

private fun formatDuration(ms: Long): String {
    val total = ms / 1000L
    return if (total >= 3600L) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", total / 3600L, (total % 3600L) / 60L, total % 60L)
    } else {
        String.format(Locale.getDefault(), "%d:%02d", total / 60L, total % 60L)
    }
}
