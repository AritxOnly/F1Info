package com.aritxonly.f1info.ui.racing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aritxonly.f1info.data.racing.*
import com.aritxonly.f1info.ui.components.*
import com.aritxonly.f1info.ui.navigation.ImmersiveTextTabRow
import com.aritxonly.f1info.ui.theme.RacingDisplayFamily
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ResultsContent(season: Int, round: Int, category: Int) {
    val context = LocalContext.current
    var reload by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var result by remember { mutableStateOf<ResultsLoad?>(null) }
    LaunchedEffect(season, round, category, reload) {
        loading = true
        result = ResultsRepository.load(context, season, round, listOf("results", "qualifying", "sprint")[category])
        loading = false
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = secondaryContentPadding()) {
        item {
            Text("$season · ROUND $round", fontFamily = RacingDisplayFamily, color = MiuixTheme.colorScheme.primary)
            if (loading) CircularProgressIndicator(Modifier.padding(20.dp))
            result?.error?.let { Text(it + if (result?.cached == true) "\n正在显示离线缓存" else "", color = MiuixTheme.colorScheme.onSurfaceVariantSummary) }
            if (!loading && result?.rows.isNullOrEmpty()) SettingsSection { Text("该场次结果尚未发布或未举办。", Modifier.padding(20.dp)) }
            if (result?.error != null) TextButton("重新加载", { reload++ }, Modifier.fillMaxWidth())
        }
        items(result?.rows.orEmpty(), key = { it.driverId }) { row ->
            SettingsSection {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(row.position.padStart(2, '0'), fontFamily = RacingDisplayFamily, style = MiuixTheme.textStyles.title3)
                    Column(Modifier.weight(1f)) {
                        Text(row.name, style = MiuixTheme.textStyles.headline1)
                        Text(row.team, style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                        Text(row.time.ifBlank { row.status.ifBlank { "时间待公布" } }, style = MiuixTheme.textStyles.footnote1, modifier = Modifier.padding(top = 8.dp))
                        if (category != 1) Text("发车 ${row.grid} · ${row.laps} 圈 · ${row.status}", style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    }
                    if (category != 1) Column { Text(row.points, fontFamily = RacingDisplayFamily, style = MiuixTheme.textStyles.title3); Text("积分", style = MiuixTheme.textStyles.footnote2) }
                }
            }
        }
        item { Text("Jolpica F1 · 正式赛果以 FIA 公布为准", style = MiuixTheme.textStyles.footnote2, modifier = Modifier.padding(top = 24.dp)) }
    }
}
