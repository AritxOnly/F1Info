package com.aritxonly.f1info.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import top.yukonga.miuix.kmp.nav.core.*
import top.yukonga.miuix.kmp.nav.transition.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.luminance
import androidx.core.view.WindowCompat
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aritxonly.f1info.model.*
import com.aritxonly.f1info.ui.base.*
import com.aritxonly.f1info.ui.components.*
import com.aritxonly.f1info.ui.home.weekendContent
import com.aritxonly.f1info.ui.home.homeContent
import com.aritxonly.f1info.ui.schedule.scheduleContent
import com.aritxonly.f1info.ui.standings.standingsContent
import com.aritxonly.f1info.ui.material.glass.*
import com.aritxonly.f1info.ui.navigation.*
import com.aritxonly.f1info.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.aritxonly.f1info.data.racing.LiveFeed
import com.aritxonly.f1info.ui.racing.*
import com.aritxonly.f1info.ui.settings.*
import top.yukonga.miuix.kmp.basic.*
import com.aritxonly.f1info.ui.navigation.ImmersiveTextTabRow
import top.yukonga.miuix.kmp.blur.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*
import top.yukonga.miuix.kmp.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun F1App(model: F1ViewModel = viewModel()) {
    val state by model.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("appearance", 0) }
    val appearance = remember { preferences.getInt("theme", 0) }
    val controller = remember(appearance) { ThemeController(when (appearance) { 1 -> ColorSchemeMode.Light; 2 -> ColorSchemeMode.System; else -> ColorSchemeMode.Dark }) }
    androidx.lifecycle.compose.LifecycleStartEffect(model) {
        model.refresh()
        onStopOrDispose { }
    }
    val baseColors = controller.currentColors()
    val brand = teamBrandTint(state.favorites.teamId) ?: baseColors.primary
    val colors = remember(baseColors, brand, baseColors.surface) { miuixBrandColors(baseColors, brand) }
    MiuixTheme(colors = colors) {
        val view = LocalView.current
        val lightBars = MiuixTheme.colorScheme.onSurface.luminance() < 0.5f
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = lightBars
                    isAppearanceLightNavigationBars = lightBars
                }
            }
        }
        val backdrop = rememberLayerBackdrop()
        CompositionLocalProvider(LocalAdvancedMaterialSpec provides AdvancedMaterialSpec(enabled = true),
            LocalAdvancedMaterialBackdrop provides backdrop) {
            ProvideAdvancedMaterialDialogBlurHost {
                RegisterAdvancedMaterialModalBackdrop(backdrop)
                Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) {
                    AppShell(state, backdrop, model::refresh, model::saveFavorites)
                }
            }
        }
    }
}

