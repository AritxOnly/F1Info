package com.aritxonly.f1info.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.*
import com.aritxonly.f1info.R

/** Oxanium OFL: angular display glyphs, Chinese falls back to the system font. */
@OptIn(ExperimentalTextApi::class)
val RacingDisplayFamily = FontFamily(
    Font(R.font.racing_oxanium, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.racing_oxanium, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
)

@OptIn(ExperimentalTextApi::class)
val RacingScoreFamily = FontFamily(
    Font(R.font.racing_oxanium, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(650))),
)
