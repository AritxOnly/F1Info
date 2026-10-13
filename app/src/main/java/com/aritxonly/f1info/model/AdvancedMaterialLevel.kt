package com.aritxonly.f1info.model

// Migrated from Deadliner 210e4a2d4c6bf7df291b6c6852d39366bc1fee17.
// Integration changes: package and MIUIX semantic UI bindings only.

enum class AdvancedMaterialLevel(
    val key: String,
    val blurRadius: Float,
    val blurSaturation: Float,
) {
    CrystalClear("crystal_clear", 28f, 1.05f),
    Light("light", 60f, 1.02f),
    Soft("soft", 128f, 1.00f),
    Hazy("hazy", 196f, 0.98f),
    Deep("deep", 256f, 0.95f);

    companion object {
        fun fromKey(value: String?): AdvancedMaterialLevel = entries.firstOrNull { it.key == value } ?: Soft
    }
}
