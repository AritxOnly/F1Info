package com.aritxonly.f1info.ui.components

import com.aritxonly.f1info.data.driverArtworkId
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.aritxonly.f1info.R
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val numberResources = mapOf(
    "sainz" to R.drawable.number_sainz,
    "bottas" to R.drawable.number_bottas,
    "bearman" to R.drawable.number_bearman,
    "max_verstappen" to R.drawable.number_max_verstappen,
    "perez" to R.drawable.number_perez,
    "colapinto" to R.drawable.number_colapinto,
    "tsunoda" to R.drawable.number_tsunoda,
    "lindblad" to R.drawable.number_lindblad,
    "alonso" to R.drawable.number_alonso,
    "ocon" to R.drawable.number_ocon,
    "leclerc" to R.drawable.number_leclerc,
    "albon" to R.drawable.number_albon,
    "norris" to R.drawable.number_norris,
    "gasly" to R.drawable.number_gasly,
    "antonelli" to R.drawable.number_antonelli,
    "hadjar" to R.drawable.number_hadjar,
    "lawson" to R.drawable.number_lawson,
    "piastri" to R.drawable.number_piastri,
    "hulkenberg" to R.drawable.number_hulkenberg,
    "russell" to R.drawable.number_russell,
    "stroll" to R.drawable.number_stroll,
    "bortoleto" to R.drawable.number_bortoleto,
    "hamilton" to R.drawable.number_hamilton,
)

/** Official driver number artwork, no imitation of personal lettering. */
@Composable
fun DriverNumberIcon(id: String, number: String, modifier: Modifier = Modifier) {
    val bitmap by rememberRacingArtwork(id, "number", numberResources[driverArtworkId(id)])
    if (bitmap != null) Image(BitmapPainter(bitmap!!.asImageBitmap()), "车号 $number", modifier, contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface))
    else Text(number, modifier = modifier, style = MiuixTheme.textStyles.headline1)

}
