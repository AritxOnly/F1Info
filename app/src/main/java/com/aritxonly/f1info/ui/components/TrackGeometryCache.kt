package com.aritxonly.f1info.ui.components

import android.content.Context
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** The bundled geometry is parsed once off the main thread, shared by all visible track rows. */
object TrackGeometryCache {
    @Volatile private var all: Map<String, TrackGeometry>? = null
    private val lock = Any()
    fun peek(name: String): TrackGeometry? = all?.get(name)
    suspend fun load(context: Context): Map<String, TrackGeometry> = withContext(Dispatchers.IO) {
        all ?: synchronized(lock) { all ?: run {
            val features = JSONObject(context.assets.open("circuits.geojson").bufferedReader().use { it.readText() }).getJSONArray("features")
            (0 until features.length()).associate { i ->
                val feature = features.getJSONObject(i); val props = feature.getJSONObject("properties")
                val raw = feature.getJSONObject("geometry").getJSONArray("coordinates")
                props.getString("Name") to TrackGeometry((0 until raw.length()).map { n -> raw.getJSONArray(n).let { Offset(it.getDouble(0).toFloat(), it.getDouble(1).toFloat()) } },
                    props.optInt("length").takeIf { it > 0 }, props.optInt("firstgp").takeIf { it > 0 })
            }.also { all = it }
        } }
    }
}
