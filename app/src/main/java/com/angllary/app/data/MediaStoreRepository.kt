package com.angllary.app.data

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentValues
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.angllary.app.model.MediaItem

class MediaStoreRepository(private val resolver: ContentResolver) {
    private val collection: Uri
        get() = MediaStore.Files.getContentUri("external")

    fun loadMedia(limit: Int = 20000, trashed: Boolean = false): List<MediaItem> {
        val projection = mutableListOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.SIZE,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.BUCKET_ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Video.VideoColumns.DURATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection += MediaStore.MediaColumns.WIDTH
            projection += MediaStore.MediaColumns.HEIGHT
            projection += MediaStore.MediaColumns.RELATIVE_PATH
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            projection += MediaStore.MediaColumns.IS_TRASHED
            projection += MediaStore.MediaColumns.IS_FAVORITE
            projection += MediaStore.MediaColumns.DATE_EXPIRES
        }

        val typeColumn = MediaStore.Files.FileColumns.MEDIA_TYPE
        val selection = buildString {
            append("($typeColumn = ? OR $typeColumn = ?)")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                append(" AND ${MediaStore.MediaColumns.IS_TRASHED} = ?")
            }
        }
        val args = buildList {
            add(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())
            add(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) add(if (trashed) "1" else "0")
        }.toTypedArray()

        val output = ArrayList<MediaItem>()
        try {
            resolver.query(
                collection,
                projection.toTypedArray(),
                selection,
                args,
                "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC, ${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                fun column(name: String) = cursor.getColumnIndex(name)
                val idCol = column(MediaStore.MediaColumns._ID)
                val nameCol = column(MediaStore.MediaColumns.DISPLAY_NAME)
                val mimeCol = column(MediaStore.MediaColumns.MIME_TYPE)
                val modifiedCol = column(MediaStore.MediaColumns.DATE_MODIFIED)
                val sizeCol = column(MediaStore.MediaColumns.SIZE)
                val takenCol = column(MediaStore.Images.ImageColumns.DATE_TAKEN)
                val albumCol = column(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
                val bucketCol = column(MediaStore.Images.ImageColumns.BUCKET_ID)
                val typeCol = column(typeColumn)
                val widthCol = column(MediaStore.MediaColumns.WIDTH)
                val heightCol = column(MediaStore.MediaColumns.HEIGHT)
                val pathCol = column(MediaStore.MediaColumns.RELATIVE_PATH)
                val durationCol = column(MediaStore.Video.VideoColumns.DURATION)
                val favoriteCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) column(MediaStore.MediaColumns.IS_FAVORITE) else -1
                val expiresCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) column(MediaStore.MediaColumns.DATE_EXPIRES) else -1

                while (cursor.moveToNext() && output.size < limit) {
                    val mediaType = cursor.getInt(typeCol)
                    val isVideo = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                    val id = cursor.getLong(idCol)
                    val itemUri = ContentUris.withAppendedId(
                        if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    output += MediaItem(
                        id = id,
                        uri = itemUri,
                        displayName = cursor.getString(nameCol).orEmpty(),
                        mimeType = if (mimeCol >= 0) cursor.getString(mimeCol).orEmpty() else "",
                        dateModifiedMillis = cursor.getLong(modifiedCol) * 1000L,
                        dateTakenMillis = if (takenCol >= 0) cursor.getLong(takenCol) else 0L,
                        width = if (widthCol >= 0) cursor.getInt(widthCol) else 0,
                        height = if (heightCol >= 0) cursor.getInt(heightCol) else 0,
                        albumName = if (albumCol >= 0) cursor.getString(albumCol).orEmpty() else "",
                        bucketId = if (bucketCol >= 0) cursor.getString(bucketCol).orEmpty() else "",
                        relativePath = if (pathCol >= 0) cursor.getString(pathCol).orEmpty() else "",
                        sizeBytes = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L,
                        durationMillis = if (isVideo && durationCol >= 0) cursor.getLong(durationCol) else 0L,
                        isVideo = isVideo,
                        isSystemFavorite = favoriteCol >= 0 && cursor.getInt(favoriteCol) == 1,
                        dateExpiresMillis = if (expiresCol >= 0 && !cursor.isNull(expiresCol)) cursor.getLong(expiresCol) * 1000L else null
                    )
                }
            }
        } catch (_: SecurityException) {
            return emptyList()
        } catch (_: IllegalArgumentException) {
            return emptyList()
        }
        return output
    }

    fun toMediaUri(item: MediaItem): Uri {
        val base = if (item.isVideo) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        return ContentUris.withAppendedId(base, item.id)
    }

    fun createTrashRequest(items: List<MediaItem>, trash: Boolean): PendingIntent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || items.isEmpty()) return null
        return MediaStore.createTrashRequest(resolver, items.map { toMediaUri(it) }, trash)
    }

    fun createDeleteRequest(items: List<MediaItem>): PendingIntent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || items.isEmpty()) return null
        return MediaStore.createDeleteRequest(resolver, items.map { toMediaUri(it) })
    }

    fun createFavoriteWriteRequest(items: List<MediaItem>): PendingIntent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || items.isEmpty()) return null
        return MediaStore.createWriteRequest(resolver, items.map { toMediaUri(it) })
    }

    fun setSystemFavorite(item: MediaItem, favorite: Boolean): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
        return try {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_FAVORITE, if (favorite) 1 else 0)
            }
            resolver.update(toMediaUri(item), values, null, null) > 0
        } catch (_: Exception) {
            false
        }
    }

    fun deleteDirect(item: MediaItem): Boolean {
        return try {
            resolver.delete(toMediaUri(item), null, null) > 0
        } catch (_: Exception) {
            false
        }
    }
}
