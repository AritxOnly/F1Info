package com.aritxonly.f1info.ui.racing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aritxonly.f1info.data.racing.*
import com.aritxonly.f1info.ui.components.*
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.preference.*
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun LiveSessionsContent(season: Int, onSelected: () -> Unit) {
    val context = LocalContext.current
    var year by remember { mutableIntStateOf(season) }
    var rows by remember { mutableStateOf<List<LiveSession>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var reload by remember { mutableIntStateOf(0) }
    LaunchedEffect(year, reload) {
        loading = true; error = null
        try { rows = LiveFeed.sessions(year) } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e; error = e.message }
        loading = false
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = secondaryContentPadding()) {
        item {
            val seasons = (season downTo 2023).toList()
            SettingsSection {
                OverlayDropdownPreference(title = "赛季", items = seasons.map { "$it 赛季" },
                    selectedIndex = seasons.indexOf(year), onSelectedIndexChange = { year = seasons[it] })
                ArrowPreference(title = "跟随最新场次", summary = "自动选择最近的比赛",
                    onClick = { LiveFeed.select(null); LiveFeed.requestRefresh(context); onSelected() })
            }
            SectionTitle("比赛场次", "设备本地时间")
            if (loading) CircularProgressIndicator(Modifier.padding(16.dp))
            error?.let { Text(it); TextButton("重试", { reload++ }) }
        }
        items(rows, key = { it.key }) { session ->
            SettingsSection { ArrowPreference(title = session.location + " · " + session.name,
                summary = session.start.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")), onClick = {
                com.aritxonly.f1info.notification.LiveRaceService.stop(context)
                LiveFeed.select(session.key); LiveFeed.requestRefresh(context); onSelected()
            }) }
        }
    }
}
