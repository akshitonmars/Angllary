package com.angllary.app.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.angllary.app.R
import com.angllary.app.data.MediaStoreRepository
import com.angllary.app.model.MediaItem
import com.angllary.app.ui.components.AngllaryBottomBar
import com.angllary.app.ui.components.AngllaryTopBar
import com.angllary.app.ui.components.MainTab
import com.angllary.app.ui.screens.AlbumsScreen
import com.angllary.app.ui.screens.GalleryScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class SortMode { NEWEST, OLDEST }
private enum class GridMode(val minCellSize: androidx.compose.ui.unit.Dp) {
    DAY(112.dp), MONTH(148.dp), YEAR(188.dp)
}

@Composable
fun AngllaryApp(repository: MediaStoreRepository) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasMediaAccess(context)) }
    var isLoading by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }
    var media by remember { mutableStateOf(emptyList<MediaItem>()) }
    var selectedTab by remember { mutableStateOf(MainTab.ALL) }
    var dateLabel by remember { mutableStateOf(context.getString(R.string.today)) }
    var sortMode by remember { mutableStateOf(SortMode.NEWEST) }
    var gridMode by remember { mutableStateOf(GridMode.DAY) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showGridDialog by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasPermission = hasMediaAccess(context)
        reloadKey++
    }

    LaunchedEffect(hasPermission, reloadKey) {
        if (!hasPermission) {
            media = emptyList()
            return@LaunchedEffect
        }
        isLoading = true
        media = withContext(Dispatchers.IO) { repository.loadRecentMedia() }
        isLoading = false
    }

    val orderedMedia = remember(media, sortMode) {
        when (sortMode) {
            SortMode.NEWEST -> media.sortedByDescending { it.dateModifiedMillis }
            SortMode.OLDEST -> media.sortedBy { it.dateModifiedMillis }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding()
        ) {
            AngllaryTopBar(
                dateLabel = dateLabel,
                onSort = { showSortDialog = true },
                onGrid = { showGridDialog = true },
                onLockedFolder = { toastFeature(context) },
                onTrash = { toastFeature(context) },
                onSettings = { toastFeature(context) }
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                    MainTab.ALL -> GalleryScreen(
                        media = orderedMedia,
                        hasPermission = hasPermission,
                        isLoading = isLoading,
                        minCellSize = gridMode.minCellSize,
                        onRequestPermission = { permissionLauncher.launch(mediaPermissions()) },
                        onDateChanged = { dateLabel = formatDateLabel(it.dateModifiedMillis, context) },
                        onMediaClick = { toastFeature(context, context.getString(R.string.viewer_coming_soon)) }
                    )
                    MainTab.ALBUMS -> AlbumsScreen(
                        media = orderedMedia,
                        hasPermission = hasPermission
                    )
                }
            }

            AngllaryBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onSearch = { searchOpen = true },
                modifier = Modifier.navigationBarsPadding()
            )
        }

        if (searchOpen) {
            SearchOverlay(media = orderedMedia, onClose = { searchOpen = false })
        }
    }

    if (showSortDialog) {
        ChoiceDialog(
            title = "Sort media",
            options = listOf(
                "Newest first" to (sortMode == SortMode.NEWEST),
                "Oldest first" to (sortMode == SortMode.OLDEST)
            ),
            onSelect = { index ->
                sortMode = if (index == 0) SortMode.NEWEST else SortMode.OLDEST
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }

    if (showGridDialog) {
        ChoiceDialog(
            title = "Grid view",
            options = listOf(
                "Day" to (gridMode == GridMode.DAY),
                "Month" to (gridMode == GridMode.MONTH),
                "Year" to (gridMode == GridMode.YEAR)
            ),
            onSelect = { index ->
                gridMode = GridMode.entries[index]
                showGridDialog = false
            },
            onDismiss = { showGridDialog = false }
        )
    }
}

@Composable
private fun ChoiceDialog(
    title: String,
    options: List<Pair<String, Boolean>>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(index) }.padding(vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option.second,
                            onClick = { onSelect(index) }
                        )
                        Text(option.first, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun SearchOverlay(
    media: List<MediaItem>,
    onClose: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    BackHandler(onBack = onClose)
    val results = remember(query, media) {
        if (query.isBlank()) emptyList()
        else media.filter {
            it.displayName.contains(query, ignoreCase = true) ||
                it.albumName.contains(query, ignoreCase = true)
        }.take(30)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.38f))) {
        Surface(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
                .statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    androidx.compose.material3.Icon(Icons.Outlined.Search, contentDescription = null)
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.search_photos)) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    IconButton(onClick = onClose) {
                        androidx.compose.material3.Icon(Icons.Outlined.Close, contentDescription = "Close search")
                    }
                }
                Text(
                    stringResource(R.string.search_local_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 34.dp, bottom = 8.dp)
                )
                if (query.isNotBlank()) {
                    if (results.isEmpty()) {
                        Text(
                            stringResource(R.string.search_no_results),
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        results.forEach { item ->
                            Text(
                                text = item.displayName,
                                modifier = Modifier.fillMaxWidth().clickable { onClose() }.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun mediaPermissions(): Array<String> = when {
    Build.VERSION.SDK_INT >= 34 -> arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    )
    Build.VERSION.SDK_INT >= 33 -> arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO
    )
    else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}

private fun hasMediaAccess(context: Context): Boolean {
    fun granted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    return when {
        Build.VERSION.SDK_INT >= 34 -> granted(Manifest.permission.READ_MEDIA_IMAGES) ||
            granted(Manifest.permission.READ_MEDIA_VIDEO) ||
            granted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        Build.VERSION.SDK_INT >= 33 -> granted(Manifest.permission.READ_MEDIA_IMAGES) ||
            granted(Manifest.permission.READ_MEDIA_VIDEO)
        else -> granted(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

private fun formatDateLabel(timestamp: Long, context: Context): String {
    val now = java.util.Calendar.getInstance()
    val target = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
    val isToday = now.get(java.util.Calendar.YEAR) == target.get(java.util.Calendar.YEAR) &&
        now.get(java.util.Calendar.DAY_OF_YEAR) == target.get(java.util.Calendar.DAY_OF_YEAR)
    if (isToday) return context.getString(R.string.today)

    now.add(java.util.Calendar.DAY_OF_YEAR, -1)
    val isYesterday = now.get(java.util.Calendar.YEAR) == target.get(java.util.Calendar.YEAR) &&
        now.get(java.util.Calendar.DAY_OF_YEAR) == target.get(java.util.Calendar.DAY_OF_YEAR)
    if (isYesterday) return context.getString(R.string.yesterday)

    return SimpleDateFormat("d MMM", Locale.ENGLISH).format(Date(timestamp))
}

private fun toastFeature(context: Context, message: String? = null) {
    Toast.makeText(
        context,
        message ?: context.getString(R.string.feature_coming_soon),
        Toast.LENGTH_SHORT
    ).show()
}
