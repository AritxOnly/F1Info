package com.aritxonly.f1info.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aritxonly.f1info.data.racing.LiveCredentials
import com.aritxonly.f1info.notification.*
import com.aritxonly.f1info.platform.rememberHyperModifierLift
import com.aritxonly.f1info.ui.components.*
import kotlinx.coroutines.*
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsContent(open: (String) -> Unit) {
    val context = LocalContext.current
    var authorized by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(IslandWhitelistBypass.mode(context)) }
    val scope = rememberCoroutineScope()
    androidx.lifecycle.compose.LifecycleStartEffect(context) {
        mode = IslandWhitelistBypass.mode(context)
        val job = scope.launch { authorized = withContext(Dispatchers.IO) { LiveCredentials.configured(context) } }
        onStopOrDispose { job.cancel() }
    }
    val lift = rememberHyperModifierLift()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = secondaryContentPadding()) {
        item {
            SectionTitle("比赛与数据")
            SettingsSection {
                ArrowPreference(title = "导入日历日程", summary = "将比赛周末加入你的日历", onClick = { open("calendar") })
                ArrowPreference(title = "OpenF1 API", summary = if (authorized) "实时授权已保存" else "历史数据免费 · 实时数据需 Sponsor 授权", onClick = { open("openf1-settings") })
            }
            SectionTitle("通知与显示")
            SettingsSection {
                ArrowPreference(title = "超级岛", summary = when (mode) { IslandBypassMode.ROOT -> "Root 推送"; IslandBypassMode.SHIZUKU -> "Shizuku 推送"; else -> "常规动态通知" }, onClick = { open("island-settings") })
                InfoRow("底栏位置", "HyperModifier · ${lift.toInt()} dp")
                Text("底栏抬高跟随 HyperModifier 的公开设置；未安装时使用默认位置。小组件外观与关注对象在桌面小组件的编辑页调整。",
                    Modifier.padding(horizontal = 22.dp, vertical = 12.dp), style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
            SectionTitle("F1Info")
            SettingsSection {
                ArrowPreference(title = "关于 F1Info", summary = "你的围场，每一个比赛周末 · ${com.aritxonly.f1info.BuildConfig.VERSION_NAME}", onClick = { open("about") })
            }
            Text("非官方 F1 信息应用", Modifier.fillMaxWidth().padding(vertical = 20.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
    }
}
