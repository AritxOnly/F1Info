package com.aritxonly.f1info.ui.navigation

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
