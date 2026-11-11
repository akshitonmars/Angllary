package com.angllary.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.angllary.app.R
import com.angllary.app.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MediaThumbnail(
    item: MediaItem,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (!item.isVideo) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.displayName,
            modifier = modifier,
            contentScale = contentScale
        )
        return
    }

    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(
        initialValue = VideoThumbnailCache.get(item.uri),
        key1 = item.uri
    ) {
        value = withContext(Dispatchers.IO) {
            VideoThumbnailCache.load(context.applicationContext, item.uri)
        }
    }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = item.displayName,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else {
            val transition = rememberInfiniteTransition(label = "videoThumbLoading")
            val alpha by transition.animateFloat(
                initialValue = 0.28f,
                targetValue = 0.62f,
                animationSpec = infiniteRepeatable(
                    animation = tween(850),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "videoThumbPulse"
            )
            Box(
                Modifier.fillMaxSize().background(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.12f)
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.PlayArrow,
                    contentDescription = stringResource(R.string.video),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

private object VideoThumbnailCache {
    // A bounded process cache keeps scrolling smooth without retaining full-size frames.
    private val cache = object : LruCache<String, Bitmap>(36 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            (value.byteCount / 1024).coerceAtLeast(1)
    }

    fun get(uri: Uri): Bitmap? = synchronized(cache) { cache.get(uri.toString()) }

    fun load(context: Context, uri: Uri): Bitmap? {
        get(uri)?.let { return it }

        val frame = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                runCatching {
                    context.contentResolver.loadThumbnail(uri, Size(512, 512), null)
                }.getOrNull()
            } else null
        }.getOrNull() ?: extractFrame(context, uri)

        if (frame != null) {
            synchronized(cache) { cache.put(uri.toString(), frame) }
        }
        return frame
    }

    private fun extractFrame(context: Context, uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val durationMs = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull() ?: 0L

            val candidates = listOf(
                (durationMs.coerceAtLeast(0L) / 4L).coerceAtLeast(250L),
                (durationMs.coerceAtLeast(0L) / 2L).coerceAtLeast(500L),
                0L
            ).distinct()

            candidates.firstNotNullOfOrNull { positionMs ->
                runCatching {
                    retriever.getFrameAtTime(
                        positionMs * 1_000L,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                }.getOrNull()
            }
        } catch (_: SecurityException) {
            null
        } catch (_: RuntimeException) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }
}
