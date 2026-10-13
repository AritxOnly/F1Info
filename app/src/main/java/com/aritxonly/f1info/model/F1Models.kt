package com.aritxonly.f1info.model

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Session(val name: String, val date: LocalDate, val start: Instant?)
data class Circuit(val id: String, val name: String, val locality: String, val country: String,
    val latitude: Double?, val longitude: Double?, val url: String)
data class Race(val season: Int, val round: Int, val name: String, val circuit: Circuit,
    val date: LocalDate, val start: Instant?, val sessions: List<Session>)
data class Driver(val id: String, val name: String, val code: String, val number: String,
    val teamId: String, val team: String, val nationality: String, val birthDate: String,
    val position: Int?, val points: String?, val wins: Int?, val url: String)
data class Team(val id: String, val name: String, val nationality: String,
    val position: Int?, val points: String?, val wins: Int?, val url: String)
data class SeasonData(val season: Int, val races: List<Race>, val drivers: List<Driver>,
    val teams: List<Team>, val standingsRound: Int?, val fetchedAt: Instant)
data class Favorites(val driverIds: List<String> = listOf("max_verstappen", "leclerc"), val teamId: String = "ferrari") {
    fun valid() = driverIds.size == 2 && driverIds.distinct().size == 2 && teamId.isNotBlank()
}

// A missing start time must not be replaced by a fabricated race time.
fun Race.isFinished(now: Instant, zone: ZoneId = ZoneId.systemDefault()): Boolean =
    start?.let { now.isAfter(it.plusSeconds(3 * 3600)) }
        ?: date.isBefore(now.atZone(zone).toLocalDate())
fun nextRace(races: List<Race>, now: Instant): Race? = races.sortedBy { it.date }.firstOrNull { !it.isFinished(now) }
fun sessionTime(session: Session, zone: ZoneId = ZoneId.systemDefault()): String =
    session.start?.atZone(zone)?.format(DateTimeFormatter.ofPattern("MM月dd日 E HH:mm", Locale.CHINA))
        ?: "${session.date.format(DateTimeFormatter.ofPattern("MM月dd日"))} · 时间待公布"
fun raceDate(race: Race, zone: ZoneId = ZoneId.systemDefault()): String =
    (race.start?.atZone(zone)?.toLocalDate() ?: race.date).format(DateTimeFormatter.ofPattern("MM月dd日", Locale.CHINA))
fun countdown(start: Instant?, now: Instant): String {
    if (start == null) return "时间待公布"
    val seconds = Duration.between(now, start).seconds
    if (seconds <= 0) return "比赛进行中"
    return "%02d 天  %02d 时  %02d 分".format(seconds / 86400, seconds / 3600 % 24, seconds / 60 % 60)
}
