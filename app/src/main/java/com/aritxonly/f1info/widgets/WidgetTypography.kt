package com.aritxonly.f1info.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.glance.*
import androidx.glance.layout.*
import com.aritxonly.f1info.R

/** RemoteViews cannot load packaged font resources into TextView; render only display numbers. */
internal object WidgetDisplayNumbers {
    private val cache = LruCache<String, Bitmap>(32)
    private var font: Typeface? = null

    @Synchronized
    fun render(context: Context, text: String, color: Int, sizeSp: Int, maxWidthDp: Float): Bitmap {
        val metrics = context.resources.displayMetrics
        val fontScale = context.resources.configuration.fontScale
        val density = metrics.density.coerceAtLeast(1f)
        val key = "$text:$color:$sizeSp:$maxWidthDp:$density:$fontScale"
        cache.get(key)?.let { return it }
        val typeface = font ?: requireNotNull(ResourcesCompat.getFont(context, R.font.racing_oxanium)).also { font = it }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.typeface = typeface
            fontVariationSettings = "'wght' 650"
            textSize = sizeSp * fontScale * density
        }
        val limit = maxWidthDp.coerceAtLeast(12f) * density
        val measured = paint.measureText(text).coerceAtLeast(1f)
        if (measured > limit) paint.textSize *= limit / measured
        val fm = paint.fontMetrics
        val width = kotlin.math.ceil(paint.measureText(text).toDouble()).toInt() + 2
        val height = kotlin.math.ceil((fm.descent - fm.ascent).toDouble()).toInt() + 2
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
            Canvas(it).drawText(text, 1f, 1f - fm.ascent, paint)
            cache.put(key, it)
        }
    }
}

@Composable
internal fun WidgetScore(text: String, color: Color, sizeSp: Int, maxWidthDp: Float) {
    val context = LocalContext.current
    val scale = context.resources.configuration.fontScale
    val density = context.resources.displayMetrics.density
    val bitmap = remember(text, color, sizeSp, maxWidthDp, scale, density) {
        WidgetDisplayNumbers.render(context, text, color.toArgb(), sizeSp, maxWidthDp)
    }
    Image(ImageProvider(bitmap), "$text 积分", GlanceModifier.width((bitmap.width / density).dp)
        .height((bitmap.height / density).dp), contentScale = ContentScale.Fit)
}
