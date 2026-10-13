package com.aritxonly.f1info.model

// Migrated from Deadliner 210e4a2d4c6bf7df291b6c6852d39366bc1fee17.
// Integration changes: package and MIUIX semantic UI bindings only.

import kotlin.math.abs

object AdvancedMaterialTuning {
    const val MinBlurRadius = 28f
    const val DefaultBlurRadius = 92f
    const val MaxBlurRadius = 256f
    fun normalizeBlurRadius(value: Float): Float = if (value.isFinite()) {
        value.coerceIn(MinBlurRadius, MaxBlurRadius)
    } else {
        DefaultBlurRadius
    }

    fun saturationForBlurRadius(value: Float): Float {
        val radius = normalizeBlurRadius(value)
        val levels = AdvancedMaterialLevel.entries
        val upperIndex = levels.indexOfFirst { radius <= it.blurRadius }
        if (upperIndex <= 0) return levels.first().blurSaturation

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
