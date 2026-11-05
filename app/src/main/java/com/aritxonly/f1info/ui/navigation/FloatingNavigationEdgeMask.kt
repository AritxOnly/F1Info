package com.aritxonly.f1info.ui.navigation

// Extracted from Deadliner's ImmersiveMiuixCompactNavigationScaffold.kt at
// 5499df09383e84a120e19bd0c629c6e6ffa7ef0d (2026-10-20).
// Integration: navigation lift is supplied by F1Info's existing system preference.

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aritxonly.f1info.ui.base.edgeFade
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal val FloatingNavigationBottomSpacing: Dp @Composable get() =
    if (com.aritxonly.f1info.ui.base.currentF1WindowLayout().fixedHeader) 0.dp else 4.dp
private val FloatingNavigationEdgeMaskExtraHeight = 32.dp

@Composable
internal fun FloatingNavigationEdgeMask(
    navigationLift: Dp,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val navigationBarHeight = with(density) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    }
    val coverageHeight = MiuixFloatingTabBarDefaults.Height +
        FloatingNavigationBottomSpacing + FloatingNavigationEdgeMaskExtraHeight +
        navigationBarHeight + navigationLift
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(coverageHeight)
            .edgeFade(top = coverageHeight, bottom = 0.dp)
            .background(MiuixTheme.colorScheme.surface),
    )
}
