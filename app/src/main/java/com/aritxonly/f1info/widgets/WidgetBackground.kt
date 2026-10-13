package com.aritxonly.f1info.widgets

import android.graphics.*
import androidx.compose.ui.graphics.toArgb
import top.yukonga.miuix.kmp.theme.Colors

// Adapted from Deadliner's TaskWidgetBackgroundRenderer, revision 210e4a2.
// Same opaque bitmap + layered gradients; MIUIX semantic colors replace task palettes.
internal object WidgetBackground {
    fun render(colors: Colors, accent: androidx.compose.ui.graphics.Color = colors.primary): Bitmap {
        val bitmap = Bitmap.createBitmap(240, 240, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(colors.surfaceContainer.toArgb())
        paint.shader = LinearGradient(0f, 0f, 240f, 240f, colors.surfaceContainer.toArgb(),
            colors.surfaceContainer.toArgb(), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, 240f, 240f, paint)
        paint.shader = RadialGradient(249.6f, -19.2f, 172.8f, colors.onSurface.copy(alpha = .035f).toArgb(),
            Color.TRANSPARENT, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, 240f, 240f, paint)
        paint.shader = LinearGradient(0f, 80f, 0f, 240f, Color.TRANSPARENT, colors.surfaceContainer.toArgb(), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, 240f, 240f, paint)
        paint.shader = null

        bitmap.setHasAlpha(false)
        return bitmap
    }
}
