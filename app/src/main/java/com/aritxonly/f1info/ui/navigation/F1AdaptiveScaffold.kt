package com.aritxonly.f1info.ui.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.aritxonly.f1info.ui.base.F1WindowLayout
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.aritxonly.f1info.ui.base.TopBarIconButton
import com.aritxonly.f1info.ui.base.fixedHeaderProgress
import com.aritxonly.f1info.ui.material.glass.LocalTopBarButtonMaterialProgress
import com.aritxonly.f1info.ui.material.glass.LocalTopBarScrollProgress
import com.aritxonly.f1info.ui.material.glass.ProgressiveTopBarMaterial
import com.aritxonly.f1info.ui.components.RacingFloatingBackground
import com.aritxonly.f1info.ui.material.glass.DeadlinerGlassRecipes
import com.aritxonly.f1info.ui.material.glass.SoftGlassSurface
import com.aritxonly.f1info.ui.theme.LocalAdvancedMaterialSpec
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.blur.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal class TabletHeaderHost(val contentTop: Dp) {
    var secondary by mutableStateOf<State<TabletPaneHeader>?>(null)
}

internal data class TabletPaneHeader(
    val list: LazyListState,
    val title: String,
    val back: (() -> Unit)?,
    val tabs: List<String>,
    val selectedTab: Int,
    val onTab: (Int) -> Unit,
)

internal val LocalTabletHeaderHost = staticCompositionLocalOf<TabletHeaderHost?> { null }

