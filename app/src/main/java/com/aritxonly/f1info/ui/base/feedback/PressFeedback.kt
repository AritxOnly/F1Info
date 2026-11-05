package com.aritxonly.f1info.ui.base.feedback

// Control feedback dependency from Deadliner; app-wide Material ripple providers omitted.
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixIndication
import top.yukonga.miuix.kmp.utils.SinkFeedback
import top.yukonga.miuix.kmp.utils.pressable

internal val DefaultClickFeedback = SinkFeedback()

/** For hosts that already contain a clickable child, including glass navigation buttons. */
@Composable
internal fun Modifier.miuixControlFeedback(
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    overlay: Boolean = true,
): Modifier {
    val source = remember { MutableInteractionSource() }
    val color = MiuixTheme.colorScheme.onSurface
    val indication = remember(color) { MiuixIndication(color) }
    return pressable(source, indication = DefaultClickFeedback, enabled = enabled)
        .then(if (overlay) Modifier.clip(shape).indication(source, indication) else Modifier)
}
