package com.aritxonly.f1info.data.racing

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.Instant

object LiveSnapshotCache {
    fun encode(data: LiveSnapshot): String = JSONObject().apply {
        put("session", JSONArray().put(JSONObject().put("session_key", data.session.key).put("session_name", data.session.name)
            .put("location", data.session.location).put("date_start", data.session.start).put("date_end", data.session.end)))
        put("drivers", JSONArray(data.drivers.map { JSONObject().put("driver_number", it.number).put("full_name", it.name).put("name_acronym", it.code).put("team_name", it.team) }))
        put("positions", JSONArray(data.positions.map { JSONObject().put("driver_number", it.number).put("position", it.position ?: JSONObject.NULL).put("classification", it.classification).put("date", it.date) }))
        put("intervals", JSONArray(data.intervals.map { JSONObject().put("driver_number", it.number).put("gap_to_leader", it.gap).put("date", it.date) }))
        put("events", JSONArray(data.events.map { JSONObject().put("date", it.date).put("category", it.category).put("flag", it.flag).put("message", it.message).put("lap_number", it.lap ?: JSONObject.NULL) }))
        put("fetched", data.fetchedAt)
    }.toString()
    fun decode(raw: String): LiveSnapshot = JSONObject(raw).let { j -> LiveSnapshot(LiveParser.sessions(j.getJSONArray("session").toString()).first(),
        LiveParser.drivers(j.getJSONArray("drivers").toString()), LiveParser.positions(j.getJSONArray("positions").toString()),
        LiveParser.intervals(j.getJSONArray("intervals").toString()), LiveParser.events(j.getJSONArray("events").toString()), Instant.parse(j.getString("fetched"))) }
    fun read(context: Context, key: String): LiveSnapshot? = runCatching { decode(File(context.filesDir, "live-$key.json").readText()) }.getOrNull()
    fun write(context: Context, key: String, data: LiveSnapshot) {
        runCatching { val file = File(context.filesDir, "live-$key.json"); val temp = File(context.filesDir, "live-$key.tmp"); temp.writeText(encode(data)); check(temp.renameTo(file)) }
    }
}
