package com.aritxonly.f1info.widgets

import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.glance.appwidget.compose
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

private class ConfigurationPreview(kind: WidgetKind, configuration: WidgetConfiguration) :
    RacingWidget(kind, previewConfiguration = configuration)

@Composable
internal fun WidgetLivePreview(configuration: WidgetConfiguration, providerName: String) {
    val context = LocalContext.current
    val kind = when {
        providerName.contains("Portrait") -> WidgetKind.Portrait
        providerName.contains("Drivers") -> WidgetKind.Drivers
        providerName.contains("Teams") -> WidgetKind.Teams
        else -> WidgetKind.Schedule
    }
    val large = providerName.contains("Large")
    val wide = large || providerName.contains("Wide") || kind == WidgetKind.Drivers || kind == WidgetKind.Teams
    val width = (if (wide) minOf(340, LocalConfiguration.current.screenWidthDp - 32) else 160).dp
    val size = DpSize(width, if (large) 320.dp else 160.dp)
    var remote by remember { mutableStateOf<RemoteViews?>(null) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(configuration, providerName) {
        failed = false
        delay(180)
        runCatching { ConfigurationPreview(kind, configuration).compose(context, size = size) }
            .onSuccess { remote = it }
            .onFailure { failed = true; android.util.Log.w("F1WidgetPreview", "Configuration preview failed", it) }
    }
    Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
        val previewColors = if (configuration.lightText) darkColorScheme() else lightColorScheme()
        val surface = previewColors.surface
        val tile = previewColors.surfaceContainer
        Box(Modifier.size(size).clipToBounds()) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(surface)
                val step = 16.dp.toPx()
                for (row in 0..(this.size.height / step).toInt()) {
                    for (column in 0..(this.size.width / step).toInt()) {
                        if ((row + column) % 2 == 0) drawRect(tile, Offset(column * step, row * step), Size(step, step))
                    }
                }
            }
            if (failed) Text("预览暂时不可用", modifier = Modifier.align(Alignment.Center))
            else if (remote == null) Text("正在准备预览", modifier = Modifier.align(Alignment.Center))
            else AndroidView(factory = { FrameLayout(it) }, modifier = Modifier.fillMaxSize(), update = { host ->
                host.removeAllViews()
                remote?.let { views -> host.addView(views.apply(context, host), FrameLayout.LayoutParams(-1, -1)) }
            })
        }
    }
}
