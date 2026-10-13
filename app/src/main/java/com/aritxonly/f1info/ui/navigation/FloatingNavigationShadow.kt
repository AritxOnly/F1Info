package com.aritxonly.f1info.ui.navigation

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.aritxonly.f1info.ui.material.glass.SoftGlassShadowTokens
import com.aritxonly.f1info.ui.material.glass.softGlassShadow
import com.aritxonly.f1info.ui.material.glass.softGlassShadowTokens

internal fun floatingNavigationShadowTokens(isDark: Boolean): SoftGlassShadowTokens =
    softGlassShadowTokens(isDark)

internal fun Modifier.floatingNavigationShadow(
    shape: Shape,
    isDark: Boolean,
): Modifier {
    return softGlassShadow(
        shape = shape,
        isDark = isDark,
    )
}
