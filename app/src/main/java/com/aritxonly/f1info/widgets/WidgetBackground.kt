package com.aritxonly.f1info.widgets

import android.graphics.*
import androidx.compose.ui.graphics.toArgb
import top.yukonga.miuix.kmp.theme.Colors

// Dual compatibility: HyperOS soft-light glass with subtle vertical depth and ambient corner glow vs AOSP native Material 3 clean surface.
internal object WidgetBackground {
    fun render(
        colors: Colors,
        accent: androidx.compose.ui.graphics.Color = colors.primary,
        themeStyle: WidgetThemeStyle = WidgetThemeStyle.HyperOS,
        layout: WidgetLayout = WidgetLayout.Compact
    ): Bitmap {
        // Generate bitmap matching actual widget aspect ratio:
        // Wide (4x2) -> 480x240 (2:1 ratio, prevents horizontal stretching distortion)
        // Compact (2x2) -> 240x240 (1:1 ratio)
        // Expanded (4x4) -> 360x360 (1:1 ratio)
        val (width, height) = when (layout) {
            WidgetLayout.Wide -> 480 to 240
            WidgetLayout.Expanded -> 360 to 360
            WidgetLayout.Compact -> 240 to 240
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val baseColor = colors.surfaceContainer.toArgb()
        canvas.drawColor(baseColor)

        if (themeStyle == WidgetThemeStyle.HyperOS) {
            // Soft-light vertical depth gradient (HyperOS glass simulation)
            paint.shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                baseColor,
                colors.surfaceContainer.copy(alpha = 0.88f).toArgb(),
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            // Ambient corner glow (HyperOS dynamic lighting, dynamically anchored to top-right corner)
            val glowRadius = minOf(width, height) * 0.72f
            paint.shader = RadialGradient(
                width * 1.04f, -height * 0.08f, glowRadius,
                colors.onSurface.copy(alpha = 0.04f).toArgb(),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        } else {
            // AOSP Native Material 3 Style: Clean tonal surface, no highlight stroke
            paint.shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                baseColor,
                baseColor,
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }

        bitmap.setHasAlpha(true)
        return bitmap
    }
}
