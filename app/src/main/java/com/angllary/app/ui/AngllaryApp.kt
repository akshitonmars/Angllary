package com.angllary.app.ui

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.angllary.app.R
import com.angllary.app.data.GalleryStore
import com.angllary.app.data.LockedEntry
import com.angllary.app.data.LockedFolderStore
import com.angllary.app.data.MediaStoreRepository
import com.angllary.app.model.CustomAlbum
import com.angllary.app.model.MediaItem
import com.angllary.app.security.LockedFolderAuthenticator
import com.angllary.app.ui.components.AngllaryBottomBar
import com.angllary.app.ui.components.AngllaryTopBar
import com.angllary.app.ui.components.MainTab
import com.angllary.app.ui.screens.AlbumsScreen
import com.angllary.app.ui.screens.GalleryScreen
import com.angllary.app.ui.screens.LockedFolderScreen
import com.angllary.app.ui.screens.SearchOverlay
import com.angllary.app.ui.screens.SettingsScreen
import com.angllary.app.ui.screens.SimpleAlbumScreen
import com.angllary.app.ui.screens.TrashScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class AppPage { HOME, FAVORITES, TRASH, LOCKED, SETTINGS, ALBUM }
private data class OpenAlbum(val name: String, val deviceBucketId: String? = null, val customAlbumId: String? = null)

