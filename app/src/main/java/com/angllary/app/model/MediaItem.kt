package com.angllary.app.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val dateModifiedMillis: Long,
    val dateTakenMillis: Long,
    val width: Int,
    val height: Int,
    val albumName: String,
    val bucketId: String,
    val relativePath: String,
    val sizeBytes: Long,
    val durationMillis: Long,
    val isVideo: Boolean,
    val isLocked: Boolean = false,
    val isSystemFavorite: Boolean = false,
    val dateExpiresMillis: Long? = null
) {
    val displayDateMillis: Long
        get() = dateTakenMillis.takeIf { it > 0L } ?: dateModifiedMillis
}

data class CustomAlbum(
    val id: String,
    val name: String,
    val mediaUris: Set<String>,
    val isPinned: Boolean = false,
    val isHidden: Boolean = false
)
