package com.aritxonly.f1info.model

// Synced from Deadliner 5499df0 + local working changes, 2026-10-20.
// AppearancePreferences mutation entry points omitted; F1Info owns its appearance state.

import kotlin.math.abs

object AdvancedMaterialTuning {
    const val MinBlurRadius = 16f
    const val DefaultBlurRadius = 84f
    const val MaxBlurRadius = 168f
    fun normalizeBlurRadius(value: Float): Float = if (value.isFinite()) {
        value.coerceIn(MinBlurRadius, MaxBlurRadius)
    } else {
        DefaultBlurRadius
    }

    fun saturationForBlurRadius(value: Float): Float {
        val radius = normalizeBlurRadius(value)
        val levels = AdvancedMaterialLevel.entries
        val upperIndex = levels.indexOfFirst { radius <= it.blurRadius }
        if (upperIndex < 0) return levels.last().blurSaturation
        if (upperIndex == 0) return levels.first().blurSaturation

        val upper = levels[upperIndex]
        val lower = levels[upperIndex - 1]
        val fraction = (radius - lower.blurRadius) / (upper.blurRadius - lower.blurRadius)
        return lower.blurSaturation + (upper.blurSaturation - lower.blurSaturation) * fraction
    }

    fun nearestLevel(value: Float): AdvancedMaterialLevel {
        val radius = normalizeBlurRadius(value)
        return AdvancedMaterialLevel.entries.minBy { level -> abs(level.blurRadius - radius) }
    }

}
