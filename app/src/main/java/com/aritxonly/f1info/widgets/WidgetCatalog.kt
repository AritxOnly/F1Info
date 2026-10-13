package com.aritxonly.f1info.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

internal data class WidgetCatalogEntry(val label: String, val receiver: Class<out RacingWidgetReceiver>)
internal val widgetCatalog = listOf(
    WidgetCatalogEntry("赛程 · 2×2", F1WidgetReceiver::class.java),
    WidgetCatalogEntry("赛程 · 4×2", ScheduleWideWidgetReceiver::class.java),
    WidgetCatalogEntry("赛程 · 4×4", ScheduleLargeWidgetReceiver::class.java),
    WidgetCatalogEntry("车手积分 · 2×2", DriverPortraitWidgetReceiver::class.java),
    WidgetCatalogEntry("车手积分榜 · 4×2", DriversStandingsWidgetReceiver::class.java),
    WidgetCatalogEntry("车手积分榜 · 4×4", DriversLargeWidgetReceiver::class.java),
    WidgetCatalogEntry("车队积分榜 · 4×2", TeamsStandingsWidgetReceiver::class.java),
    WidgetCatalogEntry("车队积分榜 · 4×4", TeamsLargeWidgetReceiver::class.java),
)
internal fun installedWidgets(context: Context): List<Pair<Int, String>> {
    val manager = AppWidgetManager.getInstance(context)
    return (widgetCatalog + WidgetCatalogEntry("车手积分 · 旧版", SecondDriverPortraitWidgetReceiver::class.java)).flatMap { entry ->
        manager.getAppWidgetIds(ComponentName(context, entry.receiver)).map { it to entry.label }
    }
}
