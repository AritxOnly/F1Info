package com.aritxonly.f1info.ui.racing

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.aritxonly.f1info.data.racing.*
import com.aritxonly.f1info.notification.LiveRaceService
import com.aritxonly.f1info.ui.components.*
import com.aritxonly.f1info.ui.theme.RacingDisplayFamily
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun LazyListScope.liveContent(state: LiveFeed.State, now: Instant, onSessions: () -> Unit, onSettings: () -> Unit) {
    val data = state.data
    item("live-header") { LiveHeader(state, now, onSessions, onSettings) }
    if (data != null) {
        item("positions-title") { Box(Modifier.padding(horizontal = 16.dp)) { SectionTitle(if (data.session.active(now)) "赛道位置" else "最终排名", "间隔 / 差距") } }
        if (data.positions.isEmpty()) item { Text("该场次暂无排名数据", modifier = Modifier.padding(16.dp)) }
        items(data.positions, key = { "position-${it.number}" }) { position ->
            val driver = data.drivers.firstOrNull { it.number == position.number }
            Box(Modifier.padding(horizontal = 16.dp)) { SettingsSection {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(position.position?.toString()?.padStart(2, '0') ?: position.classification.ifBlank { "—" }, fontFamily = RacingDisplayFamily, style = MiuixTheme.textStyles.title3)
                    Column(Modifier.weight(1f)) {
                        Text(driver?.name ?: "#${position.number}", style = MiuixTheme.textStyles.headline1)
                        Text(driver?.team.orEmpty(), style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    }
                    Text(formatRaceGap(data.intervals.firstOrNull { it.number == position.number }?.gap, position.position), fontFamily = RacingDisplayFamily, style = MiuixTheme.textStyles.footnote1)
                }
            } }
        }
        item("control-title") { Box(Modifier.padding(horizontal = 16.dp)) { SectionTitle("比赛动态", "Race Control") } }
        if (data.events.isEmpty()) item { Text("暂无赛会动态", modifier = Modifier.padding(16.dp)) }
        items(data.events, key = { "event-${it.key}" }) { event ->
            Box(Modifier.padding(horizontal = 16.dp)) { SettingsSection {
                Column(Modifier.padding(16.dp)) {
                    Text(event.date.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm:ss")) + (event.lap?.let { " · LAP $it" } ?: "") + if (event.flag.isBlank()) "" else " · ${event.flag}",
                        fontFamily = RacingDisplayFamily, style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.primary)
                    Text(event.message, style = MiuixTheme.textStyles.body2, modifier = Modifier.padding(top = 8.dp))
                }
            } }
        }
    }
    item { Text("OpenF1 · 约每 15 秒更新 · 设备本地时间\n历史动态免费，赛中实时数据需要 OpenF1 Sponsor 授权。", style = MiuixTheme.textStyles.footnote2, modifier = Modifier.padding(16.dp), color = MiuixTheme.colorScheme.onSurfaceVariantSummary) }
}

@Composable
private fun LiveHeader(state: LiveFeed.State, now: Instant, onSessions: () -> Unit, onSettings: () -> Unit) {
    val context = LocalContext.current
    var message by remember { mutableStateOf<String?>(null) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) LiveRaceService.start(context) else message = "需要通知权限才能在后台显示比赛动态。"
    }
    val data = state.data
    val monitoring by LiveRaceService.active.collectAsStateWithLifecycle()
    Column(Modifier.padding(horizontal = 16.dp)) {
        SettingsSection {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Text(if (state.error != null && data?.session?.active(now) == true) "数据过期" else data?.label(now) ?: "等待数据", fontFamily = RacingDisplayFamily, color = MiuixTheme.colorScheme.primary)
                Text(data?.session?.location ?: "围场动态", style = MiuixTheme.textStyles.title2, modifier = Modifier.padding(top = 12.dp))
                Text(data?.session?.name ?: "选择场次，查看排名与赛会消息", style = MiuixTheme.textStyles.body2)
                data?.let { Text("更新于 " + it.fetchedAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MM/dd HH:mm:ss")), style = MiuixTheme.textStyles.footnote2, modifier = Modifier.padding(top = 12.dp)) }
                if (state.loading && data == null) CircularProgressIndicator(Modifier.padding(top = 12.dp).size(24.dp))
                state.error?.let { Text(it, style = MiuixTheme.textStyles.footnote1, modifier = Modifier.padding(top = 12.dp)) }
                message?.let { Text(it, style = MiuixTheme.textStyles.footnote1) }
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onSessions) { Icon(MiuixIcons.Months, "选择场次 / 历史回看", Modifier.size(24.dp)) }
                        Text("场次", style = MiuixTheme.textStyles.footnote2)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onSettings) { Icon(MiuixIcons.Settings, "实时授权与超级岛", Modifier.size(24.dp)) }
                        Text("实时设置", style = MiuixTheme.textStyles.footnote2)
                    }
                }
            }
            if (data?.session?.active(now) == true || monitoring) {
                SwitchPreference(title = "后台关注", summary = "通过动态通知或超级岛跟踪比赛", checked = monitoring,
                    enabled = monitoring || (state.error == null && data?.label(now) == "LIVE"),
                    onCheckedChange = { enabled ->
                        message = null
                        if (!enabled) { LiveRaceService.stop(context); message = "已停止后台关注" }
                        else if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) LiveRaceService.start(context)
                        else permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    })
            }
        }
    }
}

private fun formatRaceGap(raw: String?, position: Int?): String {
    if (position == 1) return "领先"
    if (raw == null || raw == "—") return "—"
    return raw.toDoubleOrNull()?.let { "+%.3f s".format(java.util.Locale.ROOT, it) } ?: raw
}
