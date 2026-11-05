package com.aritxonly.f1info.model

// Synced from Deadliner 5499df09383e84a120e19bd0c629c6e6ffa7ef0d + local working changes (2026-10-20).
// Integration: package and MIUIX semantic UI bindings; see docs/UPSTREAM.md.

enum class MiuixModalKind {
    Dialog,
    BottomSheet,
}

data class MiuixModalMaterialTuning(
    val backgroundAlpha: Float,
    val blurRadiusDp: Float,
) {
    companion object {
        const val MinBackgroundAlpha = 0.20f
        const val MaxBackgroundAlpha = 0.95f
        const val MinBlurRadiusDp = 0f
        const val MaxBlurRadiusDp = 150f

        const val DialogDefaultBackgroundAlpha = 0.70237434f
        const val DialogDefaultBlurRadiusDp = 80f
        const val BottomSheetDefaultBackgroundAlpha = 0.69986033f
        const val BottomSheetDefaultBlurRadiusDp = 80f
    }
}

fun AdvancedMaterialFineTuning.miuixModalMaterial(
    kind: MiuixModalKind,
): MiuixModalMaterialTuning {
    val tuning = normalized()
    return when (kind) {
        MiuixModalKind.Dialog -> MiuixModalMaterialTuning(
            backgroundAlpha = tuning.dialogBackgroundAlpha,
            blurRadiusDp = tuning.dialogBlurRadiusDp,
        )

        MiuixModalKind.BottomSheet -> MiuixModalMaterialTuning(
            backgroundAlpha = tuning.bottomSheetBackgroundAlpha,
            blurRadiusDp = tuning.bottomSheetBlurRadiusDp,
        )
    }
}
