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

/** Visual appearance style: HyperOS soft-light glass or AOSP native Material 3 style */
enum class WidgetThemeStyle(val label: String) {
    HyperOS("澎湃柔光"),
    Material("原生 Material");

    companion object {
        fun fromStoredValue(value: String?) = entries.firstOrNull { it.name == value } ?: HyperOS
    }
}

/** Per-instance values. topPadding/bottomPadding are retained storage-compatible custom margin drafts. */
data class WidgetConfiguration(
    val opacity: Float = .96f,
    val topPadding: Int = 0,
    val bottomPadding: Int = 0,
    val driverId: String = "",
    val lightText: Boolean = true,
    val marginPreset: WidgetMarginPreset = WidgetMarginPreset.Default,
    val cornerRadiusDp: Int = DEFAULT_CORNER_RADIUS_DP,
    val themeStyle: WidgetThemeStyle = WidgetThemeStyle.HyperOS
) {
    fun topMarginDp() = if (marginPreset == WidgetMarginPreset.Custom) topPadding else marginPreset.cardTopMarginDp
    fun bottomMarginDp() = if (marginPreset == WidgetMarginPreset.Custom) bottomPadding else marginPreset.cardBottomMarginDp
    fun isDefaultCornerRadius() = cornerRadiusDp == DEFAULT_CORNER_RADIUS_DP
    fun effectiveCornerRadiusDp() = if (themeStyle == WidgetThemeStyle.HyperOS && isDefaultCornerRadius()) 16 else cornerRadiusDp
    fun normalized(): WidgetConfiguration {
        val top = topPadding.coerceIn(0, 28)
        return copy(opacity = if (opacity.isFinite()) opacity.coerceIn(0f, 1f) else .96f,
            topPadding = top, bottomPadding = bottomPadding.coerceIn(0, 28).coerceAtMost(40 - top),
            cornerRadiusDp = cornerRadiusDp.coerceIn(8, 32))
    }

    companion object {
        const val DEFAULT_CORNER_RADIUS_DP = 22
    }
}
object WidgetConfigurationStore {
    fun read(context: Context, id: Int): WidgetConfiguration {
        val p = context.getSharedPreferences("widget_configuration", 0)
        val fallback = context.getSharedPreferences("appearance", 0).getFloat("widget_background_opacity", .96f)
        return WidgetConfiguration(
            p.getFloat("$id.opacity", fallback),
            p.getInt("$id.top", 0),
            p.getInt("$id.bottom", 0),
            p.getString("$id.driver", "").orEmpty(),
            p.getBoolean("$id.light", true),
            WidgetMarginPreset.fromStoredValue(p.getString("$id.margin_preset", null)),
            p.getInt("$id.corner_radius", WidgetConfiguration.DEFAULT_CORNER_RADIUS_DP),
            WidgetThemeStyle.fromStoredValue(p.getString("$id.theme_style", null))
        ).normalized()
    }
    fun write(context: Context, id: Int, configuration: WidgetConfiguration) {
        val c = configuration.normalized()
        context.getSharedPreferences("widget_configuration", 0).edit().putFloat("$id.opacity", c.opacity)
            .putInt("$id.top", c.topPadding).putInt("$id.bottom", c.bottomPadding)
            .putString("$id.margin_preset", c.marginPreset.name).putInt("$id.corner_radius", c.cornerRadiusDp)
            .putString("$id.driver", c.driverId).putBoolean("$id.light", c.lightText)
            .putString("$id.theme_style", c.themeStyle.name).apply()
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
    fun hasShortcutPermission(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? android.app.AppOpsManager
            if (appOps != null) {
                val checkOp = appOps.javaClass.getMethod("checkOpNoThrow", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, String::class.java)
                val result = checkOp.invoke(appOps, 10017, android.os.Process.myUid(), context.packageName) as Int
                result == android.app.AppOpsManager.MODE_ALLOWED
            } else true
        } catch (_: Throwable) {
            true
        }
    }
    fun requestPin(context: Context, providerClass: Class<*>) {
        val manager = context.getSystemService(AppWidgetManager::class.java) ?: return
        val provider = android.content.ComponentName(context, providerClass)
        if (manager.isRequestPinAppWidgetSupported) {
            val callback = android.app.PendingIntent.getBroadcast(
                context, 0, Intent("com.aritxonly.f1info.PIN_WIDGET_SUCCESS"),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            manager.requestPinAppWidget(provider, null, callback)
        }
    }
}
