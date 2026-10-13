package com.aritxonly.f1info.calendar

import com.aritxonly.f1info.model.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class CalendarEntry(val key: String, val title: String, val location: String, val description: String,
    val start: Instant, val end: Instant)
data class CalendarPlan(val entries: List<CalendarEntry>, val unknownTimes: Int)

/** Stable keys survive timetable changes; unknown start times never become fabricated events. */
fun planCalendar(races: List<Race>, upcomingOnly: Boolean, raceOnly: Boolean, now: Instant): CalendarPlan {
    var unknown = 0
    val entries = races.flatMap { race ->
        val sessions = race.sessions.ifEmpty { listOf(Session("正赛", race.date, race.start)) }
        sessions.filter { !raceOnly || it.name == "正赛" }.mapNotNull { session ->
            val start = session.start
            if (start == null) {
                if (!upcomingOnly || !session.date.isBefore(now.atZone(ZoneOffset.UTC).toLocalDate())) unknown++
                null
            } else if (upcomingOnly && start < now) null
            else CalendarEntry("f1info://calendar/${race.season}/${race.round}/${session.name}",
                "F1 · ${race.name} · ${session.name}", "${race.circuit.name}, ${race.circuit.locality}, ${race.circuit.country}",
                "${race.season} 赛季 · 第 ${race.round} 轮\n来源：Jolpica F1 / F1Info\n结束时间为估算（正赛2小时，其他场次1小时）；请以官方赛程为准。",
                start, start.plusSeconds(if (session.name == "正赛") 7200 else 3600))
        }
    }.distinctBy { it.key }.sortedBy { it.start }
    return CalendarPlan(entries, unknown)
}

private fun icsText(value: String) = value.replace("\\", "\\\\").replace("\r\n", "\n")
    .replace("\r", "\n").replace("\n", "\\n").replace(";", "\\;").replace(",", "\\,")

/** RFC 5545 folds at 75 UTF-8 octets without splitting a Unicode code point. */
internal fun foldCalendarLine(value: String): String {
    val result = StringBuilder()
    var octets = 0
    value.codePoints().forEach { point ->
        val s = String(Character.toChars(point))
        val size = s.toByteArray(Charsets.UTF_8).size
        if (octets + size > 75) { result.append("\r\n "); octets = 1 }
        result.append(s); octets += size
    }
    return result.toString()
}
fun calendarIcs(entries: List<CalendarEntry>, now: Instant): String {
    val format = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC)
    val lines = mutableListOf("BEGIN:VCALENDAR", "VERSION:2.0", "PRODID:-//F1Info//Race calendar//ZH", "CALSCALE:GREGORIAN", "METHOD:PUBLISH")
    entries.forEach {
        lines += listOf("BEGIN:VEVENT", "UID:${java.util.UUID.nameUUIDFromBytes(it.key.toByteArray(Charsets.UTF_8))}@f1info.aritxonly.com",
            "DTSTAMP:${format.format(now)}", "DTSTART:${format.format(it.start)}", "DTEND:${format.format(it.end)}",
            "SUMMARY:${icsText(it.title)}", "LOCATION:${icsText(it.location)}", "DESCRIPTION:${icsText(it.description)}", "END:VEVENT")
    }
    lines += "END:VCALENDAR"
    return lines.joinToString("\r\n", postfix = "\r\n", transform = ::foldCalendarLine)
}
