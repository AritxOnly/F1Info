package com.aritxonly.f1info.data.racing

import java.time.Instant

data class RaceResult(val position: String, val driverId: String, val name: String, val number: String,
    val team: String, val grid: String, val laps: String, val points: String, val status: String, val time: String)
data class LiveSession(val key: Int, val name: String, val location: String, val start: Instant, val end: Instant) {
    fun active(now: Instant) = !now.isBefore(start) && now.isBefore(end)
}
data class LiveDriver(val number: Int, val name: String, val code: String, val team: String)
data class LivePosition(val number: Int, val position: Int?, val date: Instant, val classification: String = "")
data class LiveInterval(val number: Int, val gap: String, val date: Instant)
data class RaceEvent(val date: Instant, val category: String, val flag: String, val message: String, val lap: Int?) {
    val key get() = "$date|$category|$message"
}
data class LiveSnapshot(val session: LiveSession, val drivers: List<LiveDriver>, val positions: List<LivePosition>,
    val intervals: List<LiveInterval>, val events: List<RaceEvent>, val fetchedAt: Instant) {
    fun label(now: Instant): String = when {
        now.isBefore(session.start) -> "尚未开始"
        !session.active(now) -> "历史动态"
        latestSample()?.let { now.epochSecond - it.epochSecond in -5..60 } == true -> "LIVE"
        else -> "数据过期"
    }
    fun latestSample(): Instant? = positions.maxOfOrNull { it.date }
    fun leader(): String? = positions.filter { it.position != null }.minByOrNull { it.position!! }?.let { p -> drivers.firstOrNull { it.number == p.number }?.code }
}

fun mergePositions(previous: List<LivePosition>, fresh: List<LivePosition>) =
    (previous + fresh).groupBy { it.number }.values.map { rows -> rows.maxBy { it.date } }.sortedBy { it.position ?: Int.MAX_VALUE }
fun mergeIntervals(previous: List<LiveInterval>, fresh: List<LiveInterval>) =
    (previous + fresh).groupBy { it.number }.values.map { rows -> rows.maxBy { it.date } }
fun mergeEvents(previous: List<RaceEvent>, fresh: List<RaceEvent>) =
    (previous + fresh).distinctBy { it.key }.sortedByDescending { it.date }.take(100)
