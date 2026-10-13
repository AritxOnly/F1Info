package com.aritxonly.f1info.ui.standings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aritxonly.f1info.model.*
import com.aritxonly.f1info.ui.theme.RacingDisplayFamily
import androidx.compose.runtime.rememberUpdatedState
import com.aritxonly.f1info.ui.components.*
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

fun LazyListScope.standingsContent(data: SeasonData, favorites: Favorites, category: Int,
    onDriver: (Driver) -> Unit, onTeam: (Team) -> Unit) {
    val rankedDrivers = data.drivers.filter { it.position != null }
    val rankedTeams = data.teams.filter { it.position != null }
    if ((category == 0 && rankedDrivers.isEmpty()) || (category == 1 && rankedTeams.isEmpty())) {
        item("empty") { Column(Modifier.padding(horizontal = 16.dp)) { SettingsSection { Column(Modifier.padding(26.dp)) {
            Icon(MiuixIcons.TopDownloads, null, Modifier.size(48.dp), tint = MiuixTheme.colorScheme.primary)
            Text("积分尚未公布", style = MiuixTheme.textStyles.title3, modifier = Modifier.padding(top = 16.dp))
            Text("数据源尚未提供本赛季排名。公布后刷新即可查看。", modifier = Modifier.padding(top = 8.dp),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        } } } }
    } else {
        item("leader") { Column(Modifier.padding(horizontal = 16.dp)) {
            val driver = if (category == 0) rankedDrivers.first() else null
            val team = if (category == 1) rankedTeams.first() else null
            CompositionLocalProvider(LocalRacingAccent provides rememberUpdatedState(teamBrandColor(driver?.teamId ?: team?.id))) { LeaderHero(driver, team, data.season, { driver?.let(onDriver); team?.let(onTeam) }) }
        } }
        if (category == 0) items(rankedDrivers, key = { it.id }) { d ->
            StandingRow(teamBrandColor(d.teamId), d.position, d.name, d.team, d.points, d.id in favorites.driverIds, { onDriver(d) }) { DriverNumberIcon(d.id, d.number, Modifier.size(38.dp, 32.dp)) }
        } else items(rankedTeams, key = { it.id }) { t ->
            StandingRow(teamBrandColor(t.id), t.position, t.name, t.nationality, t.points, t.id == favorites.teamId, { onTeam(t) }) { TeamLogo(t.id, Modifier.size(38.dp, 32.dp)) }
        }
    }
}

@Composable
private fun LeaderHero(driver: Driver?, team: Team?, season: Int, onClick: () -> Unit) {
    val c = MiuixTheme.colorScheme
    val accent = teamBrandColor(driver?.teamId ?: team?.id)
    Box(Modifier.fillMaxWidth().height(if (team != null) 340.dp else 285.dp).clickable(onClick = onClick)) {
        TeamLogo(driver?.teamId ?: team?.id, Modifier.align(Alignment.TopCenter).fillMaxWidth(.85f).height(225.dp).then(Modifier.alpha(.13f)))
        if (driver != null) DriverPortrait(driver.id, Modifier.align(Alignment.TopEnd).fillMaxWidth(.6f).height(245.dp))
        else TeamCarVisual(team?.id, season, Modifier.align(Alignment.TopEnd).fillMaxWidth().height(235.dp))
        Column(Modifier.align(Alignment.BottomStart).padding(bottom = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(MiuixIcons.TopDownloads, null, Modifier.size(20.dp), tint = accent)
                Text("  CHAMPIONSHIP LEADER", fontFamily = RacingDisplayFamily, style = MiuixTheme.textStyles.footnote2, color = accent)
            }
            Text(driver?.name ?: team?.name.orEmpty(), style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))
            Text("${driver?.points ?: team?.points ?: "—"} 积分", fontFamily = RacingDisplayFamily, style = MiuixTheme.textStyles.title3)
        }
    }
}

@Composable
private fun StandingRow(accent: androidx.compose.ui.graphics.Color, position: Int?, name: String, summary: String, points: String?, followed: Boolean, onClick: () -> Unit, identity: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(position?.let { "%02d".format(it) } ?: "—", fontFamily = RacingDisplayFamily, style = MiuixTheme.textStyles.title3,
            modifier = Modifier.width(32.dp), fontWeight = FontWeight.Medium)
        identity()
        Column(Modifier.weight(1f)) {
            Text(name, style = MiuixTheme.textStyles.headline1, fontWeight = FontWeight.Medium)
            Text(summary + if (followed) " · 已关注" else "", color = accent,
                style = MiuixTheme.textStyles.footnote1, modifier = Modifier.padding(top = 5.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(points ?: "—", fontFamily = RacingDisplayFamily, style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.SemiBold)
            Text("积分", style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
        Icon(MiuixIcons.ChevronForward, "查看 $name", Modifier.size(18.dp), tint = MiuixTheme.colorScheme.onSurfaceVariantActions)
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MiuixTheme.colorScheme.dividerLine)
}
