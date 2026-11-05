package com.aritxonly.f1info.ui.material.glass

// Synced from Deadliner 5499df09383e84a120e19bd0c629c6e6ffa7ef0d + local working changes (2026-10-20).
// Integration: package and MIUIX semantic UI bindings; see docs/UPSTREAM.md.

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

/** Keep only the thin luminous rim; body contrast is handled separately. */
internal fun Modifier.glassBoundary(
    shape: Shape,
    spec: GlassEdgeOpticsSpec,
    isDark: Boolean,
    optical: Boolean,
): Modifier {
    val width = spec.edgeStrokeWidth(isDark)
    if (width.value <= 0f) return this
    return border(width, spec.edgeBrush(isDark, optical), shape)
}
