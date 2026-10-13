package com.aritxonly.f1info.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.aritxonly.f1info.data.F1Repository
import com.aritxonly.f1info.ui.components.*
import com.aritxonly.f1info.ui.theme.LocalAdvancedMaterialBackdrop
import com.aritxonly.f1info.ui.base.ProvideAdvancedMaterialDialogBlurHost
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.preference.*
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.*
import kotlin.math.roundToInt

class WidgetConfigurationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(androidx.activity.SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            androidx.activity.SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT))
        window.isNavigationBarContrastEnforced = false
        val id = WidgetConfigurationStore.resolveId(intent)
        val info = AppWidgetManager.getInstance(this).getAppWidgetInfo(id)
        if (id == AppWidgetManager.INVALID_APPWIDGET_ID || info?.provider?.packageName != packageName) { finish(); return }
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        setResult(Activity.RESULT_CANCELED, result)
        val portrait = info.provider.className.contains("Portrait")
        setContent {
            WidgetEditorTheme {
                val drivers by produceState<List<com.aritxonly.f1info.model.Driver>>(emptyList()) {
                    value = F1Repository(this@WidgetConfigurationActivity).cached()?.drivers.orEmpty()
                }
                val backdrop = rememberLayerBackdrop()
                CompositionLocalProvider(LocalAdvancedMaterialBackdrop provides backdrop) {
                    ProvideAdvancedMaterialDialogBlurHost {
                        SecondaryPage("编辑小组件", MiuixTheme.colorScheme.primary, { finish() }, brandStyling = false) {
                            WidgetConfigurationEditor(WidgetConfigurationStore.read(this, id), portrait,
                                drivers, info.provider.className) { configuration ->
                                WidgetConfigurationStore.write(this, id, configuration)
                                WidgetConfigurationStore.registerEditor(this, intArrayOf(id))
                                lifecycleScope.launch {
                                    refreshF1Widgets(this@WidgetConfigurationActivity)
                                    setResult(Activity.RESULT_OK, result); finish()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun WidgetConfigurationEditor(initial: WidgetConfiguration, portrait: Boolean,
    drivers: List<com.aritxonly.f1info.model.Driver>, providerName: String, onSave: (WidgetConfiguration) -> Unit) {
    var configuration by remember { mutableStateOf(initial) }
    Box(Modifier.fillMaxSize()) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = secondaryContentPadding(extraBottom = 100.dp)) {
        item {
            Text("每个小组件独立保存外观。透明背景可选择浅色或深色文字来搭配壁纸。",
                style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            WidgetLivePreview(configuration, providerName)
            SectionTitle("外观")
            SettingsSection(containerAlpha = 1f) {
                SliderPreference(configuration.opacity, { configuration = configuration.copy(opacity = it) },
                    title = "背景不透明度", valueText = "${(configuration.opacity * 100).roundToInt()}%")
                OverlayDropdownPreference(title = "文字颜色", items = listOf("浅色文字", "深色文字"),
                    selectedIndex = if (configuration.lightText) 0 else 1,
                    summary = "选择与壁纸对比更清晰的文字",
                    onSelectedIndexChange = { configuration = configuration.copy(lightText = it == 0) })
            }
            SectionTitle("布局")
            SettingsSection(containerAlpha = 1f) {
                OverlayDropdownPreference(title = "背景卡片边距", items = WidgetMarginPreset.entries.map { it.label },
                    selectedIndex = WidgetMarginPreset.entries.indexOf(configuration.marginPreset),
                    summary = "上 ${configuration.topMarginDp()} dp · 下 ${configuration.bottomMarginDp()} dp，作用于整张背景卡片",
                    onSelectedIndexChange = { configuration = configuration.copy(marginPreset = WidgetMarginPreset.entries[it]) })
                if (configuration.marginPreset == WidgetMarginPreset.Custom) {
                    SliderPreference(configuration.topPadding.toFloat(), { configuration = configuration.copy(topPadding = it.roundToInt()).normalized() },
                        title = "卡片上边距", valueText = "${configuration.topPadding} dp", valueRange = 0f..28f, steps = 27)
                    SliderPreference(configuration.bottomPadding.toFloat(), { configuration = configuration.copy(bottomPadding = it.roundToInt()).normalized() },
                        title = "卡片下边距", valueText = "${configuration.bottomPadding} dp", valueRange = 0f..28f, steps = 27)
                    Text("上下边距合计最多 40 dp；卡片内容保留固定内边距。", Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                        style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
                SliderPreference(configuration.cornerRadiusDp.toFloat(), { configuration = configuration.copy(cornerRadiusDp = it.roundToInt()) },
                    title = "背景圆角", valueText = "${configuration.cornerRadiusDp} dp", valueRange = 8f..32f, steps = 23)
            }
        }
        if (portrait) item {
            SectionTitle("关注车手")
            val options = listOf("跟随首页关注车手") + drivers.map { it.name }
            val selected = drivers.indexOfFirst { it.id == configuration.driverId } + 1
            SettingsSection(containerAlpha = 1f) {
                OverlayDropdownPreference(title = "显示车手", items = options, selectedIndex = selected,
                    summary = "仅修改当前小组件", maxHeight = 400.dp,
                    onSelectedIndexChange = { configuration = configuration.copy(driverId = drivers.getOrNull(it - 1)?.id.orEmpty()) })
            }
        }
    }
    TextButton("保存", { onSave(configuration.normalized()) }, Modifier.align(Alignment.BottomCenter)
        .navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
        colors = ButtonDefaults.textButtonColorsPrimary())
    }
}
