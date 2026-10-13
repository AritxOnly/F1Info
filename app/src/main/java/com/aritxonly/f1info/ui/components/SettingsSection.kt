package com.aritxonly.f1info.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

// Adapted from Deadliner ui/settings/SettingsComponents.kt, revision 210e4a2.
// Original grouping, 8dp spacing, 24dp labels, 24dp content radius unified, including emphasis cards (user preference).
// Original attribution: https://github.com/YangDai2003/OpenNote-Compose/
// Local change: Material Surface/color/typography replaced with MIUIX roles.
@Composable
fun SettingsSection(modifier: Modifier = Modifier, topLabel: String? = null,
    mainContent: Boolean = false, enabled: Boolean = false, customColor: Color? = null,
    containerAlpha: Float = .76f, clipContent: Boolean = true,
    content: @Composable ColumnScope.() -> Unit) {
    val colors = MiuixTheme.colorScheme
    val container = customColor ?: if (enabled && mainContent) colors.primaryContainer else colors.surfaceContainer
    Column(modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        topLabel?.let { Text(it, style = MiuixTheme.textStyles.footnote1, color = colors.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) }
        val shape = RoundedCornerShape(24.dp)
        Column(Modifier.fillMaxWidth().then(if (clipContent) Modifier.clip(shape) else Modifier)
            .background(container.copy(alpha = container.alpha * containerAlpha)), content = content)
    }
}

@Composable
fun SectionTitle(title: String, trailing: String? = null,
    trailingColor: Color = MiuixTheme.colorScheme.onSurfaceVariantSummary) {
    Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f), style = MiuixTheme.textStyles.subtitle, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        trailing?.let { Text(it, modifier = Modifier.widthIn(max = 200.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End, style = MiuixTheme.textStyles.footnote1, color = trailingColor) }
    }
}
@Composable
fun InfoRow(label: String, value: String, emphasis: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(label, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, modifier = Modifier.weight(1f),
            style = MiuixTheme.textStyles.body2)
        Text(value, modifier = Modifier.weight(1.3f), style = MiuixTheme.textStyles.body2,
            fontFamily = if (emphasis) com.aritxonly.f1info.ui.theme.RacingDisplayFamily else null,
            fontWeight = if (emphasis) androidx.compose.ui.text.font.FontWeight.SemiBold else null,
            textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}
