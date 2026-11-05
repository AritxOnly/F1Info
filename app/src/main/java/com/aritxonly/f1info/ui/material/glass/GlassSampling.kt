package com.aritxonly.f1info.ui.material.glass

// Synced from Deadliner 5499df09383e84a120e19bd0c629c6e6ffa7ef0d + local working changes (2026-10-20).
// Integration: package and MIUIX semantic UI bindings; see docs/UPSTREAM.md.

/** Three Gaussian sigmas plus the lens displacement and final softening kernel, in pixels. */
internal fun glassEffectPadding(
    radiusX: Float,
    radiusY: Float,
    postRadius: Float,
    displacement: Float,
    dispersion: Float,
): Float = maxOf(radiusX, radiusY, 0f) * 3f +
    displacement.coerceAtLeast(0f) + dispersion.coerceAtLeast(0f) +
    postRadius.coerceAtLeast(0f) * 3f