/** Keep the same route host across folds. Only its bounds and the companion pane change. */
@Composable
internal fun F1AdaptiveScaffold(
    layout: F1WindowLayout,
    items: List<MiuixFloatingTabItem>,
    selected: String,
    onSelect: (String) -> Unit,
    backdrop: LayerBackdrop,
    showCompanion: Boolean,
    pageTitle: String,
    list: LazyListState,
    filters: (@Composable () -> Unit)?,
    actions: @Composable () -> Unit,
    primary: @Composable () -> Unit,
    routes: @Composable () -> Unit,
) {
    val tablet = layout == F1WindowLayout.Tablet
    val sidebarText = MiuixTheme.colorScheme.onSurfaceContainer
    val preferences = LocalContext.current.getSharedPreferences("appearance", 0)
    BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))) {
        val landscape = maxWidth > maxHeight
        var expanded by remember(landscape) { mutableStateOf(preferences.getBoolean("tablet_sidebar_expanded", landscape)) }
        fun setExpanded(value: Boolean) {
            expanded = value
            preferences.edit().putBoolean("tablet_sidebar_expanded", value).apply()
        }
        val sidebarWidth = (maxWidth * .18f).coerceIn(128.dp, 216.dp)
        val inset by animateDpAsState(if (tablet && expanded) sidebarWidth + 12.dp else 0.dp, label = "sidebar-width")
        val topInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
        val rowHeight = maxOf(56.dp, with(LocalDensity.current) { 32.sp.toDp() } + 8.dp)
        val contentTop = topInsets.asPaddingValues().calculateTopPadding() + rowHeight
        val host = if (tablet) remember(contentTop) { TabletHeaderHost(contentTop) } else null

        // Record the atmosphere and both full-height scrolling panes. Sample them only from sibling chrome.
        Box(Modifier.fillMaxSize().then(if (tablet) Modifier.layerBackdrop(backdrop) else Modifier)) {
            if (tablet) RacingFloatingBackground(Modifier.fillMaxSize())
            CompositionLocalProvider(LocalTabletHeaderHost provides host) {
                Row(Modifier.fillMaxSize().padding(start = if (tablet) inset else 0.dp)) {
                    if (showCompanion) Box(Modifier.weight(.48f).fillMaxHeight().clipToBounds()) { primary() }
                    Box(Modifier.weight(if (showCompanion) .52f else 1f).fillMaxHeight().clipToBounds()) { routes() }
                }
            }
        }
        if (host != null) {
            val pane = host.secondary?.value
            val detail = pane?.takeIf { it.back != null }
            val progress = maxOf(list.fixedHeaderProgress(), pane?.list?.fixedHeaderProgress() ?: 0f)
            val buttonProgress by animateFloatAsState(progress, tween(280, easing = FastOutSlowInEasing), label = "tablet-header-glass")
            CompositionLocalProvider(LocalTopBarScrollProgress provides progress, LocalTopBarButtonMaterialProgress provides buttonProgress) {
                ProgressiveTopBarMaterial(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().windowInsetsPadding(topInsets).padding(start = inset).height(rowHeight),
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(Modifier.weight(1f).padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            detail?.back?.let { back ->
                                TopBarIconButton(back) { Icon(MiuixIcons.ChevronBackward, "返回", Modifier.size(24.dp), tint = MiuixTheme.colorScheme.onSurface) }
                            }
                            val title = detail?.title ?: pageTitle
                            if (title.isNotEmpty()) Text(title, Modifier.padding(horizontal = 8.dp).semantics { heading() },
                                style = MiuixTheme.textStyles.title2, color = MiuixTheme.colorScheme.onSurface,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (!expanded) {
                            ImmersiveTextTabRow(items.map { it.label }, items.indexOfFirst { it.key == selected },
                                { onSelect(items[it].key) }, Modifier.width(56.dp + 56.dp * items.size),
                                leadingControl = {
                                    IconButton({ setExpanded(true) }) { Icon(MiuixIcons.Sidebar, "展开侧边导航", Modifier.size(22.dp), tint = sidebarText) }
                                })
                        }
                        Row(Modifier.weight(1f).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f).padding(horizontal = 4.dp), contentAlignment = Alignment.CenterEnd) {
                                if (detail != null && detail.tabs.isNotEmpty()) {
                                    ImmersiveTextTabRow(detail.tabs, detail.selectedTab, detail.onTab)
                                } else filters?.invoke()
                            }
                            actions()
                        }
                    }
                }
            }
        }
        if (tablet && inset > 12.dp) {
            SoftGlassSurface(
                modifier = Modifier.windowInsetsPadding(topInsets).navigationBarsPadding().padding(start = 12.dp, bottom = 12.dp)
                    .width(inset - 12.dp).fillMaxHeight(),
                shape = RoundedCornerShape(28.dp),
                recipe = DeadlinerGlassRecipes.tabletNavigation(LocalAdvancedMaterialSpec.current.fineTuning),
                backdrop = backdrop,
            ) {
                Column(Modifier.fillMaxSize().padding(8.dp).verticalScroll(rememberScrollState()).selectableGroup()) {
                    IconButton({ setExpanded(false) }) { Icon(MiuixIcons.Sidebar, "收起侧边导航", Modifier.size(22.dp), tint = sidebarText) }
                    items.forEach { item ->
                        val active = selected == item.key
                        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(RoundedCornerShape(24.dp))
                            .background(if (active) MiuixTheme.colorScheme.onSurface.copy(alpha = .10f) else androidx.compose.ui.graphics.Color.Transparent)
                            .selectable(active, role = Role.Tab) {
                                onSelect(item.key)
                            }.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(if (active) item.selectedIcon else item.unselectedIcon, null, Modifier.size(22.dp), tint = sidebarText)
                            Text(item.label, color = sidebarText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CompactDockHost(layout: F1WindowLayout, tab: String, content: @Composable () -> Unit) {
    val cover = layout == F1WindowLayout.WideCover
    var collapsed by remember(cover, tab) { mutableStateOf(false) }
    val observer = remember(cover, tab) {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && consumed.y != 0f) collapsed = consumed.y < 0f
                return Offset.Zero
            }
        }
    }
    val fraction by animateFloatAsState(if (cover && collapsed) 1f else 0f, label = "scrolling-dock")
    Box(Modifier.fillMaxSize().then(if (cover) Modifier.nestedScroll(observer) else Modifier)) {
        CompositionLocalProvider(LocalDockScrollFraction provides fraction, content = content)
    }
}
