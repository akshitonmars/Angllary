package com.angllary.app.data

import android.content.Context
import com.angllary.app.model.CustomAlbum
import java.util.UUID

class GalleryStore(context: Context) {
    private val preferences = context.getSharedPreferences("angllary_gallery", Context.MODE_PRIVATE)

    fun favoriteUris(): Set<String> =
        preferences.getStringSet(KEY_FAVORITES, emptySet())?.toSet() ?: emptySet()

    fun isFavorite(uri: String): Boolean = favoriteUris().contains(uri)

    fun setFavorite(uri: String, favorite: Boolean) {
        val updated = favoriteUris().toMutableSet()
        if (favorite) updated.add(uri) else updated.remove(uri)
        preferences.edit().putStringSet(KEY_FAVORITES, updated).apply()
    }

    fun customAlbums(): List<CustomAlbum> {
        val ids = preferences.getStringSet(KEY_ALBUM_IDS, emptySet())?.toList().orEmpty()
        return ids.mapNotNull { id ->
            val name = preferences.getString("album_name_$id", null) ?: return@mapNotNull null
            val uris = preferences.getStringSet("album_items_$id", emptySet())?.toSet() ?: emptySet()
            CustomAlbum(
                id = id,
                name = name,
                mediaUris = uris,
                isPinned = preferences.getBoolean("album_pinned_$id", false),
                isHidden = preferences.getBoolean("album_hidden_$id", false)
            )
        }.sortedWith(compareByDescending<CustomAlbum> { it.isPinned }.thenBy { it.name.lowercase() })
    }

    fun createAlbum(name: String): CustomAlbum {
        val normalized = name.trim().take(48)
        require(normalized.isNotBlank()) { "Album name cannot be empty." }
        val id = UUID.randomUUID().toString()
        val ids = preferences.getStringSet(KEY_ALBUM_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        ids.add(id)
        preferences.edit()
            .putStringSet(KEY_ALBUM_IDS, ids)
            .putString("album_name_$id", normalized)
            .putStringSet("album_items_$id", emptySet())
            .apply()
        return CustomAlbum(id, normalized, emptySet())
    }

    fun setAlbumPinned(id: String, pinned: Boolean) {
        preferences.edit().putBoolean("album_pinned_$id", pinned).apply()
    }

    fun setAlbumHidden(id: String, hidden: Boolean) {
        preferences.edit().putBoolean("album_hidden_$id", hidden).apply()
    }

    fun renameAlbum(id: String, name: String) {
        val normalized = name.trim().take(48)
        if (normalized.isNotBlank()) preferences.edit().putString("album_name_$id", normalized).apply()
    }

    fun deleteAlbum(id: String) {
        val ids = preferences.getStringSet(KEY_ALBUM_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        ids.remove(id)
        preferences.edit()
            .putStringSet(KEY_ALBUM_IDS, ids)
            .remove("album_name_$id")
            .remove("album_items_$id")
            .remove("album_pinned_$id")
            .remove("album_hidden_$id")
            .apply()
    }

    fun addToAlbum(id: String, uri: String) {
        val items = preferences.getStringSet("album_items_$id", emptySet())?.toMutableSet() ?: mutableSetOf()
        items.add(uri)
        preferences.edit().putStringSet("album_items_$id", items).apply()
    }

    fun removeFromAlbum(id: String, uri: String) {
        val items = preferences.getStringSet("album_items_$id", emptySet())?.toMutableSet() ?: mutableSetOf()
        items.remove(uri)
        preferences.edit().putStringSet("album_items_$id", items).apply()
    }

    fun albumItems(id: String): Set<String> =
        preferences.getStringSet("album_items_$id", emptySet())?.toSet() ?: emptySet()

    fun themeMode(): String = preferences.getString(KEY_THEME, "system") ?: "system"
    fun setThemeMode(mode: String) { preferences.edit().putString(KEY_THEME, mode).apply() }
    fun gridMode(): String = preferences.getString(KEY_GRID, "comfortable") ?: "comfortable"
    fun setGridMode(mode: String) { preferences.edit().putString(KEY_GRID, mode).apply() }
    fun sortMode(): String = preferences.getString(KEY_SORT, "newest") ?: "newest"
    fun setSortMode(mode: String) { preferences.edit().putString(KEY_SORT, mode).apply() }

    private companion object {
        const val KEY_FAVORITES = "favorite_uris"
        const val KEY_ALBUM_IDS = "custom_album_ids"
        const val KEY_THEME = "theme_mode"
        const val KEY_GRID = "grid_mode"
        const val KEY_SORT = "sort_mode"
    }
}
