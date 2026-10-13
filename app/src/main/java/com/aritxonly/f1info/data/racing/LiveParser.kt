package com.aritxonly.f1info.data.racing

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

object LiveParser {
    fun gap(value: Any?): String = when (value) {
        null, JSONObject.NULL -> "—"
        is JSONArray -> (0 until value.length()).map { value.opt(it) }.lastOrNull { it != null && it != JSONObject.NULL }?.toString() ?: "—"
        else -> value.toString()
    }
    private fun rows(raw: String) = JSONArray(raw).let { a -> (0 until a.length()).map { a.getJSONObject(it) } }
    fun sessions(raw: String) = rows(raw).filter { !it.optBoolean("is_cancelled") }.map {
        LiveSession(it.getInt("session_key"), it.getString("session_name"), it.optString("location", it.optString("circuit_short_name")),
            Instant.parse(it.getString("date_start")), Instant.parse(it.getString("date_end")))
    }
    fun drivers(raw: String) = rows(raw).map { LiveDriver(it.getInt("driver_number"), it.optString("full_name"), it.optString("name_acronym"), it.optString("team_name")) }
    private fun rank(value: Any?) = value?.toString()?.toIntOrNull()?.takeIf { it > 0 }
    fun finalPositions(raw: String, date: Instant) = rows(raw).map {
        LivePosition(it.getInt("driver_number"), rank(it.opt("position")), date, if (it.isNull("position") || rank(it.opt("position")) != null) "" else it.optString("position"))
    }.sortedBy { it.position ?: Int.MAX_VALUE }
    fun positions(raw: String) = rows(raw).map { LivePosition(it.getInt("driver_number"), rank(it.opt("position")), Instant.parse(it.getString("date")), if (it.isNull("classification")) "" else it.optString("classification")) }
    fun intervals(raw: String) = rows(raw).map { LiveInterval(it.getInt("driver_number"), gap(it.opt("gap_to_leader")), Instant.parse(it.getString("date"))) }
    fun events(raw: String) = rows(raw).map { RaceEvent(Instant.parse(it.getString("date")), it.optString("category"), it.optString("flag").takeUnless { v -> v == "null" }.orEmpty(), it.optString("message"), it.optInt("lap_number").takeIf { n -> n > 0 }) }
}
