package com.angllary.app.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.angllary.app.ui.components.rememberHapticClick
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem as PlayerMediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.angllary.app.R
import com.angllary.app.data.MediaExportRepository
import com.angllary.app.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

@Composable
fun PhotoEditorScreen(
    item: MediaItem,
    onClose: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = item.uri) {
        value = withContext(Dispatchers.IO) { decodeForEditing(context, item) }
    }
    var rotateQuarterTurns by remember(item.uri) { mutableIntStateOf(0) }
    var cropMode by remember(item.uri) { mutableStateOf("original") }
    var brightness by remember(item.uri) { mutableFloatStateOf(0f) }
    var contrast by remember(item.uri) { mutableFloatStateOf(1f) }
    var saturation by remember(item.uri) { mutableFloatStateOf(1f) }
    var saving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showingOriginal by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                .padding(top = 18.dp, bottom = 14.dp)
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = rememberHapticClick(onClose)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back)) }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.photo_editor), style = MaterialTheme.typography.titleLarge)
                    Text(item.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Button(
                    onClick = rememberHapticClick {
                    val source = bitmap
                    if (source != null && !saving) {
                        saving = true
                        errorMessage = null
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                runCatching {
                                    val edited = renderEditedBitmap(source, rotateQuarterTurns, cropMode, brightness, contrast, saturation)
                                    MediaExportRepository(context).saveImage(edited, item.displayName).also {
                                        if (edited !== source) edited.recycle()
                                    }
                                }
                            }
                            saving = false
                            result.onSuccess { onSaved() }.onFailure { errorMessage = it.message ?: context.getString(R.string.export_failed) }
                        }
                    }
                },
                    enabled = bitmap != null && !saving,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 15.dp, vertical = 10.dp)
                ) {
                    if (saving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    else {
                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(17.dp))
                        Text(stringResource(R.string.save_copy), modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 14.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap == null) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val previewAlpha by animateFloatAsState(
                    targetValue = if (bitmap != null) 1f else 0f,
                    animationSpec = androidx.compose.animation.core.tween(220),
                    label = "editorPreviewAlpha"
                )
                val previewScale by animateFloatAsState(
                    targetValue = if (bitmap != null) 1f else 0.985f,
                    animationSpec = androidx.compose.animation.core.spring(stiffness = 420f, dampingRatio = 0.84f),
                    label = "editorPreviewScale"
                )
                val previewRotation by animateFloatAsState(
                    targetValue = rotateQuarterTurns * 90f,
                    animationSpec = androidx.compose.animation.core.spring(stiffness = 500f, dampingRatio = 0.8f),
                    label = "editorPreviewRotation"
                )
                bitmap?.let { preview ->
                    Image(
                        bitmap = preview.asImageBitmap(),
                        contentDescription = item.displayName,
                        modifier = Modifier.fillMaxSize().padding(10.dp).graphicsLayer {
                            alpha = previewAlpha
                            scaleX = previewScale
                            scaleY = previewScale
                            rotationZ = if (showingOriginal) 0f else previewRotation
                        },
                        contentScale = ContentScale.Fit,
                        colorFilter = if (showingOriginal) null else ColorFilter.colorMatrix(photoColorMatrix(brightness, contrast, saturation))
                    )
                }
            }

            Column(
                Modifier.fillMaxWidth().weight(0.82f).verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).pointerInput(item.uri) {
                        detectTapGestures(onPress = {
                            showingOriginal = true
                            tryAwaitRelease()
                            showingOriginal = false
                        })
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (showingOriginal) "Original preview" else "Press and hold to compare with original",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Crop, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.crop_ratio), style = MaterialTheme.typography.titleSmall)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf("original" to stringResource(R.string.original), "1:1" to "1:1", "4:3" to "4:3", "16:9" to "16:9").forEach { (key, label) ->
                        FilterChip(
                            selected = cropMode == key,
                            onClick = rememberHapticClick { cropMode = key },
                            shape = RoundedCornerShape(11.dp),
                            label = { Text(label) }
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.RotateRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.rotate), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    OutlinedButton(
                        onClick = rememberHapticClick { rotateQuarterTurns = (rotateQuarterTurns + 1) % 4 },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.rotate_90))
                    }
                }
                EditorSlider(stringResource(R.string.brightness), brightness, -0.35f..0.35f, { brightness = it }, "0")
                EditorSlider(stringResource(R.string.contrast), contrast, 0.65f..1.4f, { contrast = it }, "1.0")
                EditorSlider(stringResource(R.string.saturation), saturation, 0f..2f, { saturation = it }, "1.0")
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
                Text(stringResource(R.string.editor_save_note), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun EditorSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit, format: String) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
            Text(String.format(Locale.getDefault(), if (format == "0") "%.2f" else "%.1f", value), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}

