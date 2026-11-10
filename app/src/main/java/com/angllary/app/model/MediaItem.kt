package com.angllary.app.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val dateModifiedMillis: Long,
    val width: Int,
    val height: Int,
    val albumName: String,
    val isVideo: Boolean
)
