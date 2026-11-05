package com.aritxonly.f1info.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aritxonly.f1info.calendar.*
import com.aritxonly.f1info.model.*
import com.aritxonly.f1info.ui.components.*
import kotlinx.coroutines.*
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.preference.*
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.time.Instant

@Composable
fun CalendarImportContent(data: SeasonData?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var range by rememberSaveable { mutableIntStateOf(0) }
    var roundIndex by rememberSaveable { mutableIntStateOf(0) }
    var sessions by rememberSaveable { mutableIntStateOf(0) }
    var calendars by remember { mutableStateOf<List<WritableCalendar>>(emptyList()) }
    var calendarId by rememberSaveable { mutableStateOf<Long?>(null) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var calendarMessage by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }
    val races = data?.races.orEmpty().sortedBy { it.round }
    val selectedRaces = if (range == 2) races.getOrNull(roundIndex)?.let(::listOf).orEmpty() else races
    val now = remember(range, roundIndex, sessions, data) { Instant.now() }
    val plan = remember(selectedRaces, range, sessions, now) { planCalendar(selectedRaces, range == 0, sessions == 0, now) }
    var pendingExport by rememberSaveable { mutableStateOf<String?>(null) }
    fun loadCalendars() {
        busy = true
        scope.launch {
            try {
                calendars = withContext(Dispatchers.IO) { CalendarStore.calendars(context) }
                if (calendars.none { it.id == calendarId }) calendarId = calendars.firstOrNull()?.id
                loaded = true
                calendarMessage = if (calendars.isEmpty()) "没有可写日历。请在系统日历添加账号，或导出 ICS 文件。" else null
            } catch (e: Exception) { if (e is CancellationException) throw e; calendarMessage = "读取失败：${e.message}" }
            finally { busy = false }
        }
    }
    val permissions = arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (permissions.all { grants[it] == true }) loadCalendars()
        else { calendars = emptyList(); calendarId = null; loaded = false; calendarMessage = "未获得日历权限。你仍可导出 ICS 文件，在日历应用中导入。" }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/calendar")) { uri ->
        val content = pendingExport
        pendingExport = null
        if (uri != null && content != null) {
            busy = true
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val stream = context.contentResolver.openOutputStream(uri) ?: error("无法打开目标文件")
                        stream.bufferedWriter(Charsets.UTF_8).use { it.write(content) }
                    }
                    message = "ICS 已保存，可在日历应用中导入。"
                } catch (e: Exception) { if (e is CancellationException) throw e; message = "导出失败：${e.message}" }
                finally { busy = false }
            }
        }
    }
    LaunchedEffect(Unit) { if (permissions.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) loadCalendars() }
    LazyColumn(Modifier.fillMaxSize(), state = LocalSecondaryListState.current, contentPadding = secondaryContentPadding()) {
        item {
            SectionTitle("${data?.season ?: "—"} 赛季", "导入系统日历")
            SettingsSection {
                OverlayDropdownPreference(title = "导入范围", items = listOf("即将举行的场次", "整个赛季", "单个大奖赛"), selectedIndex = range,
                    onSelectedIndexChange = { range = it; message = null }, enabled = !busy)
                if (range == 2 && races.isNotEmpty()) OverlayDropdownPreference(title = "大奖赛", items = races.map { "R${it.round} · ${it.name}" },
                    selectedIndex = roundIndex.coerceIn(races.indices), onSelectedIndexChange = { roundIndex = it }, enabled = !busy)
                OverlayDropdownPreference(title = "包含场次", items = listOf("仅正赛", "全部场次（练习 / 排位 / 冲刺 / 正赛）"), selectedIndex = sessions,
                    onSelectedIndexChange = { sessions = it }, enabled = !busy)
            }
            SectionTitle("目标日历")
            SettingsSection {
                if (calendars.isNotEmpty()) OverlayDropdownPreference(title = "写入日历", items = calendars.map { it.label },
                    selectedIndex = calendars.indexOfFirst { it.id == calendarId }.coerceAtLeast(0),
                    onSelectedIndexChange = { calendarId = calendars[it].id }, enabled = !busy)
                ArrowPreference(title = if (loaded) "重新读取日历" else "选择系统日历", summary = "仅在你授权后访问可写日历", enabled = !busy, onClick = {
                    if (permissions.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) loadCalendars()
                    else permissionLauncher.launch(permissions)
                })
            }
            calendarMessage?.let { Text(it, Modifier.padding(vertical = 12.dp), style = MiuixTheme.textStyles.body2) }
            SectionTitle("导入预览", "${plan.entries.size} 场")
            SettingsSection {
                if (data == null) Text("赛程尚未加载，请返回主页刷新。", Modifier.padding(22.dp))
                plan.entries.take(3).forEach { entry ->
                    BasicComponent(title = entry.title, summary = sessionTime(Session("", entry.start.atZone(java.time.ZoneId.systemDefault()).toLocalDate(), entry.start)))
                }
                if (plan.entries.size > 3) Text("另有 ${plan.entries.size - 3} 场", Modifier.padding(horizontal = 22.dp, vertical = 12.dp), style = MiuixTheme.textStyles.footnote2)
                Text("按设备时区显示。结束时间为估算：正赛 2 小时，其他场次 1 小时。${if (plan.unknownTimes > 0) "\n${plan.unknownTimes} 场时间待公布，将跳过。" else ""}\n再次导入会更新此应用在所选日历中写入的日程；不会自动同步后续赛程变更。",
                    Modifier.padding(22.dp), style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
            message?.let { Text(it, Modifier.padding(vertical = 12.dp), style = MiuixTheme.textStyles.body2) }
            TextButton(if (busy) "正在处理…" else "导入 ${plan.entries.size} 场日程", {
                val target = calendarId ?: return@TextButton
                busy = true; message = null
                scope.launch {
                    try {
                        val result = withContext(Dispatchers.IO) { CalendarStore.import(context, target, plan.entries) }
                        message = "导入完成：新增 ${result.inserted} 场，更新 ${result.updated} 场。"
                    } catch (e: Exception) { if (e is CancellationException) throw e; message = "导入失败：${e.message}。可重新读取日历后重试。" }
                    finally { busy = false }
                }
            }, Modifier.fillMaxWidth().padding(top = 12.dp), colors = ButtonDefaults.textButtonColorsPrimary(), enabled = !busy && calendarId != null && plan.entries.isNotEmpty())
            SettingsSection {
                ArrowPreference(title = "导出 ICS 文件", summary = "无需日历权限 · 适用于其他设备与日历应用", enabled = !busy && plan.entries.isNotEmpty(), onClick = {
                    pendingExport = calendarIcs(plan.entries, Instant.now())
                    exportLauncher.launch("F1Info-${data?.season}-schedule.ics")
                })
            }
        }
    }
}
