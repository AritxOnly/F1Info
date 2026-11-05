package com.aritxonly.f1info.model

// Synced from Deadliner 5499df09383e84a120e19bd0c629c6e6ffa7ef0d + local working changes (2026-10-20).
// Integration: package and MIUIX semantic UI bindings; see docs/UPSTREAM.md.

/** Stable local preference keys; shader/material parameters are independent of these experiments. */
enum class GlassShadowPreset(val key: String) {
    BalancedDiffuse("balanced_diffuse"),
    Clean("clean"),
    Original("original"),
    Diffuse("diffuse"),
    Compact("compact"),
    Crisp("crisp"),
    Airy("airy"),
    CloseFloat("close_float"),
    Elevated("elevated"),
    SoftContact("soft_contact"),
    Minimal("minimal");

    companion object {
        val Default = BalancedDiffuse
        fun fromKey(key: String?): GlassShadowPreset = entries.firstOrNull { it.key == key } ?: Default
    }
}
