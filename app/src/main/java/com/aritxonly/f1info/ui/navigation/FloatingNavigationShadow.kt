package com.aritxonly.f1info.ui.navigation

// Synced from Deadliner 5499df09383e84a120e19bd0c629c6e6ffa7ef0d + local working changes (2026-10-20).
// Integration: package and MIUIX semantic UI bindings; see docs/UPSTREAM.md.

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.aritxonly.f1info.ui.material.glass.SoftGlassShadowTokens
import com.aritxonly.f1info.ui.material.glass.softGlassShadow
import com.aritxonly.f1info.ui.material.glass.softGlassShadowTokens

internal fun floatingNavigationShadowTokens(isDark: Boolean): SoftGlassShadowTokens =
    softGlassShadowTokens(isDark)

@Composable
internal fun Modifier.floatingNavigationShadow(
    shape: Shape,
    isDark: Boolean,
): Modifier {
    return softGlassShadow(
        shape = shape,
        isDark = isDark,
    )
}
