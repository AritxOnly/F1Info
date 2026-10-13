package com.aritxonly.f1info.data.racing

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

object ResultsParser {
    fun parse(raw: String, category: String): List<RaceResult> {
        val races = JSONObject(raw).getJSONObject("MRData").getJSONObject("RaceTable").getJSONArray("Races")
        if (races.length() == 0) return emptyList()
        val rows = races.getJSONObject(0).optJSONArray(when (category) { "qualifying" -> "QualifyingResults"; "sprint" -> "SprintResults"; else -> "Results" }) ?: return emptyList()
        return (0 until rows.length()).map { i ->
            val row = rows.getJSONObject(i); val d = row.getJSONObject("Driver")
            val time = if (category == "qualifying") listOf("Q1", "Q2", "Q3").mapNotNull { k -> row.optString(k).takeIf { it.isNotBlank() && !row.isNull(k) }?.let { "$k $it" } }.joinToString(" · ")
                else row.optJSONObject("Time")?.optString("time").orEmpty()
            RaceResult(row.optString("position", "—"), d.getString("driverId"), "${d.getString("givenName")} ${d.getString("familyName")}",
                row.optString("number", d.optString("permanentNumber", "—")), row.getJSONObject("Constructor").getString("name"),
                row.optString("grid", "—"), row.optString("laps", "—"), row.optString("points", "—"), row.optString("status"), time)
        }
    }
}
data class ResultsLoad(val rows: List<RaceResult>, val cached: Boolean, val error: String? = null)
object ResultsRepository {
    suspend fun load(context: Context, season: Int, round: Int, category: String): ResultsLoad = withContext(Dispatchers.IO) {
        require(category in listOf("results", "sprint", "qualifying"))
        val file = File(context.filesDir, "results-$season-$round-$category.json")
        try {
            val raw = RacingHttp.get("https://api.jolpi.ca/ergast/f1/$season/$round/$category.json?limit=100")
            val rows = ResultsParser.parse(raw, category)
            file.writeText(raw)
            ResultsLoad(rows, false)
        } catch (e: Exception) {
            val rows = runCatching { ResultsParser.parse(file.readText(), category) }.getOrDefault(emptyList())
            ResultsLoad(rows, rows.isNotEmpty(), e.message ?: "无法加载比赛结果")
        }
    }
}
