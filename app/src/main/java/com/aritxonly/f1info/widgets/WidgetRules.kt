package com.aritxonly.f1info.widgets

import java.time.Duration
import java.time.Instant

// Widget scheduling is coarse. Display approximate hours, never a stale second-by-second timer.
fun widgetCountdown(start: Instant?, now: Instant): String {
    if (start == null) return "正赛时间待公布"
    val hours = Duration.between(now, start).toHours()
    if (!start.isAfter(now)) return "正赛已开始"
    if (hours == 0L) return "正赛 · 不足 1 小时"
    return if (hours >= 24) "正赛 · 约 ${hours / 24} 天 ${hours % 24} 小时" else "正赛 · 约 $hours 小时"
}

internal enum class WidgetLayout { Compact, Wide, Expanded }
internal fun widgetLayout(widthDp: Float, heightDp: Float): WidgetLayout =
    if (widthDp < 230f) WidgetLayout.Compact else if (heightDp < 230f) WidgetLayout.Wide else WidgetLayout.Expanded

internal fun nearbyRaces(races: List<com.aritxonly.f1info.model.Race>, next: com.aritxonly.f1info.model.Race?) =
    if (next == null) emptyList() else races.filter { it.round > next.round }.sortedBy { it.round }.take(4)

internal data class WidgetSession(val race: com.aritxonly.f1info.model.Race,
    val session: com.aritxonly.f1info.model.Session, val ongoing: Boolean)

/** Include every scheduled session. Unknown times retain the published calendar date. */
internal fun nextWidgetSession(races: List<com.aritxonly.f1info.model.Race>, now: Instant,
    zone: java.time.ZoneId = java.time.ZoneId.systemDefault()): WidgetSession? {
    val today = now.atZone(zone).toLocalDate()
    return races.flatMap { race ->
        val sessions = race.sessions.ifEmpty { listOf(com.aritxonly.f1info.model.Session("正赛", race.date, race.start)) }
        sessions.mapNotNull { session ->
            val start = session.start
            // Same coarse ongoing window as the existing race rule; not a live result assertion.
            val duration = if (session.name == "正赛") 3 * 3600L else 3600L
            if (start != null && !now.isBefore(start.plusSeconds(duration))) null
            else if (start == null && session.date.isBefore(today)) null
            else WidgetSession(race, session, start != null && !now.isBefore(start))
        }
    }.minByOrNull { it.session.start ?: it.session.date.atStartOfDay(zone).toInstant() }
}
