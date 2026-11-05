package com.aritxonly.f1info.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.aritxonly.f1info.platform.rememberHyperModifierLift
import com.aritxonly.f1info.ui.base.*
import com.aritxonly.f1info.ui.material.glass.*
import com.aritxonly.f1info.ui.navigation.*
import com.aritxonly.f1info.ui.theme.LocalAdvancedMaterialBackdrop
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.blur.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun PrimaryPage(
    layout: F1WindowLayout,
    tab: String,
    items: List<MiuixFloatingTabItem>,
    onSelect: (String) -> Unit,
    scroll: LazyListState,
    behavior: ScrollBehavior?,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    header: @Composable () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    val tablet = layout == F1WindowLayout.Tablet
    val progress = if (behavior == null) scroll.fixedHeaderProgress() else remember(scroll) {
        derivedStateOf { if (scroll.firstVisibleItemIndex > 0) 1f else (scroll.firstVisibleItemScrollOffset / 160f).coerceIn(0f, 1f) }
    }.value
    val buttonProgress by animateFloatAsState(
        resolveTopBarButtonMaterialProgress(behavior?.state?.collapsedFraction, progress, null),
        tween(280, easing = FastOutSlowInEasing), label = "top-bar-button-material",
    )
    val backdrop = rememberLayerBackdrop()
    CompositionLocalProvider(LocalAdvancedMaterialBackdrop provides backdrop, LocalTopBarScrollProgress provides progress,
        LocalTopBarButtonMaterialProgress provides buttonProgress) {
        CompactDockHost(layout, tab) {
            val lift = if (tablet) 0.dp else rememberHyperModifierLift().dp
            val dockSpace = if (tablet) 0.dp else MiuixFloatingTabBarDefaults.Height + FloatingNavigationBottomSpacing + lift
            Scaffold(
                containerColor = if (tablet) androidx.compose.ui.graphics.Color.Transparent else MiuixTheme.colorScheme.surface,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = { if (!tablet) ProgressiveTopBarMaterial { header() } },
            ) { padding ->
                val contentTop = LocalTabletHeaderHost.current?.contentTop ?: padding.calculateTopPadding()
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize().layerBackdrop(backdrop), contentAlignment = Alignment.TopCenter) {
                        if (!tablet) RacingFloatingBackground(Modifier.fillMaxSize(), alpha = if (tab == "home") 1f - progress * .55f else .9f)
                        PullToRefresh(isRefreshing = refreshing, onRefresh = onRefresh,
                            modifier = Modifier.then(if (tablet) Modifier else Modifier.widthIn(max = 720.dp)).fillMaxSize(),
                            contentPadding = PaddingValues(top = contentTop), topAppBarScrollBehavior = behavior,
                            refreshTexts = listOf("下拉刷新", "松开刷新", "正在刷新", "刷新完成")) {
                            LazyColumn(state = scroll,
                                modifier = Modifier.fillMaxSize().then(if (behavior != null) Modifier.nestedScroll(behavior.nestedScrollConnection) else Modifier),
                                contentPadding = PaddingValues(top = contentTop + 12.dp,
                                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + dockSpace + 24.dp),
                                content = content)
                        }
                    }
                    if (!tablet) {
                        FloatingNavigationEdgeMask(lift, Modifier.align(Alignment.BottomCenter))
                        val side = if (layout.fixedHeader) 12.dp else 16.dp
                        MiuixFloatingTabBar(items, tab, { onSelect(it.key) }, Modifier.align(Alignment.BottomCenter)
                            .navigationBarsPadding().padding(start = side, end = side, bottom = FloatingNavigationBottomSpacing + lift))
                    }
                }
            }
        }
    }
}
