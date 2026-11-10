package com.angllary.app.data

import android.content.ContentResolver
import android.content.ContentUris
import android.os.Build
import android.provider.MediaStore
import com.angllary.app.model.MediaItem

class MediaStoreRepository(
    private val contentResolver: ContentResolver
) {
    fun loadRecentMedia(limit: Int = 600): List<MediaItem> {
        val collection = MediaStore.Files.getContentUri("external")
        val projection = mutableListOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection += MediaStore.MediaColumns.WIDTH
            projection += MediaStore.MediaColumns.HEIGHT
        }

        val mediaTypeColumn = MediaStore.Files.FileColumns.MEDIA_TYPE
        val selection = "($mediaTypeColumn = ? OR $mediaTypeColumn = ?)"
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )

        val results = ArrayList<MediaItem>()
        try {
            contentResolver.query(
                collection,
                projection.toTypedArray(),
                selection,
                selectionArgs,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val modifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                val albumColumn = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
                val typeColumn = cursor.getColumnIndexOrThrow(mediaTypeColumn)
                val widthColumn = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
                val heightColumn = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)

                while (cursor.moveToNext() && results.size < limit) {
                    val id = cursor.getLong(idColumn)
                    val mediaType = cursor.getInt(typeColumn)
                    results += MediaItem(
                        id = id,
                        uri = ContentUris.withAppendedId(collection, id),
                        displayName = cursor.getString(nameColumn).orEmpty(),
                        mimeType = cursor.getString(mimeColumn).orEmpty(),
                        dateModifiedMillis = cursor.getLong(modifiedColumn) * 1_000L,
                        width = if (widthColumn >= 0) cursor.getInt(widthColumn) else 0,
                        height = if (heightColumn >= 0) cursor.getInt(heightColumn) else 0,
                        albumName = if (albumColumn >= 0) cursor.getString(albumColumn).orEmpty() else "",
                        isVideo = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                    )
                }
            }
        } catch (_: SecurityException) {
            return emptyList()
        }
        return results
    }
}