@Composable
private fun AppShell(state: F1State, backdrop: LayerBackdrop, onRefresh: () -> Unit,
    onFavorites: (Favorites) -> Unit) {
    val context = LocalContext.current
    val liveState by LiveFeed.state.collectAsStateWithLifecycle()
    val liveScope = rememberCoroutineScope()
    var livePullRefreshing by remember { mutableStateOf(false) }
    val stack = rememberSaveable(saver = F1RouteStackSaver) { navBackStackOf(MainRoute) }
    var favoritesCategory by rememberSaveable { mutableIntStateOf(0) }
    var homeAccent by remember { mutableStateOf<Color?>(null) }
    var tab by rememberSaveable { mutableStateOf((context as? Activity)?.intent?.getStringExtra("widget_destination") ?: "home") }
    androidx.lifecycle.compose.LifecycleStartEffect(tab) {
        val poll = if (tab == "live") liveScope.launch { var first = true; while (true) { LiveFeed.refresh(context, first); first = false; delay(15000) } } else null
        onStopOrDispose { poll?.cancel() }
    }
    var scheduleFilter by rememberSaveable { mutableIntStateOf(0) }
    var standingsCategory by rememberSaveable { mutableIntStateOf((context as? Activity)?.intent?.getIntExtra("widget_category", 0) ?: 0) }
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) { while (true) { now = Instant.now(); delay(30000) } }
    BackHandler(enabled = tab != "home" && stack.size == 1) { tab = "home" }
    val homePager = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 3 })
    val liveScroll = rememberLazyListState()
    val homeScroll = rememberLazyListState(); val scheduleScroll = rememberLazyListState(); val standingsScroll = rememberLazyListState()
    val scroll = when (tab) { "live" -> liveScroll; "schedule" -> scheduleScroll; "standings" -> standingsScroll; else -> homeScroll }
    val layout = currentF1WindowLayout()
    val tablet = layout == F1WindowLayout.Tablet
    // Keep one restorable history. A wide window presents the settings tail over the previous destination.
    val sheetStart = if (tablet) stack.indexOfFirst { it is DetailRoute && it.kind in settingsTitles } else -1
    val paneStack = if (sheetStart < 0) stack else {
        val underlying = stack.take(sheetStart)
        remember(underlying) { navBackStackOf(*underlying.toTypedArray()) }
    }
    val split = tablet && (paneStack.size > 1 || if (tab == "live") liveState.data != null else state.data != null)
    val scrollBehavior = if (layout.fixedHeader) null else {
        val home = MiuixScrollBehavior()
        val schedule = MiuixScrollBehavior()
        val standings = MiuixScrollBehavior()
        val live = MiuixScrollBehavior()
        when (tab) { "schedule" -> schedule; "standings" -> standings; "live" -> live; else -> home }
    }
    val items = listOf(
        MiuixFloatingTabItem("home", "首页", rememberVectorPainter(MiuixIcons.Home), rememberVectorPainter(MiuixIcons.Home)),
        MiuixFloatingTabItem("schedule", "赛程", rememberVectorPainter(MiuixIcons.Months), rememberVectorPainter(MiuixIcons.Months)),
        MiuixFloatingTabItem("standings", "积分榜", rememberVectorPainter(MiuixIcons.TopDownloads), rememberVectorPainter(MiuixIcons.TopDownloads)),
        MiuixFloatingTabItem("live", "Live", rememberVectorPainter(MiuixIcons.Promotions), rememberVectorPainter(MiuixIcons.Promotions)))
    fun clearDetails() { while (stack.size > 1) stack.removeAt(stack.lastIndex) }
    fun selectTab(key: String) { clearDetails(); tab = key }
    fun openFromList(route: DetailRoute) {
        if (stack.last() == route) return
        if (tablet) clearDetails()
        stack.add(route)
    }
    fun openSettings() { stack.add(DetailRoute("settings")) }
    val openRace: (Race) -> Unit = { openFromList(DetailRoute("race", it.round.toString())) }
    val openDriver: (Driver) -> Unit = { openFromList(DetailRoute("driver", it.id)) }
    val openTeam: (Team) -> Unit = { openFromList(DetailRoute("team", it.id)) }
    val leaderTeamId = if (tab == "standings") { if (standingsCategory == 0) state.data?.drivers?.firstOrNull()?.teamId else state.data?.teams?.firstOrNull()?.id } else null
    val accentTarget = if (leaderTeamId != null) teamBrandColor(leaderTeamId) else if (tab == "home") homeAccent ?: teamBrandColor(state.favorites.teamId) else teamBrandColor(state.favorites.teamId)
    val accent = animateColorAsState(accentTarget, tween(380, easing = FastOutSlowInEasing), label = "paddock-brand")

    val detail: @Composable (DetailRoute, Boolean) -> Unit = { route, paneSelection ->
        val entryBackdrop = rememberLayerBackdrop()
        val raceOpen = route.kind == "race"
        val entityOpen = route.kind == "driver" || route.kind == "team"
        val favoritesOpen = route.kind == "favorites"
        val resultsOpen = route.kind == "results"
        var resultsCategory by rememberSaveable(route.id) { mutableIntStateOf(0) }
        val selectedRace = if (raceOpen) state.data?.races?.firstOrNull { it.round.toString() == route.id } else null
        val selectedDriver = if (route.kind == "driver") state.data?.drivers?.firstOrNull { it.id == route.id } else null
        val selectedTeam = if (route.kind == "team") state.data?.teams?.firstOrNull { it.id == route.id } else null
        val back: () -> Unit = { if (stack.size > 1) stack.removeAt(stack.lastIndex) }
        CompositionLocalProvider(LocalAdvancedMaterialBackdrop provides entryBackdrop) {
            val detailAccent = if (entityOpen) teamBrandColor(selectedDriver?.teamId ?: selectedTeam?.id) else teamBrandColor(state.favorites.teamId)
            SecondaryPage(
                title = when { raceOpen -> "比赛周末"; entityOpen -> if (route.kind == "driver") "车手详情" else "车队详情"; favoritesOpen -> "我的围场"; route.kind == "results" -> "比赛结果"; route.kind in settingsTitles -> settingsTitles.getValue(route.kind); route.kind == "live-sessions" -> "选择场次"; else -> "关于" },
                accent = detailAccent, onBack = if (paneSelection) null else back,
                tabs = when { favoritesOpen -> listOf("车手", "车队"); resultsOpen -> listOf("正赛", "排位赛", "冲刺赛"); else -> emptyList() },
                selectedTab = if (resultsOpen) resultsCategory else favoritesCategory,
                onTab = { if (resultsOpen) resultsCategory = it else favoritesCategory = it },
            ) {
                when {
                    (raceOpen && selectedRace == null) || (entityOpen && selectedDriver == null && selectedTeam == null) ->
                        Box(Modifier.fillMaxSize().padding(secondaryContentPadding()), contentAlignment = Alignment.Center) {
                            Text(if (state.loading) "正在读取赛季数据…" else "当前赛季暂无该条目")
                        }
                    raceOpen -> RaceDetailContent(selectedRace, { stack.add(DetailRoute("results", "${it.season}:${it.round}")) }, { selectTab("live") })
                    route.kind == "results" -> ResultsContent(route.id.substringBefore(":").toInt(), route.id.substringAfter(":").toInt(), resultsCategory)
                    route.kind in settingsTitles -> SettingsDestination(route.kind, state.data) { stack.add(DetailRoute(it)) }
                    route.kind == "live-sessions" -> LiveSessionsContent(state.data?.season ?: java.time.LocalDate.now().year, back)
                    entityOpen -> EntityDetailContent(selectedDriver, selectedTeam, state.favorites, state.data, onFavorites)
                    favoritesOpen -> FavoritesDetailContent(state.data, state.favorites, back, onFavorites, favoritesCategory)
                    else -> AboutContent()

                }
            }
        }
    }
    val baseColors = MiuixTheme.colorScheme
    val pageColors = remember(baseColors, accentTarget) { miuixBrandColors(baseColors, accentTarget) }
    val defaultRoute = if (split) defaultTabletDetail(tab, state.data, scheduleFilter, standingsCategory, now) else null
    val activeDetail = if (split) paneStack.filterIsInstance<DetailRoute>().firstOrNull() ?: defaultRoute else null
    val pageTitle = when (tab) { "home" -> ""; "schedule" -> "比赛赛程"; "live" -> "Live 围场"; else -> "积分榜" }
    val settingsAction: @Composable () -> Unit = {
        TopBarIconButton(::openSettings) {
            Icon(MiuixIcons.Settings, "设置", Modifier.size(22.dp), tint = MiuixTheme.colorScheme.onSurface)
        }
    }
    val filters: (@Composable () -> Unit)? = if (tab == "schedule" || tab == "standings") ({
        ImmersiveTextTabRow(
            tabs = if (tab == "schedule") listOf("全部 ${state.data?.races?.size ?: 0}", "即将举行", "已结束") else listOf("车手积分榜", "车队积分榜"),
            selectedTabIndex = if (tab == "schedule") scheduleFilter else standingsCategory,
            onTabSelected = {
                clearDetails()
                if (tab == "schedule") scheduleFilter = it else standingsCategory = it
            },
        )
    }) else null
    val header: @Composable () -> Unit = {
        F1TopAppBar(title = pageTitle,
            subtitle = state.data?.let { "${it.season} 赛季" + if (tab == "standings") " · 第 ${it.standingsRound ?: "—"} 轮后" else "" }.orEmpty(),
            small = tab == "home", behavior = scrollBehavior, actions = { settingsAction() }, tabs = filters)
    }
    val primary: @Composable () -> Unit = {
        MiuixTheme(colors = pageColors) {
            PrimaryPage(layout, tab, items, ::selectTab, scroll, scrollBehavior,
                refreshing = if (tab == "live") livePullRefreshing else state.loading,
                onRefresh = {
                    if (tab == "live") liveScope.launch {
                        livePullRefreshing = true
                        try { LiveFeed.refresh(context, true) } finally { livePullRefreshing = false }
                    } else onRefresh()
                },
                header = header,
            ) {
                if (tab == "live") liveContent(liveState, now, { openFromList(DetailRoute("live-sessions")) }, ::openSettings, split)
                else if (state.data == null) item { Box(Modifier.padding(horizontal = 16.dp)) { InitialState(state, onRefresh) } }
                else {
                    item("data-status") { Box(Modifier.padding(horizontal = 16.dp)) { DataStatus(state, onRefresh, tab == "home" && layout == F1WindowLayout.WideCover) } }
                    when (tab) {
                        "schedule" -> scheduleContent(state.data, now, scheduleFilter, openRace, split, activeDetail?.takeIf { it.kind == "race" }?.id)
                        "standings" -> standingsContent(state.data, state.favorites, standingsCategory, openDriver, openTeam, split, activeDetail?.id)
                        else -> homeContent(state.data, state.favorites, now, openRace, openDriver, openTeam, { openFromList(DetailRoute("favorites")) }, { homeAccent = it }, homePager, split)
                    }
                    item("source") {
                        Text("JOLPICA F1  ·  ${state.data.season}\n非官方 F1 信息应用", style = MiuixTheme.textStyles.footnote2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary, modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }
    }

    CompositionLocalProvider(LocalRacingAccent provides accent) {
        F1AdaptiveScaffold(layout, items, tab, ::selectTab, backdrop, split, pageTitle, scroll, filters, settingsAction, primary) {
            NavDisplay(backStack = paneStack, onBack = { if (stack.size > 1) stack.removeAt(stack.lastIndex) }, transition = if (tablet) NavTransitions.None else NavTransitions.MiuixDefault,
                effects = if (tablet) NavDisplayEffects(dimAmount = 0f) else NavDisplayEffects(backdropColor = MiuixTheme.colorScheme.surface, blockInputDuringTransition = true)) {
                entry<DetailRoute>(swipeDismiss = if (tablet) NavSwipeDirection.None else NavSwipeDirection.LeftToRight) { route ->
                    if (!tablet || route == paneStack.last()) {
                        val paneSelection = tablet && when (tab) {
                            "schedule" -> route.kind == "race"
                            "standings" -> route.kind == "driver" || route.kind == "team"
                            else -> false
                        }
                        detail(route, paneSelection)
                    }
                }
                entry<MainRoute> {
                    if (tablet && paneStack.last() != MainRoute) return@entry
                    if (!split) primary() else key(tab, standingsCategory, scheduleFilter) {
                        val data = state.data
                        if (defaultRoute != null) key(defaultRoute) { detail(defaultRoute, true) }
                        else CompositionLocalProvider(LocalAdvancedMaterialBackdrop provides rememberLayerBackdrop()) {
                            SecondaryPage(if (tab == "live") "比赛动态" else if (tab == "home") "" else "详情",
                                accent.value, onBack = null) {
                                LazyColumn(Modifier.fillMaxSize(), state = LocalSecondaryListState.current,
                                    contentPadding = PaddingValues(top = secondaryContentPadding().calculateTopPadding(),
                                        bottom = secondaryContentPadding().calculateBottomPadding())) {
                                    when (tab) {
                                        "home" -> data?.let { weekendContent(it, now, openRace) }
                                        "live" -> liveState.data?.let { raceControlContent(it) }
                                        else -> item { Text("暂无符合条件的条目", Modifier.padding(24.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (tablet) {
            val page = (stack.last() as? DetailRoute)?.kind?.takeIf { sheetStart >= 0 } ?: "settings"
            val child = sheetStart >= 0 && stack.lastIndex > sheetStart
            MiuixBottomSheet(show = sheetStart >= 0, title = settingsTitles[page],
                onDismissRequest = { if (sheetStart >= 0) while (stack.size > sheetStart) stack.removeAt(stack.lastIndex) },
                startAction = if (!child) null else ({
                    IconButton({ if (stack.lastIndex > sheetStart) stack.removeAt(stack.lastIndex) }) {
                        Icon(MiuixIcons.ChevronBackward, "返回设置", Modifier.size(22.dp), tint = MiuixTheme.colorScheme.onSurface)
                    }
                })) {
                key(page) {
                    BackHandler(enabled = child) { stack.removeAt(stack.lastIndex) }
                    CompositionLocalProvider(LocalSecondaryListState provides rememberLazyListState()) {
                        Box(Modifier.fillMaxWidth().heightIn(max = 560.dp)) {
                            SettingsDestination(page, state.data) { stack.add(DetailRoute(it)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InitialState(state: F1State, onRefresh: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 90.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(MiuixIcons.TopDownloads, null, Modifier.size(66.dp), tint = MiuixTheme.colorScheme.primary)
        Text(if (state.loading) "正在准备你的围场" else "暂时无法获取赛季数据", style = MiuixTheme.textStyles.title3,
            modifier = Modifier.padding(top = 24.dp))
        if (state.loading) CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
        else {
            Text(state.error.orEmpty(), modifier = Modifier.padding(top = 12.dp), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            TextButton("重试", onRefresh, Modifier.padding(top = 24.dp), colors = ButtonDefaults.textButtonColorsPrimary())
        }
    }
}

@Composable
private fun DataStatus(state: F1State, onRefresh: () -> Unit, compact: Boolean) {
    val timestamp = state.data?.fetchedAt?.atZone(ZoneId.systemDefault())?.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
    if (compact && state.error != null) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("更新失败 · 显示缓存 $timestamp", Modifier.weight(1f), style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            TextButton("重试", onRefresh, enabled = !state.loading)
        }
    } else if (state.error != null) SettingsSection {
        Column(Modifier.padding(18.dp)) {
            Text("${state.error}\n正在显示上次保存的数据（$timestamp）", style = MiuixTheme.textStyles.footnote1)
            TextButton("重新加载", onRefresh, Modifier.fillMaxWidth().padding(top = 10.dp), enabled = !state.loading)
        }
    } else Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(if (state.loading) "正在更新…" else if (state.cached) "已缓存 · $timestamp" else "已更新 · $timestamp",
            style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        Text("${state.data?.season} 赛季", style = MiuixTheme.textStyles.footnote2, color = LocalRacingAccent.current.value)
    }
}