private suspend fun decodeForEditing(context: android.content.Context, item: MediaItem): Bitmap? =
    withContext(Dispatchers.IO) {
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(item.uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (bounds.outWidth / sample > 3600 || bounds.outHeight / sample > 3600) sample *= 2
            val options = BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.ARGB_8888 }
            context.contentResolver.openInputStream(item.uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        }.getOrNull()
    }

private fun photoColorMatrix(brightness: Float, contrast: Float, saturation: Float): ColorMatrix {
    val inverse = 1f - saturation
    val r = 0.213f * inverse + saturation
    val g = 0.715f * inverse + saturation
    val b = 0.072f * inverse + saturation
    val offset = brightness * 255f + 128f * (1f - contrast)
    return ColorMatrix(floatArrayOf(
        r * contrast, 0.715f * inverse * contrast, 0.072f * inverse * contrast, 0f, offset,
        0.213f * inverse * contrast, g * contrast, 0.072f * inverse * contrast, 0f, offset,
        0.213f * inverse * contrast, 0.715f * inverse * contrast, b * contrast, 0f, offset,
        0f, 0f, 0f, 1f, 0f
    ))
}

private fun renderEditedBitmap(
    source: Bitmap,
    quarterTurns: Int,
    cropMode: String,
    brightness: Float,
    contrast: Float,
    saturation: Float
): Bitmap {
    val rotation = Matrix().apply { postRotate(quarterTurns * 90f) }
    val rotated = if (quarterTurns % 4 == 0) source else Bitmap.createBitmap(source, 0, 0, source.width, source.height, rotation, true)
    val targetRatio = when (cropMode) {
        "1:1" -> 1f
        "4:3" -> 4f / 3f
        "16:9" -> 16f / 9f
        else -> rotated.width.toFloat() / rotated.height.toFloat()
    }
    val sourceRatio = rotated.width.toFloat() / rotated.height.toFloat()
    val cropWidth: Int
    val cropHeight: Int
    if (sourceRatio > targetRatio) {
        cropHeight = rotated.height
        cropWidth = (cropHeight * targetRatio).toInt().coerceAtLeast(1)
    } else {
        cropWidth = rotated.width
        cropHeight = (cropWidth / targetRatio).toInt().coerceAtLeast(1)
    }
    val left = ((rotated.width - cropWidth) / 2).coerceAtLeast(0)
    val top = ((rotated.height - cropHeight) / 2).coerceAtLeast(0)
    val cropped = Bitmap.createBitmap(rotated, left, top, cropWidth.coerceAtMost(rotated.width - left), cropHeight.coerceAtMost(rotated.height - top))
    val output = Bitmap.createBitmap(cropped.width, cropped.height, Bitmap.Config.ARGB_8888)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = android.graphics.ColorMatrixColorFilter(
            android.graphics.ColorMatrix(photoColorMatrix(brightness, contrast, saturation).values)
        )
    }
    Canvas(output).drawBitmap(cropped, 0f, 0f, paint)
    if (rotated !== source) rotated.recycle()
    if (cropped !== rotated && cropped !== source) cropped.recycle()
    return output
}

