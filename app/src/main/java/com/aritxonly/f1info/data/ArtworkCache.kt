package com.aritxonly.f1info.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/** Official artwork only. URL keys invalidate changed sources; bundled HD art is the offline fallback. */
object ArtworkCache {
    private val memory = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }
    private val identities = ConcurrentHashMap<String, String>()
    fun peek(id: String, kind: String): Bitmap? = identities["$kind/${if (kind == "portrait" || kind == "number") driverArtworkId(id) else id}"]?.let { memory.get(it) }
    fun peekBundled(resource: Int): Bitmap? = memory.get("bundle:$resource")
    suspend fun bundled(context: Context, resource: Int): Bitmap? = withContext(Dispatchers.IO) {
        val key = "bundle:$resource"
        locks.getOrPut(key) { Mutex() }.withLock {
            memory.get(key) ?: BitmapFactory.decodeResource(context.resources, resource, BitmapFactory.Options().apply { inScaled = false })?.also { it.prepareToDraw(); memory.put(key, it) }
        }
    }
    private val locks = ConcurrentHashMap<String, Mutex>()
    private val retries = ConcurrentHashMap<String, Long>()
    private val sources = mutableMapOf<String, Map<String, String>>()
    @Synchronized private fun url(context: Context, id: String, kind: String): String? {
        val manifest = sources.getOrPut(kind) { runCatching {
            val json = JSONObject(context.assets.open(when (kind) { "car" -> "team-car-sources.json"; "logo" -> "team-logo-sources.json"; "number" -> "driver-number-sources.json"; else -> "portrait-sources.json" }).bufferedReader().use { it.readText() })
            json.keys().asSequence().associateWith { if (kind == "car") json.getJSONObject(it).getString("url") else json.getString(it) }
        }.getOrDefault(emptyMap()) }
        return manifest[if (kind == "portrait" || kind == "number") driverArtworkId(id) else id]
    }
    private fun key(url: String) = MessageDigest.getInstance("SHA-256").digest(url.toByteArray())
        .joinToString("") { "%02x".format(it) }
    private fun directory(context: Context) = File(context.cacheDir, "official-artwork-v1").apply { mkdirs() }

    /** Widget rendering reads the same disk cache without requiring network availability. */
    fun cachedPortrait(context: Context, id: String): Bitmap? {
        val source = url(context, id, "portrait") ?: return null
        val key = key(source)
        return memory.get(key) ?: BitmapFactory.decodeFile(File(directory(context), key).path)?.also { memory.put(key, it) }
    }

    suspend fun portrait(context: Context, id: String): Bitmap? = load(context, id, "portrait")
    suspend fun car(context: Context, id: String): Bitmap? = load(context, id, "car")
    suspend fun logo(context: Context, id: String): Bitmap? = load(context, id, "logo")
    suspend fun number(context: Context, id: String): Bitmap? = load(context, id, "number")

    private suspend fun load(context: Context, id: String, kind: String): Bitmap? = withContext(Dispatchers.IO) {
        val source = url(context, id, kind) ?: return@withContext null
        val key = key(source)
        identities["$kind/${if (kind == "portrait" || kind == "number") driverArtworkId(id) else id}"] = key
        locks.getOrPut(key) { Mutex() }.withLock {
            val directory = directory(context)
            val file = File(directory, key)
            val cached = memory.get(key) ?: BitmapFactory.decodeFile(file.path)?.also { memory.put(key, it) }
            val now = System.currentTimeMillis()
            if (cached != null && now - file.lastModified() < 7L * 24 * 60 * 60 * 1000) {
                android.util.Log.d("F1Artwork", "$kind/$id cache hit ${cached.width}x${cached.height}")
                return@withLock cached
            }
            if (now < (retries[key] ?: 0)) return@withLock cached
            val downloaded = runCatching {
                val connection = URI(source).toURL().openConnection() as HttpURLConnection
                connection.connectTimeout = 8000; connection.readTimeout = 12000
                try {
                    check(connection.responseCode == 200)
                    val bytes = connection.inputStream.use { it.readNBytes(8 * 1024 * 1024 + 1) }
                    check(bytes.size <= 8 * 1024 * 1024)
                    val bitmap = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                    check(bitmap.width >= (if (kind == "logo" || kind == "number") 128 else 512) && bitmap.height >= (if (kind == "logo" || kind == "number") 32 else 128))
                    val temporary = File(directory, "$key.tmp")
                    temporary.writeBytes(bytes)
                    check(temporary.renameTo(file))
                    bitmap.prepareToDraw()
                    memory.put(key, bitmap)
                    android.util.Log.d("F1Artwork", "$kind/$id cached ${bitmap.width}x${bitmap.height}")
                    directory.listFiles()?.filter { !it.name.endsWith(".tmp") }?.sortedByDescending { it.lastModified() }
                        ?.let { files -> var total = 0L; files.forEach { f -> total += f.length(); if (total > 64L * 1024 * 1024 && f != file) f.delete() } }
                    bitmap
                } finally { connection.disconnect() }
            }.getOrNull()
            if (downloaded == null) retries[key] = now + 60_000
            downloaded ?: cached
        }
    }
}
