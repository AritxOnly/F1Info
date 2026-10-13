package com.aritxonly.f1info.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aritxonly.f1info.model.*
import com.aritxonly.f1info.ui.theme.RacingDisplayFamily
import com.aritxonly.f1info.ui.theme.RacingScoreFamily
import androidx.compose.runtime.rememberUpdatedState
import com.aritxonly.f1info.ui.components.*
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.time.Instant

fun LazyListScope.homeContent(data: SeasonData, favorites: Favorites, now: Instant,
    onRace: (Race) -> Unit, onDriver: (Driver) -> Unit, onTeam: (Team) -> Unit, onCustomize: () -> Unit, onAccentChange: (androidx.compose.ui.graphics.Color) -> Unit, pager: PagerState) {
    item("favorites") { Column(Modifier.fillMaxWidth()) {
        val drivers = favorites.driverIds.map { id -> data.drivers.firstOrNull { it.id == id } }
        val team = data.teams.firstOrNull { it.id == favorites.teamId }
        val palette = listOf(teamBrandColor(drivers.getOrNull(0)?.teamId), teamBrandColor(drivers.getOrNull(1)?.teamId), teamBrandColor(team?.id))
        LaunchedEffect(pager, palette) {
            snapshotFlow { pager.settledPage }.collect { page -> onAccentChange(palette[page]) }
        }
        HorizontalPager(state = pager, beyondViewportPageCount = 2, modifier = Modifier.fillMaxWidth()) { page ->
            if (page < 2) DriverHero(drivers.getOrNull(page), onDriver, onCustomize)
            else TeamHero(team, data.season, onTeam, onCustomize)
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally)) {
            repeat(3) { index ->
                Box(Modifier.size(if (pager.currentPage == index) 24.dp else 5.dp, 5.dp)
                    .background(if (pager.currentPage == index) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.dividerLine, CircleShape))
            }
        }
    } }
    item("next") { Column(Modifier.padding(horizontal = 16.dp)) {
        val race = nextRace(data.races, now)
        SectionTitle(if (race != null) "下一个比赛周末" else "赛季回顾", "${data.season} SEASON", trailingColor = LocalRacingAccent.current.value)
        if (race != null) NextRaceCard(race, now, onRace) else SettingsSection {
            Column(Modifier.padding(24.dp)) {
                Text("本赛季比赛已全部结束", style = MiuixTheme.textStyles.title3)
                Text("在赛程中回顾各站比赛。新赛季发布后可刷新查看。", color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(top = 10.dp))
            }
        }
    } }
    val race = nextRace(data.races, now)
    if (race != null) item("sessions") { Column(Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("周末时间表", "${java.time.ZoneId.systemDefault().id}")
        SettingsSection { race.sessions.forEach { session -> InfoRow(session.name, sessionTime(session)) } }
    } }
    item("summary") { Column(Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("赛季速览")
        SettingsSection {
            InfoRow("大奖赛", "${data.races.size} 站")
            InfoRow("积分更新至", data.standingsRound?.let { "第 $it 轮" } ?: "积分尚未公布")
            InfoRow("我的关注", "2 名车手 · 1 支车队")
        }
    } }
}

@Composable
private fun DriverHero(driver: Driver?, onClick: (Driver) -> Unit, onCustomize: () -> Unit) {
    val c = MiuixTheme.colorScheme
    val accent = LocalRacingAccent.current.value
    Box(Modifier.fillMaxWidth().heightIn(min = 440.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Button) { if (driver != null) onClick(driver) else onCustomize() }) {
        Text(driver?.name?.substringBefore(" ")?.lowercase() ?: "race", fontFamily = RacingDisplayFamily, fontSize = 112.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = (-5).sp, maxLines = 1, softWrap = false, color = accent.copy(alpha = .20f),
            modifier = Modifier.padding(start = 16.dp, top = 34.dp).fillMaxWidth())
        if (driver != null) {
            DriverPortrait(driver.id, Modifier.align(Alignment.TopEnd).padding(top = 38.dp).fillMaxWidth(.66f).height(340.dp))
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 12.dp)) {
            Text("MY PADDOCK  /  我的关注", style = MiuixTheme.textStyles.footnote2, color = c.onSurfaceVariantSummary, letterSpacing = 2.sp)
            Spacer(Modifier.height(225.dp))
            Text(driver?.name ?: "选择你关注的车手", style = MiuixTheme.textStyles.title2, fontWeight = FontWeight.SemiBold,
                minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(driver?.team ?: "该车手未出现在当前赛季名单中", style = MiuixTheme.textStyles.body2, color = accent, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp))
            Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(driver?.position?.let { "P%02d  排名".format(it) } ?: "排名待公布", style = MiuixTheme.textStyles.footnote1)
                Text(driver?.wins?.let { "$it  胜场" } ?: "胜场待公布", style = MiuixTheme.textStyles.footnote1)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(driver?.points ?: "—", fontFamily = RacingScoreFamily, fontSize = 72.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp, maxLines = 1, softWrap = false)
                Text("积分", modifier = Modifier.padding(start = 12.dp, bottom = 14.dp), color = c.onSurfaceVariantSummary)
            }
        }
    }
}

