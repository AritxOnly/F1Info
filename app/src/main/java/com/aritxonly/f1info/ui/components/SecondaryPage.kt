package com.aritxonly.f1info.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.aritxonly.f1info.ui.theme.LocalAdvancedMaterialBackdrop
import com.aritxonly.f1info.ui.base.*
import com.aritxonly.f1info.ui.material.glass.*
import com.aritxonly.f1info.ui.navigation.ImmersiveTextTabRow
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.*
import androidx.compose.ui.graphics.luminance
import top.yukonga.miuix.kmp.blur.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*

val LocalSecondaryContentPadding = staticCompositionLocalOf { PaddingValues(0.dp) }

@Composable
fun secondaryContentPadding(extraTop: androidx.compose.ui.unit.Dp = 12.dp, extraBottom: androidx.compose.ui.unit.Dp = 32.dp): PaddingValues {
    val padding = LocalSecondaryContentPadding.current
    return PaddingValues(16.dp, padding.calculateTopPadding() + extraTop, 16.dp, padding.calculateBottomPadding() + extraBottom)
}

/** One active MIUIX scaffold; page pixels recorded separately from adaptive chrome. */
@Composable
fun SecondaryPage(title: String, accent: Color, onBack: () -> Unit,
    tabs: List<String> = emptyList(), selectedTab: Int = 0, onTab: (Int) -> Unit = {},
    brandStyling: Boolean = true,
    content: @Composable () -> Unit) {
    val backdrop = LocalAdvancedMaterialBackdrop.current
    val behavior = key(title) { MiuixScrollBehavior() }
    val progress = behavior.state.collapsedFraction.coerceIn(0f, 1f)
    val buttonProgress by animateFloatAsState(resolveTopBarButtonMaterialProgress(progress, progress, null),
        tween(280, easing = FastOutSlowInEasing), label = "secondary-material")
    val base = MiuixTheme.colorScheme
    val colors = remember(base, accent, brandStyling) { if (brandStyling) com.aritxonly.f1info.ui.theme.miuixBrandColors(base, accent) else base }
    MiuixTheme(colors = colors) {
    CompositionLocalProvider(LocalRacingAccent provides rememberUpdatedState(accent), LocalTopBarScrollProgress provides progress,
        LocalTopBarButtonMaterialProgress provides buttonProgress) {
        Scaffold(containerColor = Color.Transparent, contentWindowInsets = WindowInsets(0, 0, 0, 0), topBar = {
            ProgressiveTopBarMaterial {
                TopAppBar(title = title, color = Color.Transparent, scrollBehavior = behavior,
                    navigationIcon = { TopBarIconButton(onBack) { Icon(MiuixIcons.ChevronBackward, "返回", Modifier.size(24.dp)) } },
                    bottomContent = {
                        if (tabs.isNotEmpty()) Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                            ImmersiveTextTabRow(tabs, selectedTab, onTab)
                        }
                    })
            }
        }) { padding ->
            Box(Modifier.fillMaxSize().then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)) {
                if (brandStyling) RacingFloatingBackground(Modifier.fillMaxSize(), alpha = 1f - progress * .35f, accent = rememberUpdatedState(accent))
                else Box(Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface))
                CompositionLocalProvider(LocalSecondaryContentPadding provides PaddingValues(
                    top = padding.calculateTopPadding(), bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())) {
                    Box(Modifier.fillMaxSize().nestedScroll(behavior.nestedScrollConnection)) { content() }
                }
            }
        }
    }
}
}
