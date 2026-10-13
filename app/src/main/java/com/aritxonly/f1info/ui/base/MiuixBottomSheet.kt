package com.aritxonly.f1info.ui.base

// Migrated from Deadliner 210e4a2d4c6bf7df291b6c6852d39366bc1fee17.
// Integration changes: package and MIUIX semantic UI bindings only.

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.aritxonly.f1info.ui.material.glass.DeadlinerGlassRecipes
import com.aritxonly.f1info.model.MiuixModalKind
import com.aritxonly.f1info.ui.theme.LocalAdvancedMaterialSpec
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet

/**
 * Shared Miuix sheet host for page-level actions.
 *
 * OverlayBottomSheet shares the page window so its glass material can sample the page backdrop.
 */
@Composable
fun MiuixBottomSheet(
    show: Boolean,
    title: String? = null,
    onDismissRequest: () -> Unit,
    onDismissFinished: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    startAction: @Composable (() -> Unit)? = null,
    endAction: @Composable (() -> Unit)? = null,
    allowDismiss: Boolean = true,
    content: @Composable () -> Unit,
) {
    val advancedMaterial = LocalAdvancedMaterialSpec.current
    val shape = RoundedCornerShape(36.dp)
    val surface = rememberMiuixModalSurface(
        kind = MiuixModalKind.BottomSheet,
        shape = shape,
        recipe = DeadlinerGlassRecipes.bottomSheet(advancedMaterial.fineTuning),
    )

    OverlayBottomSheet(
        show = show,
        title = title,
        startAction = startAction,
        endAction = endAction,
        modifier = modifier.then(surface.modifier),
        backgroundColor = surface.backgroundColor,
        cornerRadius = 36.dp,
        sheetMaxWidth = 560.dp,
        // Content callers own their horizontal rhythm. Keeping OverlayBottomSheet's default 24dp
        // here would be added again to their 20dp content padding.
        insideMargin = DpSize(0.dp, 0.dp),
        renderInRootScaffold = true,
        allowDismiss = allowDismiss,
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        content = content,
    )
}