@Composable
private fun TeamHero(team: Team?, season: Int, onClick: (Team) -> Unit, onCustomize: () -> Unit) {
    val c = MiuixTheme.colorScheme
    val accent = LocalRacingAccent.current.value
    Box(Modifier.fillMaxWidth().heightIn(min = 440.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Button) { if (team != null) onClick(team) else onCustomize() }) {
        Text("team", fontFamily = RacingDisplayFamily, fontSize = 112.sp, fontWeight = FontWeight.SemiBold, color = accent.copy(alpha = .20f),
            modifier = Modifier.padding(start = 16.dp, top = 34.dp), letterSpacing = (-5).sp)
        TeamCarVisual(team?.id, season, Modifier.align(Alignment.TopEnd).padding(top = 42.dp).fillMaxWidth().height(220.dp))
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 12.dp)) {
            Text("MY PADDOCK  /  我的车队", style = MiuixTheme.textStyles.footnote2, color = c.onSurfaceVariantSummary, letterSpacing = 2.sp)
            Spacer(Modifier.height(225.dp))
            Text(team?.name ?: "选择你关注的车队", style = MiuixTheme.textStyles.title2, fontWeight = FontWeight.SemiBold,
                minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("CONSTRUCTORS' CHAMPIONSHIP", style = MiuixTheme.textStyles.body2, color = accent,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp))
            Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(team?.position?.let { "P%02d  排名".format(it) } ?: "排名待公布", style = MiuixTheme.textStyles.footnote1)
                Text(team?.wins?.let { "$it  胜场" } ?: "胜场待公布", style = MiuixTheme.textStyles.footnote1)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(team?.points ?: "—", fontFamily = RacingScoreFamily, fontSize = 72.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp, maxLines = 1, softWrap = false)
                Text("积分", modifier = Modifier.padding(start = 12.dp, bottom = 14.dp), color = c.onSurfaceVariantSummary)
            }
        }
    }
}

@Composable
fun NextRaceCard(race: Race, now: Instant, onClick: (Race) -> Unit) {
    SettingsSection(mainContent = true, containerAlpha = .82f) {
        Column(Modifier.fillMaxWidth().clickable { onClick(race) }.padding(24.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("ROUND %02d".format(race.round), fontFamily = RacingDisplayFamily, color = LocalRacingAccent.current.value, style = MiuixTheme.textStyles.footnote1)
                Text(raceDate(race), style = MiuixTheme.textStyles.footnote1)
            }
            TrackOutline(race.circuit, Modifier.fillMaxWidth().height(140.dp), prominent = true)
            Text(race.name.removeSuffix(" Grand Prix"), style = MiuixTheme.textStyles.title1, fontWeight = FontWeight.SemiBold)
            Text(race.circuit.name, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, style = MiuixTheme.textStyles.body2,
                modifier = Modifier.padding(top = 4.dp))
            Text("正赛倒计时", style = MiuixTheme.textStyles.footnote2, modifier = Modifier.padding(top = 22.dp))
            Text(countdown(race.start, now), fontFamily = RacingDisplayFamily, style = MiuixTheme.textStyles.title3, color = LocalRacingAccent.current.value,
                fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 6.dp))
            Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("查看周末赛程", style = MiuixTheme.textStyles.footnote1, color = LocalRacingAccent.current.value)
                Icon(MiuixIcons.ChevronForward, null, Modifier.size(18.dp), tint = LocalRacingAccent.current.value)
            }
        }
    }
}
