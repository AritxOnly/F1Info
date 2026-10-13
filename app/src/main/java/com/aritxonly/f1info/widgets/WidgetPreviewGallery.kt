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
    var sizeIndex by remember { mutableIntStateOf(0) }
    val portrait = category == 1
    val wide = category >= 2 || sizeIndex > 0
    val expanded = !portrait && sizeIndex == 2
    val available = (LocalConfiguration.current.screenWidthDp - 32).coerceAtLeast(230)
    val width = if (!wide || portrait) 160.dp else minOf(340, available).dp
    val height = if (expanded) 340.dp else 160.dp
    val size = DpSize(width, height)
    var remote by remember { mutableStateOf<RemoteViews?>(null) }
    var error by remember { mutableStateOf(false) }
    LaunchedEffect(category, size, revision, MiuixTheme.colorScheme.surface) {
        remote = null; error = false
        val widget = when (category) {
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
            if (!portrait) SettingsSection {
                val choices = if (category == 0) listOf(0, 1, 2) else listOf(1, 2)
                OverlayDropdownPreference(title = "预览尺寸", items = choices.map { listOf("2×2", "4×2", "4×4")[it] },
                    selectedIndex = choices.indexOf(if (category >= 2 && sizeIndex == 0) 1 else sizeIndex),
                    onSelectedIndexChange = { sizeIndex = choices[it] })
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
            Text("在桌面小组件列表添加对应尺寸；长按小组件编辑外观与关注车手。",
                style = MiuixTheme.textStyles.footnote2, modifier = Modifier.padding(top = 18.dp), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
    }
}
