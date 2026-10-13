package com.aritxonly.f1info.ui

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
                AppShell(state, backdrop, model::refresh, model::saveFavorites)
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
    var selectedRace by remember { mutableStateOf<Race?>(null) }
    var selectedDriver by remember { mutableStateOf<Driver?>(null) }
    var selectedTeam by remember { mutableStateOf<Team?>(null) }
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) { while (true) { now = Instant.now(); delay(30000) } }
    BackHandler(enabled = tab != "home" && stack.size == 1) { tab = "home" }
    val homePager = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 3 })
    val liveScroll = rememberLazyListState()
    val homeScroll = rememberLazyListState(); val scheduleScroll = rememberLazyListState(); val standingsScroll = rememberLazyListState()
    val scroll = when (tab) { "live" -> liveScroll; "schedule" -> scheduleScroll; "standings" -> standingsScroll; else -> homeScroll }
    val progress by remember(scroll) { derivedStateOf { if (scroll.firstVisibleItemIndex > 0) 1f else (scroll.firstVisibleItemScrollOffset / 160f).coerceIn(0f, 1f) } }
    val liveBehavior = MiuixScrollBehavior()
    val homeBehavior = MiuixScrollBehavior()
    val scheduleBehavior = MiuixScrollBehavior()
    val standingsBehavior = MiuixScrollBehavior()
    val scrollBehavior = when (tab) { "live" -> liveBehavior; "schedule" -> scheduleBehavior; "standings" -> standingsBehavior; else -> homeBehavior }
    val buttonMaterialProgress by animateFloatAsState(
        resolveTopBarButtonMaterialProgress(scrollBehavior.state.collapsedFraction, progress, null),
        tween(280, easing = FastOutSlowInEasing), label = "top-bar-button-material")
    val navigationLift = com.aritxonly.f1info.platform.rememberHyperModifierLift().dp
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val items = listOf(
        MiuixFloatingTabItem("home", "首页", rememberVectorPainter(MiuixIcons.Home), rememberVectorPainter(MiuixIcons.Home)),
        MiuixFloatingTabItem("schedule", "赛程", rememberVectorPainter(MiuixIcons.Months), rememberVectorPainter(MiuixIcons.Months)),
        MiuixFloatingTabItem("standings", "积分榜", rememberVectorPainter(MiuixIcons.TopDownloads), rememberVectorPainter(MiuixIcons.TopDownloads)),
        MiuixFloatingTabItem("live", "Live", rememberVectorPainter(MiuixIcons.Promotions), rememberVectorPainter(MiuixIcons.Promotions)))
    val openRace: (Race) -> Unit = { selectedRace = it; stack.add(DetailRoute("race", it.round.toString())) }
    val openDriver: (Driver) -> Unit = { selectedDriver = it; selectedTeam = null; stack.add(DetailRoute("driver", it.id)) }
    val openTeam: (Team) -> Unit = { selectedTeam = it; selectedDriver = null; stack.add(DetailRoute("team", it.id)) }
    val leaderTeamId = if (tab == "standings") { if (standingsCategory == 0) state.data?.drivers?.firstOrNull()?.teamId else state.data?.teams?.firstOrNull()?.id } else null
    val accentTarget = if (leaderTeamId != null) teamBrandColor(leaderTeamId) else if (tab == "home") homeAccent ?: teamBrandColor(state.favorites.teamId) else teamBrandColor(state.favorites.teamId)
    val accent = animateColorAsState(accentTarget, tween(380, easing = FastOutSlowInEasing), label = "paddock-brand")

    NavDisplay(backStack = stack, transition = NavTransitions.MiuixDefault,
        effects = NavDisplayEffects(backdropColor = MiuixTheme.colorScheme.surface, blockInputDuringTransition = true)) {
    entry<DetailRoute>(swipeDismiss = NavSwipeDirection.LeftToRight) { route ->
        val entryBackdrop = rememberLayerBackdrop()
        val raceOpen = route.kind == "race"
        val entityOpen = route.kind == "driver" || route.kind == "team"
        val favoritesOpen = route.kind == "favorites"
        val resultsOpen = route.kind == "results"
        var resultsCategory by rememberSaveable(route.id) { mutableIntStateOf(0) }
        val selectedRace = state.data?.races?.firstOrNull { it.round.toString() == route.id } ?: selectedRace
        val selectedDriver = if (route.kind == "driver") state.data?.drivers?.firstOrNull { it.id == route.id } ?: selectedDriver else null
        val selectedTeam = if (route.kind == "team") state.data?.teams?.firstOrNull { it.id == route.id } ?: selectedTeam else null
        val back: () -> Unit = { if (stack.size > 1) stack.removeAt(stack.lastIndex) }
        CompositionLocalProvider(LocalAdvancedMaterialBackdrop provides entryBackdrop) {
        val detailAccent = if (entityOpen) teamBrandColor(selectedDriver?.teamId ?: selectedTeam?.id) else teamBrandColor(state.favorites.teamId)
        SecondaryPage(
            title = when { raceOpen -> "比赛周末"; entityOpen -> if (selectedDriver != null) "车手详情" else "车队详情"; favoritesOpen -> "我的围场"; route.kind == "results" -> "比赛结果"; route.kind == "settings" || route.kind == "live-settings" -> "设置"; route.kind == "openf1-settings" -> "OpenF1 API"; route.kind == "island-settings" -> "超级岛"; route.kind == "calendar" -> "导入日历日程"; route.kind == "live-sessions" -> "选择场次"; else -> "关于" },
            accent = detailAccent, onBack = back,
            tabs = when { favoritesOpen -> listOf("车手", "车队"); resultsOpen -> listOf("正赛", "排位赛", "冲刺赛"); else -> emptyList() },
            selectedTab = if (resultsOpen) resultsCategory else favoritesCategory,
            onTab = { if (resultsOpen) resultsCategory = it else favoritesCategory = it },
        ) {
            when {
                raceOpen -> RaceDetailContent(selectedRace, { stack.add(DetailRoute("results", "${it.season}:${it.round}")) }, { tab = "live"; back() })
                route.kind == "results" -> ResultsContent(route.id.substringBefore(":").toInt(), route.id.substringAfter(":").toInt(), resultsCategory)
                route.kind == "settings" || route.kind == "live-settings" -> SettingsContent { stack.add(DetailRoute(it)) }
                route.kind == "openf1-settings" -> LiveSettingsContent("openf1")
                route.kind == "island-settings" -> LiveSettingsContent("island")
                route.kind == "calendar" -> CalendarImportContent(state.data)
                route.kind == "live-sessions" -> LiveSessionsContent(state.data?.season ?: java.time.LocalDate.now().year, back)
                entityOpen -> EntityDetailContent(selectedDriver, selectedTeam, state.favorites, state.data, onFavorites)
                favoritesOpen -> FavoritesDetailContent(state.data, state.favorites, back, onFavorites, favoritesCategory)
                else -> AboutContent()

            }
        }
        }
    }
    entry<MainRoute> {
    val entryBackdrop = rememberLayerBackdrop()
    // Page accent roles follow the exact animated glow; MIUIX neutral surfaces stay native.
    val baseColors = MiuixTheme.colorScheme
    val pageColors = remember(baseColors, accentTarget) { miuixBrandColors(baseColors, accentTarget) }
    MiuixTheme(colors = pageColors) {
    CompositionLocalProvider(LocalRacingAccent provides accent, LocalAdvancedMaterialBackdrop provides entryBackdrop) {
    Scaffold(
        containerColor = MiuixTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompositionLocalProvider(LocalTopBarScrollProgress provides progress,
                LocalTopBarButtonMaterialProgress provides buttonMaterialProgress) {
                ProgressiveTopBarMaterial {
                    val topActions: @Composable RowScope.() -> Unit = {
                            TopBarIconButton({ stack.add(DetailRoute("settings")) }) {
                                Icon(MiuixIcons.Settings, "设置", Modifier.size(22.dp), tint = MiuixTheme.colorScheme.onSurface)
                            }
                        }
                    if (tab == "home") SmallTopAppBar(title = "", color = Color.Transparent, actions = topActions, scrollBehavior = scrollBehavior)
                    else TopAppBar(
                        title = when (tab) { "schedule" -> "比赛赛程"; "live" -> "Live 围场"; else -> "积分榜" },
                        subtitle = state.data?.let { "${it.season} 赛季" + if (tab == "standings") " · 第 ${it.standingsRound ?: "—"} 轮后" else "" }.orEmpty(),
                        color = Color.Transparent, scrollBehavior = scrollBehavior, actions = topActions,
                        bottomContent = {
                            if (tab == "schedule" || tab == "standings") Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                                ImmersiveTextTabRow(
                                    tabs = if (tab == "schedule") listOf("全部 ${state.data?.races?.size ?: 0}", "即将举行", "已结束") else listOf("车手积分榜", "车队积分榜"),
                                    selectedTabIndex = if (tab == "schedule") scheduleFilter else standingsCategory,
                                    onTabSelected = { if (tab == "schedule") scheduleFilter = it else standingsCategory = it },
                                    materialProgress = null,
                                )
                            }
                        },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            // Only page pixels enter the shared recorder. Chrome is a sibling, preventing a RenderNode cycle.
            Box(Modifier.fillMaxSize().layerBackdrop(entryBackdrop), contentAlignment = Alignment.TopCenter) {
                RacingFloatingBackground(Modifier.fillMaxSize(), alpha = if (tab == "home") 1f - progress * .55f else .9f, accent = accent)
                PullToRefresh(isRefreshing = if (tab == "live") livePullRefreshing else state.loading, onRefresh = { if (tab == "live") liveScope.launch { livePullRefreshing = true; try { LiveFeed.refresh(context, true) } finally { livePullRefreshing = false } } else onRefresh() },
                    modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(top = padding.calculateTopPadding()),
                    topAppBarScrollBehavior = scrollBehavior,
                    refreshTexts = listOf("下拉刷新", "松开刷新", "正在刷新", "刷新完成")) {
                LazyColumn(state = scroll, modifier = Modifier.widthIn(max = 700.dp).fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(0.dp, padding.calculateTopPadding() + 12.dp, 0.dp, bottomInset + MiuixFloatingTabBarDefaults.Height + 4.dp + navigationLift + 24.dp)) {
                    if (tab == "live") liveContent(liveState, now, { stack.add(DetailRoute("live-sessions")) }, { stack.add(DetailRoute("live-settings")) })
                    else if (state.data == null) item { Box(Modifier.padding(horizontal = 16.dp)) { InitialState(state, onRefresh) } }
                    else {
                        item("data-status") { Box(Modifier.padding(horizontal = 16.dp)) { DataStatus(state, onRefresh) } }
                        when (tab) {
                            "schedule" -> scheduleContent(state.data, now, scheduleFilter, openRace)
                            "standings" -> standingsContent(state.data, state.favorites, standingsCategory, openDriver, openTeam)
                            else -> homeContent(state.data, state.favorites, now, openRace, openDriver, openTeam, { stack.add(DetailRoute("favorites")) }, { homeAccent = it }, homePager)
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
            Row(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 4.dp + navigationLift)
                .widthIn(max = 420.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                MiuixFloatingTabBar(items, tab, { tab = it.key }, Modifier.weight(1f), layout = MiuixFloatingTabLayout.Stacked)

            }

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
private fun DataStatus(state: F1State, onRefresh: () -> Unit) {
    val timestamp = state.data?.fetchedAt?.atZone(ZoneId.systemDefault())?.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
    if (state.error != null) SettingsSection {
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
