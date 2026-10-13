package com.aritxonly.f1info.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aritxonly.f1info.R
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val logos = mapOf(
    "mercedes" to R.drawable.team_mercedes, "ferrari" to R.drawable.team_ferrari,
    "red_bull" to R.drawable.team_red_bull, "mclaren" to R.drawable.team_mclaren,
    "rb" to R.drawable.team_rb, "racing_bulls" to R.drawable.team_rb,
    "alpine" to R.drawable.team_alpine, "haas" to R.drawable.team_haas,
    "williams" to R.drawable.team_williams, "audi" to R.drawable.team_audi,
    "cadillac" to R.drawable.team_cadillac, "aston_martin" to R.drawable.team_aston_martin)
private val cars = mapOf(
    "mercedes" to R.drawable.car_2026_mercedes, "ferrari" to R.drawable.car_2026_ferrari,
    "red_bull" to R.drawable.car_2026_red_bull, "mclaren" to R.drawable.car_2026_mclaren,
    "rb" to R.drawable.car_2026_rb, "racing_bulls" to R.drawable.car_2026_rb,
    "alpine" to R.drawable.car_2026_alpine, "haas" to R.drawable.car_2026_haas,
    "williams" to R.drawable.car_2026_williams, "audi" to R.drawable.car_2026_audi,
    "cadillac" to R.drawable.car_2026_cadillac, "aston_martin" to R.drawable.car_2026_aston_martin)

internal val vectorLogos = mapOf(
    "mercedes" to R.drawable.logo_vector_mercedes, "ferrari" to R.drawable.logo_vector_ferrari,
    "mclaren" to R.drawable.logo_vector_mclaren, "audi" to R.drawable.logo_vector_audi,
    "aston_martin" to R.drawable.logo_vector_aston_martin, "red_bull" to R.drawable.logo_vector_red_bull,
    "cadillac" to R.drawable.logo_vector_cadillac, "alpine" to R.drawable.logo_vector_alpine)

@Composable
fun TeamLogo(id: String?, modifier: Modifier = Modifier) {
    vectorLogos[id]?.let { resource ->
        Image(painterResource(resource), "车队 Logo", modifier,
            colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface), contentScale = ContentScale.Fit)
        return
    }
    val bitmap by rememberRacingArtwork(if (id == "racing_bulls") "rb" else id, "logo", logos[id])
    bitmap?.let { Image(androidx.compose.ui.graphics.painter.BitmapPainter(it.asImageBitmap()), "车队 Logo", modifier,
        colorFilter = if (id == "ferrari") null else ColorFilter.tint(MiuixTheme.colorScheme.onSurface), contentScale = ContentScale.Fit) }

}

/** Official 2026 transparent renders, used unchanged with bundled offline fallback. */
@Composable
fun TeamCarVisual(id: String?, season: Int?, modifier: Modifier = Modifier) {
    val car = if (season == 2026) cars[id] else null
    val bitmap by rememberRacingArtwork((if (id == "racing_bulls") "rb" else id).takeIf { season == 2026 }, "car", car)
    Box(modifier) {
        if (bitmap != null) Image(androidx.compose.ui.graphics.painter.BitmapPainter(bitmap!!.asImageBitmap()), "${season} 赛季赛车", Modifier.fillMaxSize().padding(top = 30.dp),
            contentScale = ContentScale.Fit)
        else if (car == null) Text("${season ?: "—"} 赛车图片待收录", style = MiuixTheme.textStyles.footnote2,
            modifier = Modifier.align(Alignment.Center), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        TeamLogo(id, Modifier.align(Alignment.TopEnd).width(80.dp).height(44.dp))
    }
}
