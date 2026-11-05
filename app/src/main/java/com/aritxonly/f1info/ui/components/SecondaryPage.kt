package com.aritxonly.f1info.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
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
import com.aritxonly.f1info.ui.navigation.LocalTabletHeaderHost
import com.aritxonly.f1info.ui.navigation.TabletPaneHeader
import top.yukonga.miuix.kmp.blur.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*

internal val LocalSecondaryListState = staticCompositionLocalOf<LazyListState> { error("Missing SecondaryPage") }

val LocalSecondaryContentPadding = staticCompositionLocalOf { PaddingValues(0.dp) }

@Composable
fun secondaryContentPadding(extraTop: androidx.compose.ui.unit.Dp = 12.dp, extraBottom: androidx.compose.ui.unit.Dp = 32.dp): PaddingValues {
    val padding = LocalSecondaryContentPadding.current
    return PaddingValues(16.dp, padding.calculateTopPadding() + extraTop, 16.dp, padding.calculateBottomPadding() + extraBottom)
}

/** One active MIUIX scaffold; page pixels recorded separately from adaptive chrome. */
@Composable
fun SecondaryPage(title: String, accent: Color, onBack: (() -> Unit)?,
    tabs: List<String> = emptyList(), selectedTab: Int = 0, onTab: (Int) -> Unit = {},
    brandStyling: Boolean = true,
    content: @Composable () -> Unit) {
    val backdrop = LocalAdvancedMaterialBackdrop.current
    val listState = rememberLazyListState()
    val host = LocalTabletHeaderHost.current
    if (host != null) {
        val pane = rememberUpdatedState(TabletPaneHeader(listState, title, onBack, tabs, selectedTab, onTab))
        DisposableEffect(host, pane) {
            host.secondary = pane
            onDispose { if (host.secondary === pane) host.secondary = null }
        }
    }
    val behavior = if (currentF1WindowLayout().fixedHeader) null else key(title) { MiuixScrollBehavior() }
    val progress = behavior?.state?.collapsedFraction ?: listState.fixedHeaderProgress()
    val buttonProgress by animateFloatAsState(resolveTopBarButtonMaterialProgress(behavior?.state?.collapsedFraction, progress, null),
        tween(280, easing = FastOutSlowInEasing), label = "secondary-material")
    val base = MiuixTheme.colorScheme
    val colors = remember(base, accent, brandStyling) { if (brandStyling) com.aritxonly.f1info.ui.theme.miuixBrandColors(base, accent) else base }
    MiuixTheme(colors = colors) {
    CompositionLocalProvider(LocalRacingAccent provides rememberUpdatedState(accent), LocalTopBarScrollProgress provides progress,
        LocalTopBarButtonMaterialProgress provides buttonProgress) {
        Scaffold(containerColor = Color.Transparent, contentWindowInsets = WindowInsets(0, 0, 0, 0), topBar = {
            if (host == null && (title.isNotEmpty() || onBack != null || tabs.isNotEmpty())) ProgressiveTopBarMaterial {
                F1TopAppBar(title = title, behavior = behavior,
                    navigationIcon = {
                        if (onBack != null) TopBarIconButton(onBack) { Icon(MiuixIcons.ChevronBackward, "返回", Modifier.size(24.dp)) }
                    },
                    tabs = if (tabs.isEmpty()) null else ({ ImmersiveTextTabRow(tabs, selectedTab, onTab) }))
            }
        }) { padding ->
            Box(Modifier.fillMaxSize().then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)) {
                if (brandStyling && host == null) RacingFloatingBackground(Modifier.fillMaxSize(), alpha = 1f - progress * .35f)
                else if (!brandStyling) Box(Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface))
                CompositionLocalProvider(LocalSecondaryListState provides listState, LocalSecondaryContentPadding provides PaddingValues(
                    top = host?.contentTop ?: padding.calculateTopPadding(), bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())) {
                    Box(Modifier.align(Alignment.TopCenter).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                        .then(if (host == null) Modifier.widthIn(max = 720.dp) else Modifier).fillMaxSize()
                        .then(if (behavior != null) Modifier.nestedScroll(behavior.nestedScrollConnection) else Modifier)) { content() }
                }
            }
        }
    }
}
}
