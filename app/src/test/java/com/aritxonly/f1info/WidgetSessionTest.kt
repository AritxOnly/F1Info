package com.aritxonly.f1info

import com.aritxonly.f1info.model.*
import com.aritxonly.f1info.widgets.nextWidgetSession
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class WidgetSessionTest {
    private val zone = ZoneOffset.UTC
    private val circuit = Circuit("test", "Circuit", "City", "Country", null, null, "")
    private fun session(name: String, instant: String) = Instant.parse(instant).let { Session(name, it.atZone(zone).toLocalDate(), it) }
    private val friday = session("一练", "2026-10-23T15:00:00Z")
    private val qualifying = session("排位赛", "2026-10-24T20:00:00Z")
    private val raceSession = session("正赛", "2026-10-25T20:00:00Z")
    private fun race(sessions: List<Session>) = Race(2026, 18, "United States", circuit,
        raceSession.date, raceSession.start, sessions)

    @Test fun fridaySelectsPracticeInsteadOfSundayRace() {
        val next = nextWidgetSession(listOf(race(listOf(raceSession, qualifying, friday))), Instant.parse("2026-10-23T10:00:00Z"), zone)
        assertEquals(friday, next?.session)
        assertFalse(next!!.ongoing)
    }
    @Test fun ongoingPracticeRemainsVisibleThenMovesToQualifying() {
        val races = listOf(race(listOf(friday, qualifying, raceSession)))
        assertTrue(nextWidgetSession(races, Instant.parse("2026-10-23T15:30:00Z"), zone)!!.ongoing)
        assertEquals(qualifying, nextWidgetSession(races, Instant.parse("2026-10-23T16:00:00Z"), zone)?.session)
    }
    @Test fun sprintIsASelectableSession() {
        val sprint = session("冲刺赛", "2026-10-24T16:00:00Z")
        assertEquals(sprint, nextWidgetSession(listOf(race(listOf(sprint, qualifying, raceSession))), Instant.parse("2026-10-24T12:00:00Z"), zone)?.session)
    }
    @Test fun missingTimePreservesPublishedDateWithoutInventingStart() {
        val unknown = Session("二练", friday.date, null)
        val next = nextWidgetSession(listOf(race(listOf(unknown, raceSession))), Instant.parse("2026-10-23T10:00:00Z"), zone)
        assertEquals(unknown, next?.session)
        assertNull(next?.session?.start)
        assertFalse(next!!.ongoing)
        assertEquals(raceSession, nextWidgetSession(listOf(race(listOf(unknown, raceSession))), Instant.parse("2026-10-24T10:00:00Z"), zone)?.session)
    }
    @Test fun finishedSeasonReturnsNothingAndEmptySessionsFallBackToPublishedRace() {
        assertNull(nextWidgetSession(listOf(race(listOf(raceSession))), Instant.parse("2026-10-26T10:00:00Z"), zone))
        assertEquals("正赛", nextWidgetSession(listOf(race(emptyList())), Instant.parse("2026-10-25T10:00:00Z"), zone)?.session?.name)
    }
}
