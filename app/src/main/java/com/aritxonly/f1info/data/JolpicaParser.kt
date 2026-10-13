package com.aritxonly.f1info.data

import com.aritxonly.f1info.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate

internal fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
private fun JSONObject.optionalInt(key: String): Int? = optString(key).toIntOrNull()
private fun JSONObject.optionalString(key: String): String? = optString(key).takeIf { it.isNotBlank() && it != "null" }
private fun start(date: String, time: String?): Instant? = time?.let { Instant.parse("${date}T$it") }

object JolpicaParser {
    fun parse(racesJson: String, driversJson: String, teamsJson: String,
        driverListJson: String, teamListJson: String, fetchedAt: Instant): SeasonData {
        fun root(raw: String) = JSONObject(raw).getJSONObject("MRData")
        val raceTable = root(racesJson).getJSONObject("RaceTable")
        val season = raceTable.getString("season").toInt()
        val races = raceTable.getJSONArray("Races").objects().map { obj ->
            val c = obj.getJSONObject("Circuit"); val loc = c.getJSONObject("Location")
            val date = obj.getString("date")
            val sessions = listOf("FirstPractice" to "练习赛 1", "SecondPractice" to "练习赛 2",
                "ThirdPractice" to "练习赛 3", "SprintQualifying" to "冲刺排位赛",
                "Sprint" to "冲刺赛", "Qualifying" to "排位赛").mapNotNull { (key, name) ->
                obj.optJSONObject(key)?.let { s -> Session(name, LocalDate.parse(s.getString("date")),
                    start(s.getString("date"), s.optionalString("time"))) }
            } + Session("正赛", LocalDate.parse(date), start(date, obj.optionalString("time")))
            Race(season, obj.getString("round").toInt(), obj.getString("raceName"),
                Circuit(c.getString("circuitId"), c.getString("circuitName"), loc.getString("locality"),
                    loc.getString("country"), loc.optString("lat").toDoubleOrNull(),
                    loc.optString("long").toDoubleOrNull(), c.optString("url")),
                LocalDate.parse(date), start(date, obj.optionalString("time")), sessions.sortedBy { it.date })
        }
        val dsTable = root(driversJson).getJSONObject("StandingsTable")
        val tsTable = root(teamsJson).getJSONObject("StandingsTable")
        val dl = dsTable.getJSONArray("StandingsLists").optJSONObject(0)
        val tl = tsTable.getJSONArray("StandingsLists").optJSONObject(0)
        require(dl == null || dl.getString("season").toInt() == season) { "车手榜赛季不匹配" }
        require(tl == null || tl.getString("season").toInt() == season) { "车队榜赛季不匹配" }
        val driverStandings = dl?.getJSONArray("DriverStandings")?.objects().orEmpty()
        val teamStandings = tl?.getJSONArray("ConstructorStandings")?.objects().orEmpty()
        val drivers = root(driverListJson).getJSONObject("DriverTable").getJSONArray("Drivers").objects().map { d ->
            val standing = driverStandings.firstOrNull { it.getJSONObject("Driver").getString("driverId") == d.getString("driverId") }
            val constructor = standing?.optJSONArray("Constructors")?.optJSONObject(0)
            Driver(d.getString("driverId"), "${d.getString("givenName")} ${d.getString("familyName")}",
                d.optString("code", d.getString("familyName").take(3).uppercase()), d.optString("permanentNumber", "—"),
                constructor?.optString("constructorId").orEmpty(), constructor?.optString("name") ?: "车队待公布",
                d.optString("nationality"), d.optString("dateOfBirth"), standing?.optionalInt("position"),
                standing?.optionalString("points"), standing?.optionalInt("wins"), d.optString("url"))
        }.sortedBy { it.position ?: Int.MAX_VALUE }
        val teams = root(teamListJson).getJSONObject("ConstructorTable").getJSONArray("Constructors").objects().map { t ->
            val s = teamStandings.firstOrNull { it.getJSONObject("Constructor").getString("constructorId") == t.getString("constructorId") }
            Team(t.getString("constructorId"), t.getString("name"), t.optString("nationality"),
                s?.optionalInt("position"), s?.optionalString("points"), s?.optionalInt("wins"), t.optString("url"))
        }.sortedBy { it.position ?: Int.MAX_VALUE }
        return SeasonData(season, races, drivers, teams, dl?.optionalInt("round"), fetchedAt)
    }
}
