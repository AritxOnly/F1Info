package com.aritxonly.f1info.data

/** Keep API identities intact; only resolve aliases at the artwork boundary. */
fun driverArtworkId(id: String): String = when (id) {
    "arvid_lindblad" -> "lindblad"
    "yuki_tsunoda" -> "tsunoda"
    else -> id
}
