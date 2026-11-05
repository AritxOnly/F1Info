package com.aritxonly.f1info.ui.components

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.core.graphics.ColorUtils
import top.yukonga.miuix.kmp.theme.MiuixTheme

val LocalRacingAccent = compositionLocalOf<State<Color>> { mutableStateOf(Color.Unspecified) }


// Snapshot team_colour from OpenF1 latest drivers, retrieved 2026-10-11.
private val teamColors = mapOf(
    "mclaren" to 0xFFF47600, "red_bull" to 0xFF4781D7, "audi" to 0xFFF50537,
    "alpine" to 0xFF00A1E8, "cadillac" to 0xFF909090, "mercedes" to 0xFF00D7B6,
    "aston_martin" to 0xFF229971, "ferrari" to 0xFFED1131, "williams" to 0xFF1868DB,
    "rb" to 0xFF6C98FF, "racing_bulls" to 0xFF6C98FF, "haas" to 0xFF9C9FA2)

@Composable
fun teamBrandColor(id: String?): Color = teamBrandTint(id) ?: MiuixTheme.colorScheme.primary

internal fun teamBrandTint(id: String?): Color? = teamColors[id]?.let { Color(it) }

/** Directly adapted from Deadliner's AboutSettings floating-glow backdrop. */
@Composable
fun RacingFloatingBackground(modifier: Modifier = Modifier, alpha: Float = 1f, accent: State<Color> = LocalRacingAccent.current) {
    val transition = rememberInfiniteTransition(label = "aboutFloatingBackground")
    val horizontalOffset = transition.animateFloat(-0.12f, 0.12f, infiniteRepeatable(tween(7_500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "aboutBackgroundHorizontalOffset")
    val verticalOffset = transition.animateFloat(0.08f, -0.08f, infiniteRepeatable(tween(5_600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "aboutBackgroundVerticalOffset")
    val accentOffset = transition.animateFloat(-0.08f, 0.1f, infiniteRepeatable(tween(6_400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "aboutBackgroundAccentOffset")
    val surface = MiuixTheme.colorScheme.surface
    androidx.compose.foundation.layout.Box(modifier.then(Modifier.drawWithCache {
        val glow = accent.value.toVividGlowColor()
        val radius = maxOf(size.width, size.height) * .9f
        val brushes = listOf(.22f to 1f, .18f to .86f, .16f to .72f).map { (opacity, scale) ->
            Brush.radialGradient(listOf(glow.copy(alpha = opacity * alpha), Color.Transparent), Offset.Zero, radius * scale)
        }
        val fade = Brush.verticalGradient(0f to Color.Transparent, .42f to Color.Transparent, 1f to surface)
        onDrawBehind {
        drawRect(surface)
        if (alpha <= 0f) return@onDrawBehind
        fun center(x: Float, y: Float) = Offset(size.width * x, size.height * y)
        val first = center(0.18f + horizontalOffset.value, 0.22f + verticalOffset.value)
        val second = center(0.86f - horizontalOffset.value, 0.66f - verticalOffset.value)
        val third = center(0.52f + accentOffset.value, 0.96f - accentOffset.value)
        listOf(first to 1f, second to .86f, third to .72f).forEachIndexed { index, (offset, scale) ->
            withTransform({ translate(offset.x, offset.y) }) { drawCircle(brushes[index], radius * scale, Offset.Zero) }
        }
        drawRect(fade)
        }
    }))
}

private fun Color.toVividGlowColor(): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(toArgb(), hsl)
    hsl[1] = (hsl[1] * 1.55f).coerceIn(0.56f, 0.92f)
    hsl[2] = if (hsl[2] < 0.5f) (hsl[2] + 0.14f).coerceAtMost(0.68f) else (hsl[2] - 0.06f).coerceAtLeast(0.36f)
    return Color(ColorUtils.HSLToColor(hsl))
}
