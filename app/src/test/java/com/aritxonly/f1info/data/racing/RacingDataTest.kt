package com.aritxonly.f1info.data.racing

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class RacingDataTest {
    private val start = Instant.parse("2026-10-11T12:00:00Z")
    private val session = LiveSession(1, "Race", "Singapore", start, start.plusSeconds(7200))
    private fun snapshot(date: Instant = start) = LiveSnapshot(session, listOf(LiveDriver(1, "Driver", "ONE", "Team")),
        listOf(LivePosition(1, 1, date)), listOf(LiveInterval(1, "—", date)), emptyList(), date)
    @Test fun staleOrEndedSessionsNeverClaimToBeLive() {
        assertEquals("LIVE", snapshot().label(start.plusSeconds(30)))
        assertEquals("数据过期", snapshot().label(start.plusSeconds(61)))
        assertEquals("历史动态", snapshot().label(start.plusSeconds(7200)))
        assertEquals("尚未开始", snapshot().label(start.minusSeconds(1)))
        assertEquals("数据过期", snapshot(start.plusSeconds(100)).label(start.plusSeconds(1)))
    }
    @Test fun overlappingOutOfOrderPollsKeepNewestPositionAndOneEvent() {
        val old = LivePosition(1, 3, start)
        val fresh = LivePosition(1, 1, start.plusSeconds(10))
        assertEquals(listOf(fresh), mergePositions(listOf(fresh), listOf(old, fresh)))
        val event = RaceEvent(start, "Flag", "YELLOW", "Yellow flag", null)
        assertEquals(listOf(event), mergeEvents(listOf(event), listOf(event)))
    }
    @Test fun nullGapsAndLapsAreNotDisplayedAsNullOrZero() {
        assertEquals("—", LiveParser.intervals("""[{"driver_number":1,"date":"2026-10-11T12:00:00Z","gap_to_leader":null}]""").single().gap)
        assertNull(LiveParser.events("""[{"date":"2026-10-11T12:00:00Z","message":"Flag","lap_number":null}]""").single().lap)
    }
    @Test fun unclassifiedQualifyingRowsRetainTheDriverWithoutInventingARank() {
        val rows = LiveParser.finalPositions("""[{"driver_number":1,"position":"RT"},{"driver_number":2,"position":1}]""", start)
        assertEquals(2, rows.size); assertEquals(2, rows.first().number)
        assertNull(rows.last().position); assertEquals("RT", rows.last().classification)
        val data = snapshot().copy(positions = rows)
        assertEquals(data, LiveSnapshotCache.decode(LiveSnapshotCache.encode(data)))
    }
    @Test fun qualifyingGapUsesLastCompletedSegmentAndHandlesLappedCars() {
        assertEquals("1.23", LiveParser.gap(org.json.JSONArray("[0.25,1.23,null]")))
        assertEquals("—", LiveParser.gap(org.json.JSONObject.NULL))
        assertEquals("+1 LAP", LiveParser.gap("+1 LAP"))
    }
    @Test fun snapshotCacheRetainsTimestampAndHistory() {
        val data = snapshot().copy(events = listOf(RaceEvent(start, "Flag", "RED", "Stopped", null)))
        assertEquals(data, LiveSnapshotCache.decode(LiveSnapshotCache.encode(data)))
    }
    private fun result(category: String, body: String) = """{"MRData":{"RaceTable":{"Races":[{"$category":[$body]}]}}}"""
    private val identity = """"Driver":{"driverId":"one","givenName":"First","familyName":"Last"},"Constructor":{"name":"Team"},"position":"1""""
    @Test fun qualifyingPreservesSessionTimesWithoutInventingMissingQ3() {
        val row = ResultsParser.parse(result("QualifyingResults", "{$identity,\"Q1\":\"1:20.000\",\"Q2\":\"1:19.000\"}"), "qualifying").single()
        assertEquals("Q1 1:20.000 · Q2 1:19.000", row.time)
        assertEquals("—", row.points)
    }
    @Test fun retiredDriverRetainsStatusAndLaps() {
        val row = ResultsParser.parse(result("Results", "{$identity,\"laps\":\"10\",\"status\":\"Engine\",\"points\":\"0\"}"), "results").single()
        assertEquals("Engine", row.status); assertEquals("10", row.laps); assertEquals("", row.time)
    }
    @Test fun unpublishedRaceResultsAreEmpty() {
        assertTrue(ResultsParser.parse("""{"MRData":{"RaceTable":{"Races":[]}}}""", "results").isEmpty())
    }
}