@Composable
fun VideoTrimScreen(
    item: MediaItem,
    onClose: () -> Unit,
    onExported: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var duration by remember(item.uri) { mutableStateOf(item.durationMillis) }
    var startFraction by remember(item.uri) { mutableFloatStateOf(0f) }
    var endFraction by remember(item.uri) { mutableFloatStateOf(1f) }
    var seekToMs by remember(item.uri) { mutableStateOf(0L) }
    var exporting by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(item.uri) {
        if (duration <= 0L) {
            duration = withContext(Dispatchers.IO) {
                runCatching {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(context, item.uri)
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                    } finally {
                        retriever.release()
                    }
                }.getOrDefault(0L)
            }
        }
    }

    Dialog(onDismissRequest = { if (!exporting) onClose() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(top = 18.dp, bottom = 16.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = rememberHapticClick(onClose), enabled = !exporting) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back)) }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.video_editor), style = MaterialTheme.typography.titleLarge)
                    Text(item.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            VideoPreview(item, seekToMs, Modifier.fillMaxWidth().weight(1f).padding(14.dp))
            Column(Modifier.padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.trim_video), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.video_trim_range, formatMs((duration * startFraction).toLong()), formatMs((duration * endFraction).toLong())),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(stringResource(R.string.trim_start), style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = startFraction,
                    onValueChange = {
                        startFraction = it.coerceAtMost(endFraction - 0.05f)
                        seekToMs = (duration * startFraction).toLong()
                    },
                    valueRange = 0f..0.95f,
                    enabled = duration > 0 && !exporting
                )
                Text(stringResource(R.string.trim_end), style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = endFraction,
                    onValueChange = {
                        endFraction = it.coerceAtLeast(startFraction + 0.05f)
                        seekToMs = (duration * endFraction).toLong()
                    },
                    valueRange = 0.05f..1f,
                    enabled = duration > 0 && !exporting
                )
                if (duration <= 0L) Text(stringResource(R.string.video_duration_unavailable), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                status?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Button(
                    onClick = rememberHapticClick {
                        if (duration <= 0L || exporting) return@rememberHapticClick
                        val startMs = (duration * startFraction).toLong()
                        val endMs = (duration * endFraction).toLong()
                        if (endMs <= startMs) return@rememberHapticClick
                        exporting = true
                        status = context.getString(R.string.exporting_video)
                        val output = File(context.cacheDir, "angllary_trim_${System.currentTimeMillis()}.mp4")
                        val input = PlayerMediaItem.Builder()
                            .setUri(item.uri)
                            .setClippingConfiguration(
                                PlayerMediaItem.ClippingConfiguration.Builder()
                                    .setStartPositionMs(startMs)
                                    .setEndPositionMs(endMs)
                                    .build()
                            )
                            .build()
                        val edited = EditedMediaItem.Builder(input).build()
                        val transformer = Transformer.Builder(context)
                            .addListener(object : Transformer.Listener {
                                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                                    scope.launch {
                                        val saved = withContext(Dispatchers.IO) {
                                            runCatching { MediaExportRepository(context).saveVideo(output, item.displayName) }
                                        }
                                        output.delete()
                                        exporting = false
                                        saved.onSuccess { onExported() }
                                            .onFailure { status = it.message ?: context.getString(R.string.export_failed) }
                                    }
                                }

                                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                                    output.delete()
                                    exporting = false
                                    status = exportException.message ?: context.getString(R.string.export_failed)
                                }
                            })
                            .build()
                        runCatching { transformer.start(edited, output.absolutePath) }
                            .onFailure {
                                output.delete()
                                exporting = false
                                status = it.message ?: context.getString(R.string.export_failed)
                            }
                    },
                    enabled = duration > 0 && !exporting,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    if (exporting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text(stringResource(R.string.export_trimmed_copy))
                }
                Text(stringResource(R.string.video_editor_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun VideoPreview(item: MediaItem, seekToMs: Long, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val player = remember(item.uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(PlayerMediaItem.fromUri(item.uri))
            prepare()
            playWhenReady = false
        }
    }
    LaunchedEffect(seekToMs) {
        player.seekTo(seekToMs)
    }
    androidx.compose.runtime.DisposableEffect(player) { onDispose { player.release() } }
    AndroidView(
        modifier = modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFF101010)),
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                useController = true
                this.player = player
            }
        },
        update = { it.player = player }
    )
}

private fun formatMs(ms: Long): String {
    val seconds = (ms / 1000L).coerceAtLeast(0)
    return String.format(Locale.getDefault(), "%d:%02d", seconds / 60, seconds % 60)
}
