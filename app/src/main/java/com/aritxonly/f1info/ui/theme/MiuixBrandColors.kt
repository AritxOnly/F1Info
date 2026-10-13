package com.aritxonly.f1info.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import top.yukonga.miuix.kmp.theme.Colors

/** MIUIX's original neutral palette with explicit team-brand overrides for accent roles. */
fun miuixBrandColors(base: Colors, brand: Color): Colors {
    val dark = base.surface.luminance() < .5f
    val primary = readableBrand(brand, base.surface, dark)
    val onPrimary = if (contrast(base.onSurface, primary) >= contrast(base.surface, primary)) base.onSurface else base.surface
    val tertiary = lerp(base.surfaceContainer, primary, if (dark) .18f else .10f)
    val disabled = lerp(base.surfaceContainer, primary, .30f)
    return base.copy(
        primary = primary, onPrimary = onPrimary,
        primaryVariant = primary, onPrimaryVariant = onPrimary,
        primaryContainer = primary, onPrimaryContainer = onPrimary,
        tertiaryContainer = tertiary, onTertiaryContainer = primary, tertiaryContainerVariant = tertiary,
        disabledPrimary = disabled, disabledOnPrimary = base.disabledOnSurface,
        disabledPrimaryButton = disabled, disabledOnPrimaryButton = base.disabledOnSurface,
        disabledPrimarySlider = disabled,
    )
}

private fun readableBrand(brand: Color, surface: Color, dark: Boolean): Color {
    if (contrast(brand, surface) >= 4.5) return brand
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(brand.toArgb(), hsl)
    var color = brand
    repeat(100) {
        hsl[2] = (hsl[2] + if (dark) .01f else -.01f).coerceIn(0f, 1f)
        color = Color(ColorUtils.HSLToColor(hsl))
        if (contrast(color, surface) >= 4.5) return color
    }
    return color
}

private fun contrast(first: Color, second: Color): Double {
    val a = first.luminance(); val b = second.luminance()
    return ((maxOf(a, b) + .05f) / (minOf(a, b) + .05f)).toDouble()
}
