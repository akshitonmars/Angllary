package com.angllary.app.ui

import android.content.Intent
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.angllary.app.R
import com.angllary.app.model.MediaItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MediaViewer(media: List<MediaItem>, initialIndex: Int, onDismiss: () -> Unit) {
    if (media.isEmpty()) return
    var currentIndex by remember(media, initialIndex) { mutableIntStateOf(initialIndex.coerceIn(media.indices)) }
    val item = media.getOrNull(currentIndex) ?: return
    val context = LocalContext.current
    var detailsVisible by remember(item.id) { mutableStateOf(false) }
    BackHandler(onBack = onDismiss)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color(0xFF111111))) {
            if (item.isVideo) {
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
            } else {
                var scale by remember(item.id) { mutableFloatStateOf(1f) }
                var offset by remember(item.id) { mutableStateOf(Offset.Zero) }
                val transformState: TransformableState = rememberTransformableState { zoomChange, panChange, _ ->
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
                    },
                    contentScale = ContentScale.Fit
                )
            }

            Row(
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.78f), Color.Transparent)))
                    .statusBarsPadding().padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Close viewer", tint = Color(0xFFF1F1EF))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(formatViewerDate(item.dateModifiedMillis), color = Color(0xFFF1F1EF), style = MaterialTheme.typography.titleSmall)
                    Text(item.displayName, color = Color(0xFFB5B5B5), style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
                Surface(shape = CircleShape, color = Color(0xFF303030)) {
                    Text("${currentIndex + 1} / ${media.size}", modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp), color = Color(0xFFF1F1EF), style = MaterialTheme.typography.labelMedium)
                }
            }

            if (detailsVisible) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .padding(horizontal = 14.dp).padding(bottom = 82.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFF202020)
                ) {
                    Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text(stringResource(R.string.viewer_details), color = Color(0xFFF1F1EF), style = MaterialTheme.typography.titleMedium)
                        MetadataLine(stringResource(R.string.viewer_file), item.displayName)
                        MetadataLine(stringResource(R.string.viewer_date), formatViewerDate(item.dateModifiedMillis))
                        if (item.width > 0 && item.height > 0) MetadataLine(stringResource(R.string.viewer_resolution), "${item.width} × ${item.height}")
                        if (item.albumName.isNotBlank()) MetadataLine(stringResource(R.string.viewer_album), item.albumName)
                        if (item.mimeType.isNotBlank()) MetadataLine(stringResource(R.string.viewer_type), item.mimeType)
                    }
                }
            }

            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f), Color.Black.copy(alpha = 0.92f))))
                    .navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (currentIndex > 0) currentIndex-- }, enabled = currentIndex > 0) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Previous item", tint = if (currentIndex > 0) Color(0xFFF1F1EF) else Color(0xFF666666))
                }
                IconButton(onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = item.mimeType.ifBlank { if (item.isVideo) "video/*" else "image/*" }
                        putExtra(Intent.EXTRA_STREAM, item.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, null))
                }) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color(0xFFF1F1EF))
                }
                IconButton(onClick = { detailsVisible = !detailsVisible }) {
                    Icon(Icons.Outlined.Info, contentDescription = stringResource(R.string.viewer_details), tint = Color(0xFFF1F1EF))
                }
                IconButton(onClick = { if (currentIndex < media.lastIndex) currentIndex++ }, enabled = currentIndex < media.lastIndex) {
                    Icon(Icons.Outlined.ArrowForward, contentDescription = "Next item", tint = if (currentIndex < media.lastIndex) Color(0xFFF1F1EF) else Color(0xFF666666))
                }
            }
        }
    }
}

@Composable
private fun MetadataLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, modifier = Modifier.weight(0.36f), color = Color(0xFFAAAAAA), style = MaterialTheme.typography.bodySmall)
        Text(value, modifier = Modifier.weight(0.64f), color = Color(0xFFF1F1EF), style = MaterialTheme.typography.bodySmall)
    }
}

private fun formatViewerDate(timestamp: Long): String =
    SimpleDateFormat("d MMM yyyy · HH:mm", Locale.getDefault()).format(Date(timestamp))
