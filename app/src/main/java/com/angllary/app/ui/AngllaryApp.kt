package com.angllary.app.ui

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import com.angllary.app.ui.components.rememberHapticClick
import com.angllary.app.ui.screens.AlbumsScreen
import com.angllary.app.ui.screens.GalleryScreen
import com.angllary.app.ui.screens.LockedFolderScreen
import com.angllary.app.ui.screens.SearchScreen
import com.angllary.app.ui.screens.SettingsScreen
import com.angllary.app.ui.screens.SimpleAlbumScreen
import com.angllary.app.ui.screens.TrashScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class AppPage { HOME, SEARCH, FAVORITES, TRASH, LOCKED, SETTINGS, ALBUM }
private data class OpenAlbum(val name: String, val deviceBucketId: String? = null, val customAlbumId: String? = null, val quickFilter: String? = null)

@Composable
fun AngllaryApp(
    repository: MediaStoreRepository,
    galleryStore: GalleryStore,
    onThemeModeChange: (String) -> Unit,
    incomingIntent: Intent? = null,
    onPickerResult: (Uri) -> Unit = {}
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
    val homeTabPagerState = rememberPagerState(
        initialPage = if (selectedTab == MainTab.ALBUMS) 1 else 0
    ) { 2 }
    LaunchedEffect(selectedTab) {
        val targetPage = if (selectedTab == MainTab.ALBUMS) 1 else 0
        if (homeTabPagerState.currentPage != targetPage) {
            homeTabPagerState.animateScrollToPage(targetPage)
        }
    }
    LaunchedEffect(homeTabPagerState) {
        snapshotFlow { homeTabPagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                val tab = if (page == 0) MainTab.ALL else MainTab.ALBUMS
                if (selectedTab != tab) selectedTab = tab
            }
    }
    val homeGridState = rememberLazyStaggeredGridState()
    val albumsListState = rememberLazyListState()

    val headerIsScrolled by remember {
        derivedStateOf {
            if (selectedTab == MainTab.ALL) {
                homeGridState.firstVisibleItemIndex > 0 || homeGridState.firstVisibleItemScrollOffset > 36
            } else {
                albumsListState.firstVisibleItemIndex > 0 || albumsListState.firstVisibleItemScrollOffset > 36
            }
        }
    }
    var activePage by remember { mutableStateOf(AppPage.HOME) }
    var openedAlbum by remember { mutableStateOf<OpenAlbum?>(null) }
    var dateLabel by remember { mutableStateOf(context.getString(R.string.today)) }
    var mediaSelectionActive by remember { mutableStateOf(false) }
    var selectedUris by remember { mutableStateOf(emptySet<String>()) }
    var externalViewer by remember { mutableStateOf(incomingIntent?.action == Intent.ACTION_VIEW || incomingIntent?.action == Intent.ACTION_SEND || incomingIntent?.action == Intent.ACTION_SEND_MULTIPLE) }
    var pickerRequest by remember { mutableStateOf(incomingIntent?.action == Intent.ACTION_PICK || incomingIntent?.action == Intent.ACTION_GET_CONTENT) }
    var pendingFavoriteChanges by remember { mutableStateOf(emptyList<Pair<MediaItem, Boolean>>()) }
    var pendingTrashUndoItems by remember { mutableStateOf(emptyList<MediaItem>()) }
    val snackbarHostState = remember { SnackbarHostState() }
    var sortMode by remember { mutableStateOf(galleryStore.sortMode()) }
    var gridMode by remember { mutableStateOf(galleryStore.gridMode()) }
    var themeMode by remember { mutableStateOf(galleryStore.themeMode()) }

    var backProgress by remember { mutableFloatStateOf(0f) }
    var viewerItems by remember { mutableStateOf(emptyList<MediaItem>()) }
    var viewerIndex by remember { mutableStateOf<Int?>(null) }
    var closeViewerAfterTrash by remember { mutableStateOf(false) }
    var lockedPreviewEntry by remember { mutableStateOf<LockedEntry?>(null) }
    var editorItem by remember { mutableStateOf<MediaItem?>(null) }

    var showNewAlbum by remember { mutableStateOf(false) }
    var newAlbumName by remember { mutableStateOf("") }
    var albumTargets by remember { mutableStateOf(emptyList<MediaItem>()) }
    var editAlbumTarget by remember { mutableStateOf<CustomAlbum?>(null) }
    var renameAlbumValue by remember { mutableStateOf("") }
    var lockedUnlocked by remember { mutableStateOf(false) }

    LaunchedEffect(incomingIntent) {
        when (incomingIntent?.action) {
            Intent.ACTION_VIEW, Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE -> {
                externalViewer = true
                pickerRequest = false
                @Suppress("DEPRECATION")
                val uris = when (incomingIntent.action) {
                    Intent.ACTION_SEND_MULTIPLE -> incomingIntent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM).orEmpty()
                    Intent.ACTION_SEND -> listOfNotNull(incomingIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
                    else -> listOfNotNull(incomingIntent.data)
                }.ifEmpty { listOfNotNull(incomingIntent.data) }
                val incomingItems = withContext(Dispatchers.IO) {
                    uris.distinct().mapNotNull { readIncomingMediaItem(context, it) }
                }
                if (incomingItems.isNotEmpty()) {
                    viewerItems = incomingItems
                    viewerIndex = 0
                } else {
                    externalViewer = false
                    showMessage(context, "Angllary could not open this media item.")
                }
            }
            Intent.ACTION_PICK, Intent.ACTION_GET_CONTENT -> {
                pickerRequest = true
                externalViewer = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasPermission = hasMediaAccess(context)
        reloadKey++
    }

    val systemMediaAction = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (pendingFavoriteChanges.isNotEmpty()) {
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                pendingFavoriteChanges.forEach { (item, favorite) ->
                    repository.setSystemFavorite(item, favorite)
                    galleryStore.setFavorite(item.uri.toString(), favorite)
                    galleryStore.setFavorite("content://media/external/file/${item.id}", favorite)
                }
                favoriteUris = galleryStore.favoriteUris() + media.filter { it.isSystemFavorite }.map { it.uri.toString() }
            }
            pendingFavoriteChanges = emptyList()
        }
        if (pendingTrashUndoItems.isNotEmpty()) {
            val recentlyTrashed = pendingTrashUndoItems
            pendingTrashUndoItems = emptyList()
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                scope.launch {
                    val action = snackbarHostState.showSnackbar(
                        message = if (recentlyTrashed.size == 1) "Moved to Recently Deleted" else "${recentlyTrashed.size} items moved to Recently Deleted",
                        actionLabel = "Undo"
                    )
                    if (action == SnackbarResult.ActionPerformed) {
                        showSystemMediaRequest(repository.createTrashRequest(recentlyTrashed, false))
                    }
                }
            }
        }
        reloadKey++
        if (closeViewerAfterTrash) {
            if (result.resultCode == android.app.Activity.RESULT_OK) viewerIndex = null
            closeViewerAfterTrash = false
        }
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

    LaunchedEffect(hasPermission, reloadKey, externalViewer) {
        if (externalViewer) {
            isLoading = false
            return@LaunchedEffect
        }
        if (!hasPermission) {
            media = emptyList()
            trash = emptyList()
            return@LaunchedEffect
        }
        isLoading = true
        try {
            val loaded = withContext(Dispatchers.IO) { repository.loadMedia() }
            media = loaded.sortedByDescending { it.displayDateMillis }
            favoriteUris = galleryStore.favoriteUris() + loaded.filter { it.isSystemFavorite }.map { it.uri.toString() }
            trash = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                withContext(Dispatchers.IO) { repository.loadMedia(trashed = true) }
                    .sortedByDescending { it.displayDateMillis }
            } else emptyList()
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(storeRevision) {
        favoriteUris = galleryStore.favoriteUris() + media.filter { it.isSystemFavorite }.map { it.uri.toString() }
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

    val selectedMedia = remember(orderedMedia, selectedUris) {
        orderedMedia.filter { it.uri.toString() in selectedUris }
    }

    DisposableEffect(activePage, lockedUnlocked, lockedPreviewEntry) {
        val activity = context as? android.app.Activity
        val window = activity?.window
        val previouslySecure = window?.attributes?.flags?.and(WindowManager.LayoutParams.FLAG_SECURE) != 0
        val shouldSecure = (activePage == AppPage.LOCKED && lockedUnlocked) || lockedPreviewEntry != null
        if (shouldSecure) window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            if (previouslySecure) window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            else window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    val gridCellSize = when (gridMode) {
        "compact" -> 104.dp
        "large" -> 164.dp
        else -> 132.dp
    }

    val lockedFolderLabel = stringResource(R.string.locked_folder)

    val activeAlbumItems = remember(media, openedAlbum, customAlbums, favoriteUris) {
        val album = openedAlbum ?: return@remember emptyList()
        when {
            album.quickFilter != null -> when (album.quickFilter) {
                "favorites" -> media.filter { it.uri.toString() in favoriteUris || "content://media/external/file/${it.id}" in favoriteUris }
                "videos" -> media.filter { it.isVideo }
                "screenshots" -> media.filter { it.relativePath.contains("screenshot", true) || it.displayName.contains("screenshot", true) }
                "large" -> media.filter { it.sizeBytes >= 50L * 1024L * 1024L }
                "recent" -> media.sortedByDescending { it.displayDateMillis }.take(100)
                else -> emptyList()
            }
            album.customAlbumId != null -> {
                val uris = customAlbums.firstOrNull { it.id == album.customAlbumId }?.mediaUris.orEmpty()
                media.filter { it.uri.toString() in uris || "content://media/external/file/${it.id}" in uris }
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
        val current = item.uri.toString() in favoriteUris || "content://media/external/file/${item.id}" in favoriteUris
        val next = !current
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && item.id >= 0L) {
            pendingFavoriteChanges = listOf(item to next)
            showSystemMediaRequest(repository.createFavoriteWriteRequest(listOf(item)))
        } else {
            galleryStore.setFavorite(item.uri.toString(), next)
            if (item.id >= 0L) galleryStore.setFavorite("content://media/external/file/${item.id}", next)
            favoriteUris = galleryStore.favoriteUris()
        }
    }

    fun showSystemMediaRequest(pendingIntent: PendingIntent?) {
        if (pendingIntent == null) {
            pendingFavoriteChanges = emptyList()
            pendingTrashUndoItems = emptyList()
            closeViewerAfterTrash = false
            showMessage(context, context.getString(R.string.system_trash_unavailable))
            return
        }
        runCatching {
            systemMediaAction.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
        }.onFailure { showMessage(context, it.message ?: context.getString(R.string.action_failed)) }
    }

    fun moveToTrash(item: MediaItem, shouldTrash: Boolean = true) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            closeViewerAfterTrash = false
            showMessage(context, context.getString(R.string.system_trash_requires_android_11))
            return
        }
        pendingTrashUndoItems = if (shouldTrash) listOf(item) else emptyList()
        runCatching {
            showSystemMediaRequest(repository.createTrashRequest(listOf(item), shouldTrash))
        }.onFailure {
            pendingTrashUndoItems = emptyList()
            closeViewerAfterTrash = false
            showMessage(context, it.message ?: context.getString(R.string.action_failed))
        }
    }

    fun deleteForever(item: MediaItem) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val deleted = repository.deleteDirect(item)
            if (deleted) {
                reloadKey++
                showMessage(context, context.getString(R.string.item_deleted))
            } else {
                showMessage(context, context.getString(R.string.action_failed))
            }
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

    fun shareSelected() {
        if (selectedMedia.isEmpty()) return
        val send = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = if (selectedMedia.all { !it.isVideo }) "image/*" else "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(selectedMedia.map { it.uri }))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { context.startActivity(Intent.createChooser(send, "Share media")) }
        selectedUris = emptySet()
    }

    fun favoriteSelected() {
        if (selectedMedia.isEmpty()) return
        val allFavorite = selectedMedia.all { it.uri.toString() in favoriteUris || "content://media/external/file/${it.id}" in favoriteUris }
        val target = !allFavorite
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            pendingFavoriteChanges = selectedMedia.map { it to target }
            showSystemMediaRequest(repository.createFavoriteWriteRequest(selectedMedia))
        } else {
            selectedMedia.forEach { item ->
                galleryStore.setFavorite(item.uri.toString(), target)
                if (item.id >= 0L) galleryStore.setFavorite("content://media/external/file/${item.id}", target)
            }
            favoriteUris = galleryStore.favoriteUris()
        }
        selectedUris = emptySet()
    }

    fun lockSelected() {
        if (selectedMedia.isEmpty()) return
        val items = selectedMedia
        selectedUris = emptySet()
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                items.count { item ->
                    runCatching { lockedStore.importUri(item.uri, item.displayName, item.mimeType) }.isSuccess
                }
            }
            storeRevision++
            showMessage(context, "$result of ${items.size} items copied to Locked Folder")
        }
    }

    fun trashSelected() {
        if (selectedMedia.isEmpty()) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            showMessage(context, context.getString(R.string.system_trash_requires_android_11))
            return
        }
        pendingTrashUndoItems = selectedMedia
        val request = repository.createTrashRequest(selectedMedia, true)
        showSystemMediaRequest(request)
        selectedUris = emptySet()
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

    fun navigateBack() {
        when (activePage) {
            AppPage.ALBUM -> {
                activePage = AppPage.HOME
                openedAlbum = null
            }
            AppPage.LOCKED -> {
                lockedUnlocked = false
                activePage = AppPage.HOME
            }
            else -> activePage = AppPage.HOME
        }
    }

    PredictiveBackHandler(enabled = activePage != AppPage.HOME) { progress ->
        try {
            progress.collect { event -> backProgress = event.progress }
            navigateBack()
        } catch (_: CancellationException) {
            // Gesture cancelled: restore the current destination without changing state.
        } finally {
            backProgress = 0f
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.onBackground
    ) {
        Box(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
        Column(Modifier.fillMaxSize()) {
            if (activePage == AppPage.HOME) {
                AngllaryTopBar(
                    dateLabel = dateLabel,
                    showDate = selectedTab == MainTab.ALL,
                    isScrolled = false,
                    modifier = Modifier.blur(if (mediaSelectionActive) 17.dp else 0.dp),
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
                        AppPage.SEARCH -> stringResource(R.string.search)
                        AppPage.FAVORITES -> stringResource(R.string.favorites)
                        AppPage.TRASH -> stringResource(R.string.recently_deleted)
                        AppPage.LOCKED -> stringResource(R.string.locked_folder)
                        AppPage.SETTINGS -> stringResource(R.string.settings)
                        AppPage.ALBUM -> openedAlbum?.name.orEmpty()
                        AppPage.HOME -> stringResource(R.string.app_name)
                    },
                    onBack = { navigateBack() }
                )
            }

            Box(
                Modifier.weight(1f).fillMaxWidth().graphicsLayer {
                    translationX = size.width * 0.18f * backProgress
                    scaleX = 1f - (0.018f * backProgress)
                    scaleY = 1f - (0.018f * backProgress)
                }
            ) {
                AnimatedContent(
                    targetState = activePage,
                    transitionSpec = {
                        val forward = targetState.ordinal >= initialState.ordinal
                        if (forward) {
                            (slideInHorizontally(
                                animationSpec = tween(250),
                                initialOffsetX = { it / 14 }
                            ) + fadeIn(animationSpec = tween(210))) togetherWith
                                (slideOutHorizontally(
                                    animationSpec = tween(190),
                                    targetOffsetX = { -it / 20 }
                                ) + fadeOut(animationSpec = tween(160)))
                        } else {
                            (slideInHorizontally(
                                animationSpec = tween(230),
                                initialOffsetX = { -it / 18 }
                            ) + fadeIn(animationSpec = tween(200))) togetherWith
                                (slideOutHorizontally(
                                    animationSpec = tween(180),
                                    targetOffsetX = { it / 14 }
                                ) + fadeOut(animationSpec = tween(150)))
                        }
                    },
                    label = "destinationTransition"
                ) { page ->
                    when (page) {
                        AppPage.HOME -> {
                            HorizontalPager(
                                state = homeTabPagerState,
                                modifier = Modifier.fillMaxSize(),
                                beyondViewportPageCount = 1,
                                key = { it }
                            ) { pageIndex ->
                                when (if (pageIndex == 0) MainTab.ALL else MainTab.ALBUMS) {
                                    MainTab.ALL -> GalleryScreen(
                                        media = orderedMedia,
                                        favoriteUris = favoriteUris,
                                        hasPermission = hasPermission,
                                        isLoading = isLoading,
                                        minCellSize = gridCellSize,
                                        onRequestPermission = { permissionLauncher.launch(mediaPermissions()) },
                                        onDateChanged = { dateLabel = formatDateLabel(it.displayDateMillis, context) },
                                        onMediaClick = { item ->
                                            if (pickerRequest) onPickerResult(item.uri) else openViewer(item, orderedMedia)
                                        },
                                        onFavoriteToggle = { updateFavorite(it) },
                                        onSelectionActiveChange = { mediaSelectionActive = it },
                                        selectedUris = selectedUris,
                                        onSelectionChange = { selectedUris = it },
                                        isPartialAccess = isPartialMediaAccess(context),
                                        gridState = homeGridState
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
                                        },
                                        onOpenQuickView = { key, label ->
                                            openedAlbum = OpenAlbum(name = label, quickFilter = key)
                                            activePage = AppPage.ALBUM
                                        },
                                        onTogglePinned = { album ->
                                            galleryStore.setAlbumPinned(album.id, !album.isPinned)
                                            storeRevision++
                                        },
                                        onToggleHidden = { album ->
                                            galleryStore.setAlbumHidden(album.id, !album.isHidden)
                                            storeRevision++
                                        },
                                        listState = albumsListState
                                    )
                                }
                            }
                        }

                        AppPage.SEARCH -> SearchScreen(
                            media = orderedMedia,
                            favoriteUris = favoriteUris,
                            onOpen = { clicked, results -> openViewer(clicked, results) }
                        )

                        AppPage.FAVORITES -> SimpleAlbumScreen(
                            media = orderedMedia.filter { it.uri.toString() in favoriteUris || "content://media/external/file/${it.id}" in favoriteUris },
                            favoriteUris = favoriteUris,
                            minCellSize = gridCellSize,
                            onDateChanged = {},
                            onMediaClick = { openViewer(it, orderedMedia.filter { m -> m.uri.toString() in favoriteUris || "content://media/external/file/${m.id}" in favoriteUris }) }
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
                            totalMediaCount = media.size,
                            photosCount = media.count { !it.isVideo },
                            videosCount = media.count { it.isVideo },
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
                            media = activeAlbumItems,
                            favoriteUris = favoriteUris,
                            minCellSize = gridCellSize,
                            onDateChanged = { dateLabel = formatDateLabel(it.displayDateMillis, context) },
                            onMediaClick = { openViewer(it, activeAlbumItems) }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 82.dp)
        )

        if (activePage == AppPage.HOME && selectedUris.isNotEmpty()) {
            androidx.compose.material3.Surface(
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = 8.dp, vertical = 10.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("${selectedUris.size} selected", modifier = Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.labelLarge, maxLines = 1)
                    TextButton(onClick = { selectedUris = emptySet() }) { Text("Cancel") }
                    TextButton(onClick = { shareSelected() }) { Text("Share") }
                    TextButton(onClick = { albumTargets = selectedMedia; selectedUris = emptySet() }) { Text("Album") }
                    TextButton(onClick = { lockSelected() }) { Text("Lock") }
                    TextButton(onClick = { favoriteSelected() }) { Text("♥") }
                    TextButton(onClick = { trashSelected() }) { Text("Delete") }
                }
            }
        } else {
            AnimatedVisibility(
                visible = activePage == AppPage.HOME,
                enter = fadeIn(tween(220)) + slideInVertically(tween(300)) { it / 2 },
                exit = fadeOut(tween(150)) + slideOutVertically(tween(220)) { it / 2 },
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 10.dp)
            ) {
                AngllaryBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onSearch = { activePage = AppPage.SEARCH }
                )
            }
        }
    }
    }

    val currentViewerIndex = viewerIndex
    if (currentViewerIndex != null && viewerItems.isNotEmpty()) {
        MediaViewer(
            media = viewerItems,
            initialIndex = currentViewerIndex,
            isFavorite = { it.uri.toString() in favoriteUris || "content://media/external/file/${it.id}" in favoriteUris },
            onToggleFavorite = { updateFavorite(it) },
            onTrash = {
                if (it.id >= 0L) {
                    closeViewerAfterTrash = true
                    moveToTrash(it)
                } else showMessage(context, "This shared item is read-only in Angllary.")
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
            onAddToAlbum = { albumTargets = listOf(it) },
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
                if (externalViewer) externalViewer = false
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
                                            albumTargets.forEach { target -> galleryStore.addToAlbum(album.id, target.uri.toString()) }
                            albumTargets = emptyList()
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

    if (albumTargets.isNotEmpty()) {
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
                            albumTargets.forEach { target -> galleryStore.addToAlbum(album.id, target.uri.toString()) }
                            storeRevision++
                            albumTargets = emptyList()
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
            confirmButton = { TextButton(onClick = { albumTargets = emptyList() }) { Text(stringResource(R.string.close)) } }
        )
    }
}

@Composable
private fun SubPageHeader(title: String, onBack: () -> Unit) {
    val backClick = rememberHapticClick(onBack)
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column {
            Spacer(Modifier.windowInsetsTopHeight(androidx.compose.foundation.layout.WindowInsets.statusBars))
            Row(
                modifier = Modifier.fillMaxWidth().height(62.dp).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.IconButton(onClick = backClick) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), maxLines = 1)
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

private fun readIncomingMediaItem(context: Context, uri: Uri): MediaItem? {
    return runCatching {
        val mime = context.contentResolver.getType(uri).orEmpty().ifBlank { "image/*" }
        var name = "Shared media"
        var size = 0L
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) name = cursor.getString(nameIndex).orEmpty().ifBlank { name }
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
            }
        }
        val video = mime.startsWith("video/")
        MediaItem(
            id = -1L,
            uri = uri,
            displayName = name,
            mimeType = mime,
            dateModifiedMillis = System.currentTimeMillis(),
            dateTakenMillis = 0L,
            width = 0,
            height = 0,
            albumName = "Shared",
            bucketId = "",
            relativePath = "",
            sizeBytes = size,
            durationMillis = 0L,
            isVideo = video
        )
    }.getOrNull()
}

private fun isPartialMediaAccess(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < 34) return false
    val selected = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED
    val images = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
    val videos = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
    return selected && !(images && videos)
}
