package com.aritxonly.f1info.ui.navigation

import com.aritxonly.f1info.model.*
import java.time.Instant
import androidx.compose.runtime.saveable.Saver
import top.yukonga.miuix.kmp.nav.core.NavBackStack
import top.yukonga.miuix.kmp.nav.core.NavKey
import top.yukonga.miuix.kmp.nav.core.navBackStackOf

/** Routes contain identity only; restored details resolve against the repository snapshot. */
data object MainRoute : NavKey
data class DetailRoute(val kind: String, val id: String = "") : NavKey

// String identities are Bundle-saveable without a serialization compiler plugin.
val F1RouteStackSaver = Saver<NavBackStack, ArrayList<String>>(
    save = { stack -> ArrayList(stack.map { route ->
        when (route) {
            is DetailRoute -> "${route.kind}|${route.id}"
            else -> "main"
        }
    }) },
    restore = { saved -> navBackStackOf(*saved.map { value ->
        if (value == "main") MainRoute else {
            val parts = value.split('|', limit = 2)
            DetailRoute(parts[0], parts.getOrElse(1) { "" })
        }
    }.toTypedArray()) },
)

// The initial detail belongs to the filtered list and does not add a synthetic back-stack entry.
internal fun defaultTabletDetail(tab: String, data: SeasonData?, filter: Int, category: Int, now: Instant): DetailRoute? {
    data ?: return null
    return when (tab) {
        "schedule" -> {
            val race = when (filter) {
                1 -> nextRace(data.races, now)
                2 -> data.races.filter { it.isFinished(now) }.maxByOrNull { it.date }
                else -> nextRace(data.races, now) ?: data.races.maxByOrNull { it.date }
            }
            race?.let { DetailRoute("race", it.round.toString()) }
        }
        "standings" -> if (category == 0) {
            data.drivers.firstOrNull { it.position != null }?.let { DetailRoute("driver", it.id) }
        } else {
            data.teams.firstOrNull { it.position != null }?.let { DetailRoute("team", it.id) }
        }
        else -> null
    }
}
