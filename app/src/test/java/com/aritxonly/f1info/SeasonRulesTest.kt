package com.aritxonly.f1info

import com.aritxonly.f1info.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class SeasonRulesTest {
    private fun race(date: String, time: String? = null) = Race(2026, 1, "Test GP",
        Circuit("test", "Test Circuit", "Test", "Test", null, null, ""), LocalDate.parse(date),
        time?.let { Instant.parse(it) }, emptyList())
    @Test fun nextRaceIncludesTheRaceDuringItsWindow() {
        val r = race("2026-10-11", "2026-10-11T12:00:00Z")
        assertEquals(r, nextRace(listOf(r), Instant.parse("2026-10-11T13:00:00Z")))
        assertNull(nextRace(listOf(r), Instant.parse("2026-10-11T16:00:00Z")))
    }
    @Test fun missingTimeDoesNotInventMidnight() {
        val session = Session("Race", LocalDate.parse("2026-10-11"), null)
        assertTrue(sessionTime(session, ZoneId.of("Asia/Shanghai")).contains("待公布"))
        assertFalse(race("2026-10-11").isFinished(Instant.parse("2026-10-11T08:00:00Z"), ZoneId.of("Asia/Shanghai")))
    }
    @Test fun raceTimesUseDeviceZoneAcrossDateBoundary() {
        val s = Session("Race", LocalDate.parse("2026-10-11"), Instant.parse("2026-10-11T23:00:00Z"))
        assertTrue(sessionTime(s, ZoneId.of("Asia/Shanghai")).contains("10月12日"))
        assertTrue(sessionTime(s, ZoneId.of("Asia/Shanghai")).contains("07:00"))
    }
    @Test fun favoriteSlotsAreStrictAndDistinct() {
        assertTrue(Favorites(listOf("a", "b"), "team").valid())
        assertFalse(Favorites(listOf("a", "a"), "team").valid())
        assertFalse(Favorites(listOf("a"), "team").valid())
        assertFalse(Favorites(listOf("a", "b"), "").valid())
    }
    @Test fun countdownHandlesUnknownAndStartedSessions() {
        assertEquals("时间待公布", countdown(null, Instant.EPOCH))
        assertEquals("比赛进行中", countdown(Instant.EPOCH, Instant.EPOCH.plusSeconds(1)))
        assertEquals("01 天  02 时  03 分", countdown(Instant.EPOCH.plusSeconds(93780), Instant.EPOCH))
    }
}
