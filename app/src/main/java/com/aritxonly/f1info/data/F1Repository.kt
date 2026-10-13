package com.aritxonly.f1info.data

import android.content.Context
import com.aritxonly.f1info.model.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

class F1Repository(context: Context) {
    private val prefs = context.getSharedPreferences("f1info", Context.MODE_PRIVATE)
    private val cache = File(context.filesDir, "season-cache.json")
    fun favorites(): Favorites {
        val ids = prefs.getString("drivers", null)?.split(",") ?: Favorites().driverIds
        return Favorites(ids, prefs.getString("team", "ferrari") ?: "ferrari").takeIf { it.valid() } ?: Favorites()
    }
    fun saveFavorites(value: Favorites) {
        require(value.valid())
        prefs.edit().putString("drivers", value.driverIds.joinToString(",")).putString("team", value.teamId).apply()
    }
    suspend fun cached(): SeasonData? = withContext(Dispatchers.IO) {
        runCatching { decode(JSONObject(cache.readText())) }.getOrNull()
    }
    suspend fun fetch(): SeasonData = withContext(Dispatchers.IO) {
        // Resolve the service's current season once, then request all tables for that exact season.
        val races = request("current.json?limit=100")
        val season = JSONObject(races).getJSONObject("MRData").getJSONObject("RaceTable").getString("season")
        val paths = listOf("driverStandings", "constructorStandings", "drivers", "constructors")
        val tables = coroutineScope { paths.map { async { request("$season/$it.json?limit=100") } }.awaitAll() }
        val json = JSONObject().put("races", races).put("drivers", tables[0]).put("teams", tables[1])
            .put("driverList", tables[2]).put("teamList", tables[3]).put("fetchedAt", Instant.now().toString())
        val parsed = decode(json)
        val staging = File(cache.parentFile, "season-cache.tmp")
        staging.writeText(json.toString())
        check(staging.renameTo(cache)) { "无法保存离线缓存" }
        parsed
    }
    private fun decode(j: JSONObject) = JolpicaParser.parse(j.getString("races"), j.getString("drivers"),
        j.getString("teams"), j.getString("driverList"), j.getString("teamList"), Instant.parse(j.getString("fetchedAt")))
    private fun request(path: String): String {
        val connection = URL("https://api.jolpi.ca/ergast/f1/$path").openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 15000; connection.readTimeout = 15000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "F1Info/0.1 (Android)")
            check(connection.responseCode == 200) { "数据服务返回 ${connection.responseCode}" }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally { connection.disconnect() }
    }
}
