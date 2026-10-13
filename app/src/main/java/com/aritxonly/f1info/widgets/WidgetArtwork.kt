package com.aritxonly.f1info.widgets

import com.aritxonly.f1info.data.driverArtworkId
import android.content.Context
import android.graphics.*
import androidx.compose.ui.graphics.toArgb
import com.aritxonly.f1info.R
import com.aritxonly.f1info.model.Circuit
import com.aritxonly.f1info.ui.components.circuitNames
import com.aritxonly.f1info.ui.components.portraitResources
import org.json.JSONObject
import kotlin.math.cos
import kotlin.math.min

/** Render vector track geometry and bundled art into small RemoteViews-compatible bitmaps. */
internal object WidgetArtwork {
    private var geometry: JSONObject? = null
    private val teamLogos = mapOf("mercedes" to R.drawable.team_mercedes, "ferrari" to R.drawable.team_ferrari,
        "red_bull" to R.drawable.team_red_bull, "mclaren" to R.drawable.team_mclaren,
        "rb" to R.drawable.team_rb, "racing_bulls" to R.drawable.team_rb, "alpine" to R.drawable.team_alpine,
        "haas" to R.drawable.team_haas, "audi" to R.drawable.team_audi, "cadillac" to R.drawable.team_cadillac,
        "aston_martin" to R.drawable.team_aston_martin, "williams" to R.drawable.team_williams)

    @Synchronized
    fun circuit(context: Context, circuit: Circuit, accent: Int): Bitmap? = runCatching {
        val features = (geometry ?: JSONObject(context.assets.open("circuits.geojson").bufferedReader().use { it.readText() })
            .also { geometry = it }).getJSONArray("features")
        val feature = (0 until features.length()).map { features.getJSONObject(it) }.firstOrNull {
            it.getJSONObject("properties").getString("Name") == (circuitNames[circuit.id] ?: circuit.name)
        } ?: return@runCatching null
        val coordinates = feature.getJSONObject("geometry").getJSONArray("coordinates")
        val scaleLongitude = cos(Math.toRadians(circuit.latitude ?: 0.0))
        val points = (0 until coordinates.length()).map { coordinates.getJSONArray(it) }.map { it.getDouble(0) * scaleLongitude to -it.getDouble(1) }
        val minX = points.minOf { it.first }; val minY = points.minOf { it.second }
        val width = points.maxOf { it.first } - minX; val height = points.maxOf { it.second } - minY
        if (width <= 0 || height <= 0) return@runCatching null
        val scale = min(280.0 / width, 210.0 / height)
        val x = (320 - width * scale) / 2; val y = (250 - height * scale) / 2
        val path = Path().apply { points.forEachIndexed { index, p ->
            val px = (x + (p.first - minX) * scale).toFloat(); val py = (y + (p.second - minY) * scale).toFloat()
            if (index == 0) moveTo(px, py) else lineTo(px, py)
        } }
        Bitmap.createBitmap(320, 250, Bitmap.Config.ARGB_8888).also { bitmap ->
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent; style = Paint.Style.STROKE; strokeJoin = Paint.Join.ROUND; strokeCap = Paint.Cap.ROUND }
            paint.alpha = 45; paint.strokeWidth = 22f; canvas.drawPath(path, paint)
            paint.alpha = 255; paint.strokeWidth = 7f; canvas.drawPath(path, paint)
        }
    }.getOrNull()

    fun portrait(context: Context, id: String): Bitmap? {
        val source = com.aritxonly.f1info.data.ArtworkCache.cachedPortrait(context, id)
            ?: portraitResources[driverArtworkId(id)]?.let { BitmapFactory.decodeResource(context.resources, it) } ?: return null
        return Bitmap.createBitmap(280, 320, Bitmap.Config.ARGB_8888).also { bitmap ->
            val canvas = Canvas(bitmap); val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            val ratio = min(280f / source.width, 320f / source.height)
            val width = source.width * ratio; val height = source.height * ratio
            val left = (280 - width) / 2; val top = 8f
            canvas.drawBitmap(source, null, RectF(left, top, left + width, top + height), paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            paint.shader = LinearGradient(0f, height * .5f, 0f, height * .94f, Color.WHITE, Color.TRANSPARENT, Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, 280f, 320f, paint)
        }
    }

    fun teamLogo(context: Context, id: String, foreground: Int): Bitmap? {
        val vector = com.aritxonly.f1info.ui.components.vectorLogos[id]
        if (vector != null) {
            val drawable = runCatching { context.getDrawable(vector)?.mutate() }.getOrNull()
                ?: return bitmapLogo(context, id, foreground)
            return Bitmap.createBitmap(560, 400, Bitmap.Config.ARGB_8888).also { bitmap ->
                drawable.setTint(foreground)
                val ratio = min(480f / drawable.intrinsicWidth, 340f / drawable.intrinsicHeight)
                val width = (drawable.intrinsicWidth * ratio).toInt()
                val height = (drawable.intrinsicHeight * ratio).toInt()
                drawable.setBounds((560 - width) / 2, (400 - height) / 2, (560 + width) / 2, (400 + height) / 2)
                drawable.draw(Canvas(bitmap))
            }
        }
        return bitmapLogo(context, id, foreground)
    }

    private fun bitmapLogo(context: Context, id: String, foreground: Int): Bitmap? = teamLogos[id]?.let { resource ->
        val source = BitmapFactory.decodeResource(context.resources, resource) ?: return@let null
        Bitmap.createBitmap(280, 200, Bitmap.Config.ARGB_8888).also { bitmap ->
            val ratio = min(240f / source.width, 170f / source.height)
            val width = source.width * ratio; val height = source.height * ratio
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                colorFilter = if (id == "ferrari") null else PorterDuffColorFilter(foreground, PorterDuff.Mode.SRC_IN)
            }
            Canvas(bitmap).drawBitmap(source, null, RectF((280-width)/2, (200-height)/2, (280+width)/2, (200+height)/2), paint)
        }
    }
}
