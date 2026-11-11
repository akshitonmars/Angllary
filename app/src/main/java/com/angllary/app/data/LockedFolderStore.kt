package com.angllary.app.data

import android.content.Context
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
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

    init {
        // Remove decrypted previews left behind if the process was terminated while the viewer was open.
        context.cacheDir.listFiles()?.filter { it.name.startsWith("locked_") }?.forEach { it.delete() }
    }

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

    @Synchronized
    fun exportBackup(destination: Uri): Int {
        val current = entries()
        val output = context.contentResolver.openOutputStream(destination, "w")
            ?: error("Could not create the backup file.")
        ZipOutputStream(output.buffered()).use { zip ->
            val manifest = JSONArray()
            current.forEach { entry ->
                manifest.put(
                    JSONObject()
                        .put("id", entry.id)
                        .put("name", entry.displayName)
                        .put("mime", entry.mimeType)
                        .put("size", entry.encryptedSize)
                        .put("iv", entry.iv)
                )
            }
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(manifest.toString().toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            current.forEach { entry ->
                val encryptedFile = File(directory, entry.id + ".bin")
                if (!encryptedFile.exists()) return@forEach
                zip.putNextEntry(ZipEntry("locked/${entry.id}.bin"))
                encryptedFile.inputStream().buffered().use { input -> input.copyTo(zip, 64 * 1024) }
                zip.closeEntry()
            }
        }
        return current.size
    }

    @Synchronized
    fun restoreBackup(source: Uri): Int {
        val staging = File(context.cacheDir, "locked_restore_${UUID.randomUUID()}").apply { mkdirs() }
        try {
            var manifestBytes: ByteArray? = null
            var restoredBytes = 0L
            val maximumArchiveBytes = 8L * 1024L * 1024L * 1024L

            val input = context.contentResolver.openInputStream(source)
                ?: error("Could not open the backup file.")
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    when {
                        entry.name == "manifest.json" -> {
                            val buffer = ByteArrayOutputStream()
                            zip.copyTo(buffer, 16 * 1024)
                            if (buffer.size() > 2 * 1024 * 1024) error("Backup manifest is too large.")
                            manifestBytes = buffer.toByteArray()
                        }
                        entry.name.startsWith("locked/") &&
                            entry.name.matches(Regex("locked/[0-9a-fA-F-]{36}\\.bin")) -> {
                            val id = entry.name.removePrefix("locked/").removeSuffix(".bin")
                            val stagedFile = File(staging, "$id.bin")
                            stagedFile.outputStream().buffered().use { output ->
                                val buffer = ByteArray(64 * 1024)
                                while (true) {
                                    val count = zip.read(buffer)
                                    if (count <= 0) break
                                    restoredBytes += count
                                    if (restoredBytes > maximumArchiveBytes) error("Backup exceeds the size limit.")
                                    output.write(buffer, 0, count)
                                }
                            }
                        }
                        else -> {
                            // Ignore unrecognised entries; paths are never extracted directly.
                            while (zip.read() != -1) Unit
                        }
                    }
                    zip.closeEntry()
                }
            }

            val rawManifest = manifestBytes ?: error("This is not a valid Angllary backup.")
            val manifest = JSONArray(String(rawManifest, Charsets.UTF_8))
            val recovered = ArrayList<LockedEntry>()
            for (index in 0 until manifest.length()) {
                val json = manifest.getJSONObject(index)
                val id = json.getString("id")
                if (!id.matches(Regex("[0-9a-fA-F-]{36}"))) continue
                val stagedFile = File(staging, "$id.bin")
                if (!stagedFile.isFile) continue
                recovered += LockedEntry(
                    id = id,
                    displayName = json.optString("name", "Private media"),
                    mimeType = json.optString("mime", "application/octet-stream"),
                    encryptedSize = json.optLong("size", stagedFile.length()),
                    iv = json.getString("iv")
                )
            }
            if (recovered.isEmpty() && manifest.length() > 0) {
                error("No valid encrypted media was found in this backup.")
            }

            recovered.forEach { entry ->
                stagedFileFor(staging, entry.id).copyTo(File(directory, entry.id + ".bin"), overwrite = true)
            }
            val replacedIds = recovered.map { it.id }.toSet()
            saveEntries(entries().filterNot { it.id in replacedIds } + recovered)
            return recovered.size
        } finally {
            staging.deleteRecursively()
        }
    }

    private fun stagedFileFor(staging: File, id: String): File = File(staging, "$id.bin")

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
