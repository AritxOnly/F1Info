package com.aritxonly.f1info.widgets

import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.glance.appwidget.compose
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import com.aritxonly.f1info.ui.components.SettingsSection
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.time.Instant

/** Uses the exact Glance RemoteViews composition, so previews match desktop layout. */
@Composable
fun WidgetPreviewGallery(category: Int, revision: Instant?) {
    val context = LocalContext.current
    var currentCategory by remember { mutableIntStateOf(category) }
    var sizeIndex by remember { mutableIntStateOf(0) }
    val portrait = currentCategory == 1
    val wide = currentCategory >= 2 || sizeIndex > 0
    val expanded = !portrait && sizeIndex == 2
    val available = (LocalConfiguration.current.screenWidthDp - 32).coerceAtLeast(230)
    val width = if (!wide || portrait) 160.dp else minOf(340, available).dp
    val height = if (expanded) 340.dp else 160.dp
    val size = DpSize(width, height)
    var remote by remember { mutableStateOf<RemoteViews?>(null) }
    var error by remember { mutableStateOf(false) }
    LaunchedEffect(currentCategory, size, revision, MiuixTheme.colorScheme.surface) {
        remote = null; error = false
        val widget = when (currentCategory) {
            1 -> DriverPortraitWidget()
            2 -> DriversStandingsWidget()
            3 -> TeamsStandingsWidget()
            else -> F1Widget()
        }
        runCatching { widget.compose(context, size = size) }.onSuccess { remote = it }.onFailure {
            android.util.Log.e("F1WidgetPreview", "Widget composition failed", it); error = true
        }
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = com.aritxonly.f1info.ui.components.secondaryContentPadding()) {
        item {
            Text("与桌面相同，显示最近刷新的比赛和积分。", style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            SettingsSection {
                OverlayDropdownPreference(
                    title = "微件类型",
                    items = listOf("F1 围场赛程", "关注车手卡片", "车手积分榜", "车队积分榜"),
                    selectedIndex = currentCategory,
                    onSelectedIndexChange = {
                        currentCategory = it
                        sizeIndex = 0
                    }
                )
                if (!portrait) {
                    val choices = if (currentCategory == 0) listOf(0, 1, 2) else listOf(1, 2)
                    OverlayDropdownPreference(
                        title = "预览尺寸",
                        items = choices.map { listOf("2×2", "4×2", "4×4")[it] },
                        selectedIndex = choices.indexOf(if (currentCategory >= 2 && sizeIndex == 0) 1 else sizeIndex),
                        onSelectedIndexChange = { sizeIndex = choices[it] }
                    )
                }
            }
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (remote != null) AndroidView(factory = { FrameLayout(it) }, modifier = Modifier.size(width, height), update = { host ->
                    host.removeAllViews()
                    remote?.let { views -> host.addView(views.apply(context, host), FrameLayout.LayoutParams(-1, -1)) }
                }) else Box(Modifier.size(width, height), contentAlignment = Alignment.Center) {
                    if (error) Text("预览暂时不可用", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    else CircularProgressIndicator()
                }
            }
            val targetReceiver: Class<out RacingWidgetReceiver> = when (currentCategory) {
                1 -> DriverPortraitWidgetReceiver::class.java
                2 -> if (sizeIndex == 2) DriversLargeWidgetReceiver::class.java else DriversStandingsWidgetReceiver::class.java
                3 -> if (sizeIndex == 2) TeamsLargeWidgetReceiver::class.java else TeamsStandingsWidgetReceiver::class.java
                else -> when (sizeIndex) {
                    1 -> ScheduleWideWidgetReceiver::class.java
                    2 -> ScheduleLargeWidgetReceiver::class.java
                    else -> F1WidgetReceiver::class.java
                }
            }
            val aospReceiver: Class<out RacingWidgetReceiver> = when (currentCategory) {
                1 -> DriverPortraitAospWidgetReceiver::class.java
                2 -> if (sizeIndex == 2) DriversLargeAospWidgetReceiver::class.java else DriversStandingsAospWidgetReceiver::class.java
                3 -> if (sizeIndex == 2) TeamsLargeAospWidgetReceiver::class.java else TeamsStandingsAospWidgetReceiver::class.java
                else -> when (sizeIndex) {
                    1 -> ScheduleWideAospWidgetReceiver::class.java
                    2 -> ScheduleLargeAospWidgetReceiver::class.java
                    else -> F1AospWidgetReceiver::class.java
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    text = "添加澎湃卡片",
                    onClick = { WidgetConfigurationStore.requestPin(context, targetReceiver) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
                TextButton(
                    text = "添加原生组件",
                    onClick = { WidgetConfigurationStore.requestPin(context, aospReceiver) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors()
                )
            }
            Text("双通道架构：澎湃卡片支持曝光按需刷新；原生组件在桌面所有应用抽屉中100%永久可用。",
                style = MiuixTheme.textStyles.footnote2, modifier = Modifier.padding(top = 12.dp), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
    }
}
