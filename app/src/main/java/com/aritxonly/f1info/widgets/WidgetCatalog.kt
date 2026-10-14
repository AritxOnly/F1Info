package com.aritxonly.f1info.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

internal data class WidgetCatalogEntry(val label: String, val receiver: Class<out RacingWidgetReceiver>)
internal val widgetCatalog = listOf(
    // 澎湃卡片通道
    WidgetCatalogEntry("赛程 · 2×2 (澎湃卡片)", F1WidgetReceiver::class.java),
    WidgetCatalogEntry("赛程 · 4×2 (澎湃卡片)", ScheduleWideWidgetReceiver::class.java),
    WidgetCatalogEntry("赛程 · 4×4 (澎湃卡片)", ScheduleLargeWidgetReceiver::class.java),
    WidgetCatalogEntry("车手积分 · 2×2 (澎湃卡片)", DriverPortraitWidgetReceiver::class.java),
    WidgetCatalogEntry("车手积分榜 · 4×2 (澎湃卡片)", DriversStandingsWidgetReceiver::class.java),
    WidgetCatalogEntry("车手积分榜 · 4×4 (澎湃卡片)", DriversLargeWidgetReceiver::class.java),
    WidgetCatalogEntry("车队积分榜 · 4×2 (澎湃卡片)", TeamsStandingsWidgetReceiver::class.java),
    WidgetCatalogEntry("车队积分榜 · 4×4 (澎湃卡片)", TeamsLargeWidgetReceiver::class.java),
    // 原生 AOSP 通道
    WidgetCatalogEntry("赛程 · 2×2 (原生)", F1AospWidgetReceiver::class.java),
    WidgetCatalogEntry("赛程 · 4×2 (原生)", ScheduleWideAospWidgetReceiver::class.java),
    WidgetCatalogEntry("赛程 · 4×4 (原生)", ScheduleLargeAospWidgetReceiver::class.java),
    WidgetCatalogEntry("车手积分 · 2×2 (原生)", DriverPortraitAospWidgetReceiver::class.java),
    WidgetCatalogEntry("车手积分榜 · 4×2 (原生)", DriversStandingsAospWidgetReceiver::class.java),
    WidgetCatalogEntry("车手积分榜 · 4×4 (原生)", DriversLargeAospWidgetReceiver::class.java),
    WidgetCatalogEntry("车队积分榜 · 4×2 (原生)", TeamsStandingsAospWidgetReceiver::class.java),
    WidgetCatalogEntry("车队积分榜 · 4×4 (原生)", TeamsLargeAospWidgetReceiver::class.java),
)
internal fun installedWidgets(context: Context): List<Pair<Int, String>> {
    val manager = AppWidgetManager.getInstance(context)
    return (widgetCatalog + WidgetCatalogEntry("车手积分 · 旧版", SecondDriverPortraitWidgetReceiver::class.java)).flatMap { entry ->
        manager.getAppWidgetIds(ComponentName(context, entry.receiver)).map { it to entry.label }
    }
}
