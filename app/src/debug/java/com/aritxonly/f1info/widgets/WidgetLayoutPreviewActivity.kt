package com.aritxonly.f1info.widgets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.aritxonly.f1info.data.F1Repository
import com.aritxonly.f1info.ui.base.ProvideAdvancedMaterialDialogBlurHost
import com.aritxonly.f1info.ui.components.SecondaryPage
import com.aritxonly.f1info.ui.theme.LocalAdvancedMaterialBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.*

/** Debug-only harness for exact Glance RemoteViews and the production editor. */
class WidgetLayoutPreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(androidx.activity.SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            androidx.activity.SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT))
        window.isNavigationBarContrastEnforced = false
        com.aritxonly.f1info.ui.components.vectorLogos.values.distinct().forEach {
            requireNotNull(getDrawable(it)) { "Vector logo failed native inflation: $it" }
        }
        android.util.Log.i("F1WidgetPreview", "All team vector logos passed native Android inflation")
        setContent {
            WidgetEditorTheme {
                val backdrop = rememberLayerBackdrop()
                CompositionLocalProvider(LocalAdvancedMaterialBackdrop provides backdrop) {
                    ProvideAdvancedMaterialDialogBlurHost {
                        SecondaryPage("小组件排版预览", MiuixTheme.colorScheme.primary, { finish() }, brandStyling = false) {
                            val provider = intent.getStringExtra("provider") ?: "F1WidgetReceiver"
                            val data by produceState<com.aritxonly.f1info.model.SeasonData?>(null) {
                                value = F1Repository(this@WidgetLayoutPreviewActivity).cached()
                            }
                            WidgetConfigurationEditor(WidgetConfiguration(opacity = intent.getFloatExtra("opacity", .96f),
                                topPadding = intent.getIntExtra("top", 0), bottomPadding = intent.getIntExtra("bottom", 0),
                                marginPreset = WidgetMarginPreset.fromStoredValue(intent.getStringExtra("preset")),
                                cornerRadiusDp = intent.getIntExtra("corner", 22)).normalized(),
                                provider.contains("Portrait"), data?.drivers.orEmpty(), provider) { finish() }
                        }
                    }
                }
            }
        }
    }
}
