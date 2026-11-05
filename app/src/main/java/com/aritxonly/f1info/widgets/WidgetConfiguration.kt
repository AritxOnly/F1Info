package com.aritxonly.f1info.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent

/** Margin contract follows Deadliner TaskWidgetPaddingPreset; names follow the requested launcher layouts. */
enum class WidgetMarginPreset(val label: String, val cardTopMarginDp: Int, val cardBottomMarginDp: Int) {
    Default("默认", 0, 0),
    Grid4x6("4×6", 16, 2),
    Grid4x7NoLabel("4×7 无字", 12, 0),
    Grid4x6NoLabel("4×6 无字", 18, 2),
    Custom("自定义", 0, 0);

    companion object {
        fun fromStoredValue(value: String?) = entries.firstOrNull { it.name == value } ?: Default
    }
}

/** Per-instance values. topPadding/bottomPadding are retained storage-compatible custom margin drafts. */
data class WidgetConfiguration(val opacity: Float = .96f, val topPadding: Int = 0,
    val bottomPadding: Int = 0, val driverId: String = "", val lightText: Boolean = true,
    val marginPreset: WidgetMarginPreset = WidgetMarginPreset.Default, val cornerRadiusDp: Int = 22) : java.io.Serializable {
    fun topMarginDp() = if (marginPreset == WidgetMarginPreset.Custom) topPadding else marginPreset.cardTopMarginDp
    fun bottomMarginDp() = if (marginPreset == WidgetMarginPreset.Custom) bottomPadding else marginPreset.cardBottomMarginDp
    fun normalized(): WidgetConfiguration {
        val top = topPadding.coerceIn(0, 28)
        return copy(opacity = if (opacity.isFinite()) opacity.coerceIn(0f, 1f) else .96f,
            topPadding = top, bottomPadding = bottomPadding.coerceIn(0, 28).coerceAtMost(40 - top),
            cornerRadiusDp = cornerRadiusDp.coerceIn(8, 32))
    }
}
object WidgetConfigurationStore {
    fun read(context: Context, id: Int): WidgetConfiguration {
        val p = context.getSharedPreferences("widget_configuration", 0)
        val fallback = context.getSharedPreferences("appearance", 0).getFloat("widget_background_opacity", .96f)
        return WidgetConfiguration(p.getFloat("$id.opacity", fallback), p.getInt("$id.top", 0),
            p.getInt("$id.bottom", 0), p.getString("$id.driver", "").orEmpty(), p.getBoolean("$id.light", true),
            WidgetMarginPreset.fromStoredValue(p.getString("$id.margin_preset", null)), p.getInt("$id.corner_radius", 22)).normalized()
    }
    fun write(context: Context, id: Int, configuration: WidgetConfiguration) {
        val c = configuration.normalized()
        context.getSharedPreferences("widget_configuration", 0).edit().putFloat("$id.opacity", c.opacity)
            .putInt("$id.top", c.topPadding).putInt("$id.bottom", c.bottomPadding)
            .putString("$id.margin_preset", c.marginPreset.name).putInt("$id.corner_radius", c.cornerRadiusDp)
            .putString("$id.driver", c.driverId).putBoolean("$id.light", c.lightText).apply()
    }
    fun resolveId(intent: Intent) = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID).takeIf { it != AppWidgetManager.INVALID_APPWIDGET_ID }
        ?: intent.data?.getQueryParameter("miuiWidgetId")?.toIntOrNull() ?: AppWidgetManager.INVALID_APPWIDGET_ID
    fun registerEditor(context: Context, ids: IntArray) {
        val manager = AppWidgetManager.getInstance(context)
        ids.forEach { id ->
            val options = manager.getAppWidgetOptions(id)
            val uri = "f1info://com.aritxonly.f1info/widget/WidgetConfigurationActivity?miuiWidgetId=$id"
            if (options.getString("miuiEditUri") != uri) {
                options.putString("miuiEditUri", uri); manager.updateAppWidgetOptions(id, options)
            }
        }
    }
}
