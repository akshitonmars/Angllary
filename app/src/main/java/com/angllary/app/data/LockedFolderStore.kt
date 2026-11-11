package com.angllary.app.data

import android.content.Context
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class LockedEntry(
    val id: String,
    val displayName: String,
    val mimeType: String,
    val encryptedSize: Long,
    val iv: String
)

class LockedFolderStore(private val context: Context) {
    private val preferences = context.getSharedPreferences("angllary_locked_folder", Context.MODE_PRIVATE)
    private val directory = File(context.noBackupFilesDir, "locked_media").apply { mkdirs() }
    private val keyAlias = "angllary.locked.media.v1"

    fun entries(): List<LockedEntry> {
        val json = preferences.getString("entries", "[]") ?: "[]"
        val array = runCatching { JSONArray(json) }.getOrElse { JSONArray() }
        return (0 until array.length()).mapNotNull { index ->
            runCatching {
                val item = array.getJSONObject(index)
                LockedEntry(item.getString("id"), item.getString("name"), item.getString("mime"), item.optLong("size"), item.getString("iv"))
            }.getOrNull()
        }.filter { File(directory, it.id + ".bin").exists() }
    }

    @Synchronized
    fun importUri(uri: Uri, displayName: String, mimeType: String): LockedEntry {
        val id = UUID.randomUUID().toString()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val file = File(directory, "$id.bin")
        try {
            val input = context.contentResolver.openInputStream(uri) ?: error("Could not open selected media.")
            input.use { source ->
                CipherOutputStream(file.outputStream(), cipher).use { encrypted ->
                    source.copyTo(encrypted, bufferSize = 64 * 1024)
                }
            }
            val entry = LockedEntry(
                id,
                displayName.ifBlank { "Private media" },
                mimeType.ifBlank { "application/octet-stream" },
                file.length(),
                Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
            )
            saveEntries(entries() + entry)
            return entry
        } catch (error: Exception) {
            file.delete()
            throw error
        }
    }

    fun decryptToCache(entry: LockedEntry): Uri {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, Base64.decode(entry.iv, Base64.NO_WRAP)))
        val preview = File(context.cacheDir, "locked_${entry.id}.${extensionFor(entry.mimeType, entry.displayName)}")
        CipherInputStream(File(directory, entry.id + ".bin").inputStream(), cipher).use { encrypted ->
            preview.outputStream().use { output -> encrypted.copyTo(output, bufferSize = 64 * 1024) }
        }
        return Uri.fromFile(preview)
    }

    fun clearPreview(entry: LockedEntry) {
        File(context.cacheDir, "locked_${entry.id}.${extensionFor(entry.mimeType, entry.displayName)}").delete()
    }

    @Synchronized
    fun delete(entry: LockedEntry) {
        File(directory, entry.id + ".bin").delete()
        File(context.cacheDir, "locked_${entry.id}.${extensionFor(entry.mimeType, entry.displayName)}").delete()
        saveEntries(entries().filterNot { it.id == entry.id })
    }

    private fun saveEntries(entries: List<LockedEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(JSONObject()
                .put("id", entry.id)
                .put("name", entry.displayName)
                .put("mime", entry.mimeType)
                .put("size", entry.encryptedSize)
                .put("iv", entry.iv))
        }
        preferences.edit().putString("entries", array.toString()).commit()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(keyAlias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private fun extensionFor(mime: String, name: String): String {
        val extension = when {
            mime.contains("jpeg", true) -> "jpg"
            mime.contains("png", true) -> "png"
            mime.contains("webp", true) -> "webp"
            mime.contains("mp4", true) -> "mp4"
            mime.contains("quicktime", true) -> "mov"
            mime.startsWith("video/") -> name.substringAfterLast('.', "mp4")
            else -> name.substringAfterLast('.', "bin")
        }
        return extension.filter { it.isLetterOrDigit() }.take(8).ifBlank { "bin" }
    }
}
