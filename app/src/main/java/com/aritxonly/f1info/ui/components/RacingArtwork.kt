package com.aritxonly.f1info.ui.components

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.aritxonly.f1info.data.ArtworkCache

/** No resource decode or disk/network I/O in composition. Warm bitmaps render immediately. */
@Composable
fun rememberRacingArtwork(id: String?, kind: String, fallback: Int?): State<Bitmap?> {
    val context = LocalContext.current.applicationContext
    return key(id, kind, fallback) { produceState(initialValue = id?.let { ArtworkCache.peek(it, kind) } ?: fallback?.let { ArtworkCache.peekBundled(it) }, id, kind, fallback) {
        if (value == null && fallback != null) value = ArtworkCache.bundled(context, fallback)
        if (id != null) {
            val online = when (kind) { "car" -> ArtworkCache.car(context, id); "logo" -> ArtworkCache.logo(context, id); "number" -> ArtworkCache.number(context, id); else -> ArtworkCache.portrait(context, id) }
            if (online != null) value = online
        }
    } }
}