@Composable
fun AngllaryApp(
    repository: MediaStoreRepository,
    galleryStore: GalleryStore,
    onThemeModeChange: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lockedStore = remember { LockedFolderStore(context) }

    var hasPermission by remember { mutableStateOf(hasMediaAccess(context)) }
    var isLoading by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var storeRevision by remember { mutableIntStateOf(0) }

    var media by remember { mutableStateOf(emptyList<MediaItem>()) }
    var trash by remember { mutableStateOf(emptyList<MediaItem>()) }
    var favoriteUris by remember { mutableStateOf(galleryStore.favoriteUris()) }
    var customAlbums by remember { mutableStateOf(galleryStore.customAlbums()) }
    var lockedEntries by remember { mutableStateOf(lockedStore.entries()) }

    var selectedTab by remember { mutableStateOf(MainTab.ALL) }
    var activePage by remember { mutableStateOf(AppPage.HOME) }
    var openedAlbum by remember { mutableStateOf<OpenAlbum?>(null) }
    var dateLabel by remember { mutableStateOf(context.getString(R.string.today)) }
    var sortMode by remember { mutableStateOf(galleryStore.sortMode()) }
    var gridMode by remember { mutableStateOf(galleryStore.gridMode()) }
    var themeMode by remember { mutableStateOf(galleryStore.themeMode()) }

    var searchOpen by remember { mutableStateOf(false) }
    var viewerItems by remember { mutableStateOf(emptyList<MediaItem>()) }
    var viewerIndex by remember { mutableStateOf<Int?>(null) }
    var lockedPreviewEntry by remember { mutableStateOf<LockedEntry?>(null) }
    var editorItem by remember { mutableStateOf<MediaItem?>(null) }

    var showNewAlbum by remember { mutableStateOf(false) }
    var newAlbumName by remember { mutableStateOf("") }
    var albumTarget by remember { mutableStateOf<MediaItem?>(null) }
    var editAlbumTarget by remember { mutableStateOf<CustomAlbum?>(null) }
    var renameAlbumValue by remember { mutableStateOf("") }
    var lockedUnlocked by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasPermission = hasMediaAccess(context)
        reloadKey++
    }

    val systemMediaAction = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        reloadKey++
    }

    val lockedBackupExport = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching { lockedStore.exportBackup(uri) }
                }
                result.onSuccess { count ->
                    showMessage(context, context.getString(R.string.backup_export_success, count))
                }.onFailure {
                    showMessage(context, it.message ?: context.getString(R.string.backup_failed))
                }
            }
        }
    }

    val lockedBackupRestore = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching { lockedStore.restoreBackup(uri) }
                }
                result.onSuccess { count ->
                    storeRevision++
                    showMessage(context, context.getString(R.string.backup_restore_success, count))
                }.onFailure {
                    showMessage(context, it.message ?: context.getString(R.string.backup_failed))
                }
            }
        }
    }

    val lockedPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        val name = displayName(context, uri)
                        val mime = context.contentResolver.getType(uri).orEmpty()
                        lockedStore.importUri(uri, name, mime)
                    }
                }
                result.onSuccess {
                    storeRevision++
                    showMessage(context, context.getString(R.string.locked_import_success))
                }.onFailure {
                    showMessage(context, it.message ?: context.getString(R.string.import_failed))
                }
            }
        }
    }

    LaunchedEffect(hasPermission, reloadKey) {
        if (!hasPermission) {
            media = emptyList()
            trash = emptyList()
            return@LaunchedEffect
        }
        isLoading = true
        try {
            val loaded = withContext(Dispatchers.IO) { repository.loadMedia() }
            media = loaded.sortedByDescending { it.displayDateMillis }
            trash = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                withContext(Dispatchers.IO) { repository.loadMedia(trashed = true) }
                    .sortedByDescending { it.displayDateMillis }
            } else emptyList()
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(storeRevision) {
        favoriteUris = galleryStore.favoriteUris()
        customAlbums = galleryStore.customAlbums()
        lockedEntries = lockedStore.entries()
        sortMode = galleryStore.sortMode()
        gridMode = galleryStore.gridMode()
    }

    val orderedMedia = remember(media, sortMode) {
        when (sortMode) {
            "oldest" -> media.sortedBy { it.displayDateMillis }
            else -> media.sortedByDescending { it.displayDateMillis }
        }
    }

    val gridCellSize = when (gridMode) {
        "compact" -> 104.dp
        "large" -> 164.dp
        else -> 132.dp
    }

    val lockedFolderLabel = stringResource(R.string.locked_folder)

    val activeAlbumItems = remember(media, openedAlbum, customAlbums) {
        val album = openedAlbum ?: return@remember emptyList()
        when {
            album.customAlbumId != null -> {
                val uris = customAlbums.firstOrNull { it.id == album.customAlbumId }?.mediaUris.orEmpty()
                media.filter { it.uri.toString() in uris }
            }
            album.deviceBucketId != null -> media.filter { it.bucketId == album.deviceBucketId }
            else -> emptyList()
        }.sortedByDescending { it.displayDateMillis }
    }

    fun openViewer(item: MediaItem, items: List<MediaItem> = orderedMedia) {
        val index = items.indexOfFirst { it.uri == item.uri }
        if (index >= 0) {
            viewerItems = items
            viewerIndex = index
        }
    }

    fun updateFavorite(item: MediaItem) {
        val next = item.uri.toString() !in favoriteUris
        galleryStore.setFavorite(item.uri.toString(), next)
        favoriteUris = galleryStore.favoriteUris()
    }

    fun showSystemMediaRequest(pendingIntent: PendingIntent?) {
        if (pendingIntent == null) {
            showMessage(context, context.getString(R.string.system_trash_unavailable))
            return
        }
        runCatching {
            systemMediaAction.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
        }.onFailure { showMessage(context, it.message ?: context.getString(R.string.action_failed)) }
    }

    fun moveToTrash(item: MediaItem, shouldTrash: Boolean = true) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            showMessage(context, context.getString(R.string.system_trash_requires_android_11))
            return
        }
        runCatching {
            showSystemMediaRequest(repository.createTrashRequest(listOf(item), shouldTrash))
        }.onFailure {
            showMessage(context, it.message ?: context.getString(R.string.action_failed))
        }
    }

    fun deleteForever(item: MediaItem) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            showMessage(context, context.getString(R.string.system_trash_unavailable))
            return
        }
        runCatching {
            showSystemMediaRequest(repository.createDeleteRequest(listOf(item)))
        }.onFailure {
            showMessage(context, it.message ?: context.getString(R.string.action_failed))
        }
    }

    fun importIntoLockedFolder(item: MediaItem) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { lockedStore.importUri(item.uri, item.displayName, item.mimeType) }
            }
            result.onSuccess {
                storeRevision++
                showMessage(context, context.getString(R.string.locked_copy_success))
            }.onFailure {
                showMessage(context, it.message ?: context.getString(R.string.import_failed))
            }
        }
    }

    fun unlockFolder() {
        val activity = context as? FragmentActivity
        if (activity == null) {
            showMessage(context, context.getString(R.string.authentication_unavailable))
            return
        }
        LockedFolderAuthenticator(
            activity,
            onSuccess = {
                lockedUnlocked = true
                lockedEntries = lockedStore.entries()
            },
            onError = { message -> showMessage(context, message) }
        ).authenticate()
    }

    BackHandler(enabled = activePage != AppPage.HOME || searchOpen) {
        when {
            searchOpen -> searchOpen = false
            activePage == AppPage.ALBUM -> {
                activePage = AppPage.HOME
                openedAlbum = null
            }
            activePage == AppPage.LOCKED -> {
                lockedUnlocked = false
                activePage = AppPage.HOME
            }
            else -> activePage = AppPage.HOME
        }
    }

    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            if (activePage == AppPage.HOME) {
                AngllaryTopBar(
                    dateLabel = dateLabel,
                    onSort = {
                        sortMode = if (sortMode == "newest") "oldest" else "newest"
                        galleryStore.setSortMode(sortMode)
                    },
                    onGrid = {
                        gridMode = when (gridMode) {
                            "compact" -> "comfortable"
                            "comfortable" -> "large"
                            else -> "compact"
                        }
                        galleryStore.setGridMode(gridMode)
                    },
                    onFavorites = { activePage = AppPage.FAVORITES },
                    onLockedFolder = {
                        lockedUnlocked = false
                        activePage = AppPage.LOCKED
                    },
                    onTrash = { activePage = AppPage.TRASH },
                    onSettings = { activePage = AppPage.SETTINGS }
                )
            } else {
                SubPageHeader(
                    title = when (activePage) {
                        AppPage.FAVORITES -> stringResource(R.string.favorites)
                        AppPage.TRASH -> stringResource(R.string.recently_deleted)
                        AppPage.LOCKED -> stringResource(R.string.locked_folder)
                        AppPage.SETTINGS -> stringResource(R.string.settings)
                        AppPage.ALBUM -> openedAlbum?.name.orEmpty()
                        else -> stringResource(R.string.app_name)
                    },
                    onBack = {
                        if (activePage == AppPage.LOCKED) lockedUnlocked = false
                        activePage = AppPage.HOME
                        openedAlbum = null
                    }
                )
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                AnimatedContent(
                    targetState = activePage,
                    transitionSpec = {
                        fadeIn(spring(stiffness = Spring.StiffnessMedium)) togetherWith
                            fadeOut(spring(stiffness = Spring.StiffnessHigh))
                    },
                    label = "pageContent"
                ) { page ->
                    when (page) {
                        AppPage.HOME -> when (selectedTab) {
                            MainTab.ALL -> GalleryScreen(
                                media = orderedMedia,
                                favoriteUris = favoriteUris,
                                hasPermission = hasPermission,
                                isLoading = isLoading,
                                minCellSize = gridCellSize,
                                onRequestPermission = { permissionLauncher.launch(mediaPermissions()) },
                                onDateChanged = { dateLabel = formatDateLabel(it.displayDateMillis, context) },
                                onMediaClick = { openViewer(it, orderedMedia) }
                            )
                            MainTab.ALBUMS -> AlbumsScreen(
                                media = orderedMedia,
                                customAlbums = customAlbums,
                                hasPermission = hasPermission,
                                onCreateAlbum = {
                                    newAlbumName = ""
                                    showNewAlbum = true
                                },
                                onOpenDeviceAlbum = { id, name ->
                                    openedAlbum = OpenAlbum(name = name, deviceBucketId = id)
                                    activePage = AppPage.ALBUM
                                },
                                onOpenCustomAlbum = { id, name ->
                                    openedAlbum = OpenAlbum(name = name, customAlbumId = id)
                                    activePage = AppPage.ALBUM
                                },
                                onEditCustomAlbum = { album ->
                                    renameAlbumValue = album.name
                                    editAlbumTarget = album
                                }
                            )
                        }

                        AppPage.FAVORITES -> SimpleAlbumScreen(
                            title = stringResource(R.string.favorites),
                            media = orderedMedia.filter { it.uri.toString() in favoriteUris },
                            favoriteUris = favoriteUris,
                            minCellSize = gridCellSize,
                            onDateChanged = {},
                            onMediaClick = { openViewer(it, orderedMedia.filter { it.uri.toString() in favoriteUris }) }
                        )

                        AppPage.TRASH -> TrashScreen(
                            media = trash,
                            onRestore = { moveToTrash(it, shouldTrash = false) },
                            onDeleteForever = { deleteForever(it) }
                        )

                        AppPage.LOCKED -> LockedFolderScreen(
                            entries = lockedEntries,
                            unlocked = lockedUnlocked,
                            onUnlock = { unlockFolder() },
                            onImport = { lockedPicker.launch(arrayOf("image/*", "video/*")) },
                            onOpen = { entry ->
                                scope.launch {
                                    val preview = withContext(Dispatchers.IO) {
                                        runCatching { lockedStore.decryptToCache(entry) }
                                    }
                                    preview.onSuccess { uri ->
                                        lockedPreviewEntry = entry
                                        viewerItems = listOf(
                                            MediaItem(
                                                id = entry.id.hashCode().toLong(),
                                                uri = uri,
                                                displayName = entry.displayName,
                                                mimeType = entry.mimeType,
                                                dateModifiedMillis = System.currentTimeMillis(),
                                                dateTakenMillis = 0L,
                                                width = 0,
                                                height = 0,
                                                albumName = lockedFolderLabel,
                                                bucketId = "",
                                                relativePath = "",
                                                sizeBytes = entry.encryptedSize,
                                                durationMillis = 0L,
                                                isVideo = entry.mimeType.startsWith("video/"),
                                                isLocked = true
                                            )
                                        )
                                        viewerIndex = 0
                                    }.onFailure {
                                        showMessage(context, it.message ?: context.getString(R.string.locked_open_failed))
                                    }
                                }
                            },
                            onDelete = {
                                lockedStore.delete(it)
                                storeRevision++
                            },
                            onBackupExport = {
                                lockedBackupExport.launch("Angllary-Locked-Folder-Backup.zip")
                            },
                            onBackupRestore = {
                                lockedBackupRestore.launch(arrayOf("application/zip", "application/octet-stream"))
                            }
                        )

                        AppPage.SETTINGS -> SettingsScreen(
                            themeMode = themeMode,
                            gridMode = gridMode,
                            sortMode = sortMode,
                            onThemeChange = {
                                themeMode = it
                                galleryStore.setThemeMode(it)
                                onThemeModeChange(it)
                            },
                            onGridChange = {
                                gridMode = it
                                galleryStore.setGridMode(it)
                            },
                            onSortChange = {
                                sortMode = it
                                galleryStore.setSortMode(it)
                            }
                        )

                        AppPage.ALBUM -> SimpleAlbumScreen(
                            title = openedAlbum?.name.orEmpty(),
                            media = activeAlbumItems,
                            favoriteUris = favoriteUris,
                            minCellSize = gridCellSize,
                            onDateChanged = { dateLabel = formatDateLabel(it.displayDateMillis, context) },
                            onMediaClick = { openViewer(it, activeAlbumItems) }
                        )
                    }
                }
            }

            if (activePage == AppPage.HOME) {
                AngllaryBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onSearch = { searchOpen = true },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }

        if (searchOpen) {
            SearchOverlay(
                media = orderedMedia,
                favoriteUris = favoriteUris,
                onOpen = {
                    searchOpen = false
                    openViewer(it, orderedMedia)
                },
                onClose = { searchOpen = false }
            )
        }
    }

    val currentViewerIndex = viewerIndex
    if (currentViewerIndex != null && viewerItems.isNotEmpty()) {
        MediaViewer(
            media = viewerItems,
            initialIndex = currentViewerIndex,
            isFavorite = { it.uri.toString() in favoriteUris },
            onToggleFavorite = { updateFavorite(it) },
            onTrash = {
                moveToTrash(it)
                viewerIndex = null
            },
            onEditPhoto = {
                viewerIndex = null
                editorItem = it
            },
            onTrimVideo = {
                viewerIndex = null
                editorItem = it
            },
            onLockCopy = { importIntoLockedFolder(it) },
            onAddToAlbum = { albumTarget = it },
            onRemoveFromAlbum = { item ->
                openedAlbum?.customAlbumId?.let { id ->
                    galleryStore.removeFromAlbum(id, item.uri.toString())
                    storeRevision++
                    showMessage(context, context.getString(R.string.removed_from_album))
                }
            },
            onDismiss = {
                viewerIndex = null
                lockedPreviewEntry?.let(lockedStore::clearPreview)
                lockedPreviewEntry = null
            }
        )
    }

    editorItem?.let { item ->
        if (item.isVideo) {
            VideoTrimScreen(
                item = item,
                onClose = { editorItem = null },
                onExported = {
                    editorItem = null
                    reloadKey++
                    showMessage(context, context.getString(R.string.video_export_success))
                }
            )
        } else {
            PhotoEditorScreen(
                item = item,
                onClose = { editorItem = null },
                onSaved = {
                    editorItem = null
                    reloadKey++
                    showMessage(context, context.getString(R.string.photo_export_success))
                }
            )
        }
    }

    if (showNewAlbum) {
        AlertDialog(
            onDismissRequest = { showNewAlbum = false },
            title = { Text(stringResource(R.string.create_album)) },
            text = {
                OutlinedTextField(
                    value = newAlbumName,
                    onValueChange = { newAlbumName = it.take(48) },
                    singleLine = true,
                    label = { Text(stringResource(R.string.album_name)) }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    runCatching { galleryStore.createAlbum(newAlbumName) }
                        .onSuccess { album ->
                            albumTarget?.let { target -> galleryStore.addToAlbum(album.id, target.uri.toString()) }
                            albumTarget = null
                            storeRevision++
                            showNewAlbum = false
                        }
                        .onFailure { showMessage(context, it.message ?: context.getString(R.string.action_failed)) }
                }) { Text(stringResource(R.string.create)) }
            },
            dismissButton = { TextButton(onClick = { showNewAlbum = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    editAlbumTarget?.let { album ->
        AlertDialog(
            onDismissRequest = { editAlbumTarget = null },
            title = { Text(stringResource(R.string.manage_album)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = renameAlbumValue,
                        onValueChange = { renameAlbumValue = it.take(48) },
                        singleLine = true,
                        label = { Text(stringResource(R.string.album_name)) }
                    )
                    TextButton(onClick = {
                        galleryStore.deleteAlbum(album.id)
                        storeRevision++
                        editAlbumTarget = null
                        if (openedAlbum?.customAlbumId == album.id) {
                            openedAlbum = null
                            activePage = AppPage.HOME
                        }
                    }) { Text(stringResource(R.string.delete_album)) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    galleryStore.renameAlbum(album.id, renameAlbumValue)
                    storeRevision++
                    editAlbumTarget = null
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { editAlbumTarget = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    albumTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { albumTarget = null },
            title = { Text(stringResource(R.string.add_to_album)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (customAlbums.isEmpty()) {
                        Text(stringResource(R.string.no_custom_albums), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    customAlbums.forEach { album ->
                        TextButton(onClick = {
                            galleryStore.addToAlbum(album.id, item.uri.toString())
                            storeRevision++
                            albumTarget = null
                            showMessage(context, context.getString(R.string.added_to_album, album.name))
                        }) {
                            Text(album.name)
                        }
                    }
                    TextButton(onClick = {
                        newAlbumName = ""
                        showNewAlbum = true
                    }) {
                        Text(stringResource(R.string.create_album))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { albumTarget = null }) { Text(stringResource(R.string.close)) } }
        )
    }
}

@Composable
private fun SubPageHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        androidx.compose.material3.IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
        }
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), maxLines = 1)
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
    val today = now.get(java.util.Calendar.YEAR) == target.get(java.util.Calendar.YEAR) &&
        now.get(java.util.Calendar.DAY_OF_YEAR) == target.get(java.util.Calendar.DAY_OF_YEAR)
    if (today) return context.getString(R.string.today)
    now.add(java.util.Calendar.DAY_OF_YEAR, -1)
    val yesterday = now.get(java.util.Calendar.YEAR) == target.get(java.util.Calendar.YEAR) &&
        now.get(java.util.Calendar.DAY_OF_YEAR) == target.get(java.util.Calendar.DAY_OF_YEAR)
    if (yesterday) return context.getString(R.string.yesterday)
    return SimpleDateFormat("d MMM", Locale.ENGLISH).format(Date(timestamp))
}

private fun displayName(context: Context, uri: android.net.Uri): String {
    return runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }.getOrNull().orEmpty().ifBlank { "Private media" }
}

private fun showMessage(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
