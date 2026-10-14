package com.aritxonly.f1info.ui.settings

import android.appwidget.AppWidgetManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aritxonly.f1info.data.racing.LiveCredentials
import com.aritxonly.f1info.notification.*
import com.aritxonly.f1info.platform.rememberHyperModifierLift
import com.aritxonly.f1info.ui.components.*
import com.aritxonly.f1info.widgets.*
import kotlinx.coroutines.*
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsContent(open: (String) -> Unit) {
    val context = LocalContext.current
    var authorized by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(IslandWhitelistBypass.mode(context)) }
    var showWidgetSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    androidx.lifecycle.compose.LifecycleStartEffect(context) {
        mode = IslandWhitelistBypass.mode(context)
        val job = scope.launch { authorized = withContext(Dispatchers.IO) { LiveCredentials.configured(context) } }
        onStopOrDispose { job.cancel() }
    }
    val lift = rememberHyperModifierLift()

    val pinWidget: (Class<out RacingWidgetReceiver>, String) -> Unit = { receiverClass, widgetName ->
        val manager = context.getSystemService(AppWidgetManager::class.java)
        if (manager?.isRequestPinAppWidgetSupported == true) {
            val hasPerm = WidgetConfigurationStore.hasShortcutPermission(context)
            if (!hasPerm) {
                Toast.makeText(context, "若未弹出桌面确认框，请在系统设置中允许【桌面快捷方式】权限", Toast.LENGTH_LONG).show()
            }
            WidgetConfigurationStore.requestPin(context, receiverClass)
            Toast.makeText(context, "已申请添加 $widgetName，请在系统弹窗中确认", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "当前桌面不支持一键添加微件，请在桌面长按手动添加", Toast.LENGTH_LONG).show()
        }
        scope.launch {
            refreshF1Widgets(context)
        }
        showWidgetSheet = false
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = secondaryContentPadding()) {
        item {
            SectionTitle("比赛与数据")
            SettingsSection {
                ArrowPreference(title = "导入日历日程", summary = "将比赛周末加入你的日历", onClick = { open("calendar") })
                ArrowPreference(title = "OpenF1 API", summary = if (authorized) "实时授权已保存" else "历史数据免费 · 实时数据需 Sponsor 授权", onClick = { open("openf1-settings") })
            }
            SectionTitle("桌面小组件")
            SettingsSection {
                ArrowPreference(
                    title = "添加澎湃小组件",
                    summary = "一键添加 2×2 / 4×2 / 4×4 澎湃卡片到主屏幕",
                    onClick = { showWidgetSheet = true }
                )
                ArrowPreference(
                    title = "小组件实时预览库",
                    summary = "查看所有尺寸小组件实时渲染与配置",
                    onClick = { open("widget-gallery") }
                )
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

    if (showWidgetSheet) {
        Dialog(
            onDismissRequest = { showWidgetSheet = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.52f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showWidgetSheet = false }
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(MiuixTheme.colorScheme.surfaceContainer)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {}
                        .padding(20.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "添加澎湃小组件",
                                style = MiuixTheme.textStyles.title2,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "选择要添加至主屏幕的微件样式",
                                style = MiuixTheme.textStyles.footnote2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        IconButton(onClick = { showWidgetSheet = false }) {
                            Icon(MiuixIcons.Close, "关闭", Modifier.size(20.dp), tint = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SectionTitle("澎湃 OS 桌面小组件 (推荐)")
                        SettingsSection {
                            ArrowPreference(
                                title = "澎湃 F1 围场赛程 · 2×2",
                                summary = "推荐 · 下场比赛/倒计时/积分 · 澎湃柔光毛玻璃",
                                onClick = { pinWidget(F1WidgetReceiver::class.java, "澎湃 F1 围场赛程 2×2") }
                            )
                            ArrowPreference(
                                title = "澎湃 F1 赛程与积分 · 4×2",
                                summary = "下场赛程与实时积分大卡片 · 澎湃大卡片",
                                onClick = { pinWidget(ScheduleWideWidgetReceiver::class.java, "澎湃 F1 赛程与积分 4×2") }
                            )
                            ArrowPreference(
                                title = "澎湃 F1 完整赛季赛程 · 4×4",
                                summary = "完整赛季分站赛程 · 超大尺寸",
                                onClick = { pinWidget(ScheduleLargeWidgetReceiver::class.java, "澎湃 F1 完整赛季赛程 4×4") }
                            )
                            ArrowPreference(
                                title = "澎湃关注车手卡片 · 2×2",
                                summary = "关注车手肖像/号码/实时积分 · 澎湃柔光毛玻璃",
                                onClick = { pinWidget(DriverPortraitWidgetReceiver::class.java, "澎湃车手卡片 2×2") }
                            )
                            ArrowPreference(
                                title = "澎湃车手积分榜 · 4×2",
                                summary = "车手排行榜速览 · 澎湃大卡片",
                                onClick = { pinWidget(DriversStandingsWidgetReceiver::class.java, "澎湃车手积分榜 4×2") }
                            )
                            ArrowPreference(
                                title = "澎湃车手积分榜 · 4×4",
                                summary = "完整赛季车手积分榜 · 超大尺寸",
                                onClick = { pinWidget(DriversLargeWidgetReceiver::class.java, "澎湃车手积分榜 4×4") }
                            )
                            ArrowPreference(
                                title = "澎湃车队榜单 · 4×2",
                                summary = "车队积分/涂装/榜单 · 澎湃柔光毛玻璃",
                                onClick = { pinWidget(TeamsStandingsWidgetReceiver::class.java, "澎湃车队榜单 4×2") }
                            )
                            ArrowPreference(
                                title = "澎湃车队榜单 · 4×4",
                                summary = "完整赛季车队积分榜 · 超大尺寸",
                                onClick = { pinWidget(TeamsLargeWidgetReceiver::class.java, "澎湃车队榜单 4×4") }
                            )
                        }

                        SectionTitle("安卓原生标准微件 (通用)")
                        SettingsSection {
                            ArrowPreference(
                                title = "原生 F1 围场赛程 · 2×2",
                                summary = "AOSP 原生风格 · 纯色平铺微件",
                                onClick = { pinWidget(F1AospWidgetReceiver::class.java, "原生 F1 围场赛程 2×2") }
                            )
                            ArrowPreference(
                                title = "原生 F1 赛程卡片 · 4×2",
                                summary = "AOSP 原生风格 · 纯色平铺微件",
                                onClick = { pinWidget(ScheduleWideAospWidgetReceiver::class.java, "原生 F1 赛程卡片 4×2") }
                            )
                            ArrowPreference(
                                title = "原生车手卡片 · 2×2",
                                summary = "AOSP 原生风格 · 纯色平铺微件",
                                onClick = { pinWidget(DriverPortraitAospWidgetReceiver::class.java, "原生车手卡片 2×2") }
                            )
                            ArrowPreference(
                                title = "原生车手积分榜 · 4×2",
                                summary = "AOSP 原生风格 · 纯色平铺微件",
                                onClick = { pinWidget(DriversStandingsAospWidgetReceiver::class.java, "原生车手积分榜 4×2") }
                            )
                            ArrowPreference(
                                title = "原生车队榜单 · 4×2",
                                summary = "AOSP 原生风格 · 纯色平铺微件",
                                onClick = { pinWidget(TeamsStandingsAospWidgetReceiver::class.java, "原生车队榜单 4×2") }
                            )
                        }
                    }
                }
            }
        }
    }
}
