package com.aritxonly.f1info.ui.components

import com.aritxonly.f1info.data.driverArtworkId
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aritxonly.f1info.model.Circuit
import org.json.JSONObject
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.cos
import kotlin.math.min

internal val circuitNames = mapOf("albert_park" to "Albert Park Circuit", "bahrain" to "Bahrain International Circuit",
    "shanghai" to "Shanghai International Circuit", "catalunya" to "Circuit de Barcelona-Catalunya",
    "monaco" to "Circuit de Monaco", "villeneuve" to "Circuit Gilles-Villeneuve", "red_bull_ring" to "Red Bull Ring",
    "silverstone" to "Silverstone Circuit", "hungaroring" to "Hungaroring", "spa" to "Circuit de Spa-Francorchamps",
    "monza" to "Autodromo Nazionale Monza", "marina_bay" to "Marina Bay Street Circuit", "suzuka" to "Suzuka International Racing Course",
    "americas" to "Circuit of the Americas", "rodriguez" to "Autódromo Hermanos Rodríguez", "interlagos" to "Autódromo José Carlos Pace - Interlagos",
    "yas_marina" to "Yas Marina Circuit", "baku" to "Baku City Circuit", "jeddah" to "Jeddah Corniche Circuit",
    "miami" to "Miami International Autodrome", "zandvoort" to "Circuit Zandvoort", "vegas" to "Las Vegas Street Circuit",
    "losail" to "Losail International Circuit", "imola" to "Autodromo Enzo e Dino Ferrari")
data class TrackGeometry(val points: List<Offset>, val length: Int?, val firstGp: Int?)

@Composable
fun rememberTrack(circuit: Circuit): TrackGeometry? {
    val context = LocalContext.current.applicationContext
    val name = circuitNames[circuit.id] ?: circuit.name
    val geometry by produceState(TrackGeometryCache.peek(name), name) { value = TrackGeometryCache.load(context)[name] }
    return geometry

}

@Composable
fun TrackOutline(circuit: Circuit, modifier: Modifier = Modifier, prominent: Boolean = false) {
    val track = rememberTrack(circuit)
    val colors = MiuixTheme.colorScheme
    val accent = LocalRacingAccent.current.value
    if (track == null) {
        Box(modifier, contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("${circuit.locality}\n赛道图待收录", style = MiuixTheme.textStyles.footnote2,
                color = colors.onSurfaceVariantSummary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
        return
    }
    Canvas(modifier) {
        val projected = track.points.map { Offset(it.x * cos(Math.toRadians(circuit.latitude ?: 0.0)).toFloat(), -it.y) }
        val minX = projected.minOf { it.x }; val minY = projected.minOf { it.y }
        val w = projected.maxOf { it.x } - minX; val h = projected.maxOf { it.y } - minY
        if (w <= 0 || h <= 0) return@Canvas
        // Scale the glow and its reserved space together on compact race cards.
        val prominence = min(1f, size.minDimension / 140.dp.toPx())
        val margin = if (prominent) 22.dp.toPx() * prominence else 5.dp.toPx()
        val scale = min((size.width - margin * 2) / w, (size.height - margin * 2) / h)
        val dx = (size.width - w * scale) / 2; val dy = (size.height - h * scale) / 2
        val path = Path().apply { projected.forEachIndexed { index, p ->
            val x = (p.x - minX) * scale + dx; val y = (p.y - minY) * scale + dy
            if (index == 0) moveTo(x, y) else lineTo(x, y)
        } }
        if (prominent) {
            drawPath(path, accent.copy(alpha = .08f), style = Stroke(22.dp.toPx() * prominence, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(path, accent.copy(alpha = .18f), style = Stroke(12.dp.toPx() * prominence, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        drawPath(path, if (prominent) accent else colors.onSurface, style = Stroke(if (prominent) 5.dp.toPx() * prominence else 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun DriverPortrait(driverId: String, modifier: Modifier = Modifier) {
    val resource = portraitResources[driverArtworkId(driverId)]
    val portrait by rememberRacingArtwork(driverId, "portrait", resource)
    val maskColor = MiuixTheme.colorScheme.onSurface
    if (portrait != null) Image(androidx.compose.ui.graphics.painter.BitmapPainter(portrait!!.asImageBitmap()), contentDescription = null,
        modifier = modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen).drawWithContent {
            drawContent()
            drawRect(Brush.verticalGradient(0f to maskColor, .45f to maskColor, .82f to Color.Transparent, 1f to Color.Transparent), blendMode = BlendMode.DstIn)
        }, contentScale = ContentScale.Fit)
}

@Composable
fun HeroWash(modifier: Modifier = Modifier) {
    val accent = LocalRacingAccent.current.value
    Canvas(modifier) {
        drawRect(Brush.radialGradient(listOf(accent.copy(alpha = .30f), accent.copy(alpha = .08f), Color.Transparent),
            center = Offset(size.width * .9f, size.height * .35f), radius = size.width * 1.2f))
    }
}
