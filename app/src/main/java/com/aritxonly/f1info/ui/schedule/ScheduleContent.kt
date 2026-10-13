package com.aritxonly.f1info.ui.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aritxonly.f1info.model.*
import com.aritxonly.f1info.ui.components.*
import com.aritxonly.f1info.ui.home.NextRaceCard
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.time.Instant

fun LazyListScope.scheduleContent(data: SeasonData, now: Instant, filter: Int, onRace: (Race) -> Unit) {
    if (filter != 2) nextRace(data.races, now)?.let { race -> item("upcoming") { Column(Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("即将举行")
        NextRaceCard(race, now, onRace)
    } } }
    item("heading") { Column(Modifier.padding(horizontal = 16.dp)) { SectionTitle(when (filter) { 1 -> "接下来的比赛"; 2 -> "已结束的比赛"; else -> "完整赛历" }) } }
    val races = data.races.filter { when (filter) { 1 -> !it.isFinished(now); 2 -> it.isFinished(now); else -> true } }
    if (races.isEmpty()) item("empty") { Column(Modifier.padding(horizontal = 16.dp)) { Text("暂无符合条件的比赛", modifier = Modifier.padding(24.dp)) } }
    items(races, key = { "race-${it.round}" }) { race -> RaceRow(race, now, onRace) }
}

@Composable
private fun RaceRow(race: Race, now: Instant, onClick: (Race) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick(race) }.padding(horizontal = 16.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(Modifier.width(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("%02d".format(race.start?.atZone(java.time.ZoneId.systemDefault())?.dayOfMonth ?: race.date.dayOfMonth),
                fontFamily = com.aritxonly.f1info.ui.theme.RacingDisplayFamily, style = MiuixTheme.textStyles.title2, fontWeight = FontWeight.SemiBold)
            Text("${race.start?.atZone(java.time.ZoneId.systemDefault())?.monthValue ?: race.date.monthValue}月",
                style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
        Column(Modifier.weight(1f)) {
            Text(race.name.removeSuffix(" Grand Prix"), style = MiuixTheme.textStyles.headline1, fontWeight = FontWeight.Medium)
            Text("R${race.round} · ${race.circuit.locality}", style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.primary, modifier = Modifier.padding(top = 5.dp))
            Text(if (race.isFinished(now)) "已结束" else "${race.circuit.country} · ${raceDate(race)}",
                style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(top = 3.dp))
        }
        TrackOutline(race.circuit, Modifier.size(65.dp, 50.dp))
        Icon(MiuixIcons.ChevronForward, "查看 ${race.name}", Modifier.size(18.dp), tint = MiuixTheme.colorScheme.onSurfaceVariantActions)
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MiuixTheme.colorScheme.dividerLine)
}
