// Adapted from Deadliner; canonical adaptive soft-glass behavior.
package com.aritxonly.f1info.ui.base

import top.yukonga.miuix.kmp.basic.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.aritxonly.f1info.ui.material.glass.LocalProgressiveTopBarMaterialHosted
import com.aritxonly.f1info.ui.material.glass.LocalTopBarButtonMaterialProgress
import com.aritxonly.f1info.ui.material.glass.SoftGlassIconButton

internal val LocalTopBarButtonMaterialHosted = staticCompositionLocalOf { false }

/** Uses a circular soft-glass host inside a progressive top bar. */
@Composable
fun TopBarIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (LocalProgressiveTopBarMaterialHosted.current && !LocalTopBarButtonMaterialHosted.current) {
        SoftGlassIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            materialAlpha = LocalTopBarButtonMaterialProgress.current,
            content = content,
        )
    } else {
        IconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            content = content,
        )
    }
}
