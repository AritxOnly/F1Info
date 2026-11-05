package com.aritxonly.f1info.model

// Synced from Deadliner 5499df09383e84a120e19bd0c629c6e6ffa7ef0d + local working changes (2026-10-20).
// Integration: package and MIUIX semantic UI bindings; see docs/UPSTREAM.md.

enum class AdvancedMaterialLevel(
    val key: String,
    val blurRadius: Float,
    val blurSaturation: Float,
) {
    CrystalClear("crystal_clear", 16f, 1.05f),
    Light("light", 34f, 1.02f),
    Soft("soft", 52f, 1.00f),
    Hazy("hazy", 72f, 0.98f),
    Deep("deep", 92f, 0.95f);

    companion object {
        fun fromKey(value: String?): AdvancedMaterialLevel = entries.firstOrNull { it.key == value } ?: Soft
    }
}
