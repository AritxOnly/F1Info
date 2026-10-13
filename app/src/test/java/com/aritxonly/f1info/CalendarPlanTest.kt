package com.aritxonly.f1info

import com.aritxonly.f1info.calendar.*
import com.aritxonly.f1info.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class CalendarPlanTest {
    private val now = Instant.parse("2026-10-20T12:00:00Z")
    private val circuit = Circuit("test", "赛道, A; B", "城市", "国家", null, null, "")
    private fun race(start: Instant = now.plusSeconds(86400)) = Race(2026, 18, "美国大奖赛", circuit,
        LocalDate.of(2026, 10, 21), start, listOf(Session("练习赛1", LocalDate.of(2026, 10, 20), now.minusSeconds(3600)),
            Session("排位赛", LocalDate.of(2026, 10, 21), null), Session("正赛", LocalDate.of(2026, 10, 21), start)))
    @Test fun upcomingFiltersPastAndSkipsUnknown() {
        val p = planCalendar(listOf(race()), true, false, now)
        assertEquals(1, p.entries.size); assertEquals(1, p.unknownTimes)
        assertTrue(p.entries[0].title.endsWith("正赛"))
    }
    @Test fun entireSeasonAndRaceOnlyHaveDistinctScopes() {
        assertEquals(2, planCalendar(listOf(race()), false, false, now).entries.size)
        assertEquals(1, planCalendar(listOf(race()), false, true, now).entries.size)
    }
    @Test fun keysSurviveTimeChangesAndRepeatedDataIsDeduplicated() {
        val a = planCalendar(listOf(race(), race()), false, true, now).entries
        val b = planCalendar(listOf(race(now.plusSeconds(90000))), false, true, now).entries
        assertEquals(1, a.size); assertEquals(a[0].key, b[0].key); assertNotEquals(a[0].start, b[0].start)
    }
    @Test fun missingSessionListFallsBackToPublishedRaceTime() {
        val r = race().copy(sessions = emptyList())
        assertEquals(1, planCalendar(listOf(r), true, false, now).entries.size)
        assertEquals(1, planCalendar(listOf(r.copy(start = null)), true, false, now).unknownTimes)
    }
    @Test fun icsPreservesUtcEscapesAndStableUid() {
        val entries = planCalendar(listOf(race()), false, true, now).entries
        val text = calendarIcs(entries, now)
        assertTrue(text.contains("DTSTART:20261021T120000Z\r\n"))
        assertTrue(text.contains("DTEND:20261021T140000Z\r\n"))
        assertTrue(text.contains("赛道\\, A\\; B"))
        assertTrue(text.endsWith("END:VCALENDAR\r\n"))
        assertEquals(text.lineSequence().first { it.startsWith("UID:") }, calendarIcs(entries, now.plusSeconds(60)).lineSequence().first { it.startsWith("UID:") })
    }
    @Test fun longUnicodeLinesAreFoldedWithoutCorruptingText() {
        val original = "SUMMARY:" + "🏎️比赛周末".repeat(40)
        val folded = foldCalendarLine(original)
        assertTrue(folded.split("\r\n").all { it.toByteArray(Charsets.UTF_8).size <= 75 })
        assertEquals(original, folded.replace("\r\n ", ""))
    }
}
