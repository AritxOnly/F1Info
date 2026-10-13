package com.aritxonly.f1info

import com.aritxonly.f1info.data.JolpicaParser
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class JolpicaParserTest {
    private val races = """{"MRData":{"RaceTable":{"season":"2026","Races":[{"round":"1","raceName":"Test Grand Prix","date":"2026-03-08","Circuit":{"circuitId":"test","circuitName":"Test Circuit","Location":{"locality":"Town","country":"Country"}},"Qualifying":{"date":"2026-03-07","time":"12:00:00Z"}}]}}}"""
    private val emptyStandings = """{"MRData":{"StandingsTable":{"StandingsLists":[]}}}"""
    private val drivers = """{"MRData":{"DriverTable":{"Drivers":[{"driverId":"test","givenName":"Test","familyName":"Driver"}]}}}"""
    private val teams = """{"MRData":{"ConstructorTable":{"Constructors":[{"constructorId":"team","name":"Team"}]}}}"""
    @Test fun emptyStandingsStillHasRosterAndWeekend() {
        val parsed = JolpicaParser.parse(races, emptyStandings, emptyStandings, drivers, teams, Instant.EPOCH)
        assertEquals(2026, parsed.season)
        assertEquals(2, parsed.races.single().sessions.size)
        assertNull(parsed.races.single().start)
        assertNull(parsed.drivers.single().points)
        assertNull(parsed.teams.single().position)
        assertNull(parsed.standingsRound)
    }
    @Test fun standingsMergeByIdentityRatherThanRosterOrder() {
        val standings = """{"MRData":{"StandingsTable":{"StandingsLists":[{"season":"2026","round":"2","DriverStandings":[{"position":"1","points":"25.5","wins":"1","Driver":{"driverId":"test"},"Constructors":[{"constructorId":"team","name":"Team"}]}]}]}}}"""
        val parsed = JolpicaParser.parse(races, standings, emptyStandings, drivers, teams, Instant.EPOCH)
        assertEquals("25.5", parsed.drivers.single().points)
        assertEquals("team", parsed.drivers.single().teamId)
        assertEquals(2, parsed.standingsRound)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsMixedSeasons() {
        val mismatch = """{"MRData":{"StandingsTable":{"StandingsLists":[{"season":"2025","round":"24","DriverStandings":[]}]}}}"""
        JolpicaParser.parse(races, mismatch, emptyStandings, drivers, teams, Instant.EPOCH)
    }
}
