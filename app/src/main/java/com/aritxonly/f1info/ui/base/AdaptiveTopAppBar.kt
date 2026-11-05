package com.aritxonly.f1info.ui.base

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal enum class F1WindowLayout {
    Phone, WideCover, WideFold, Tablet;

    val fixedHeader get() = this != Phone
}

internal fun f1WindowLayout(widthDp: Float, heightDp: Float): F1WindowLayout = when {
    widthDp >= 840f -> F1WindowLayout.Tablet
    widthDp < 400f || heightDp < 400f || heightDp / widthDp > 1.65f -> F1WindowLayout.Phone
    widthDp < 600f -> F1WindowLayout.WideCover
    else -> F1WindowLayout.WideFold
}

// Measure the window before IME insets or tablet pane allocation, as in Deadliner.
@Composable
internal fun currentF1WindowLayout(): F1WindowLayout {
    val window = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current.density
    return f1WindowLayout(window.width / density, window.height / density)
}

@Composable
internal fun LazyListState.fixedHeaderProgress(): Float {
    val density = LocalDensity.current
    return remember(this, density) {
        derivedStateOf {
            if (firstVisibleItemIndex > 0) 1f else with(density) {
                val fraction = ((firstVisibleItemScrollOffset - 4.dp.toPx()) / 24.dp.toPx()).coerceIn(0f, 1f)
                fraction * fraction * (3f - 2f * fraction)
            }
        }
    }.value
}

/** Fixed headers have no scroll connection, including MIUIX's post-fling settling. */
@Composable
internal fun F1TopAppBar(
    title: String,
    behavior: ScrollBehavior?,
    subtitle: String = "",
    small: Boolean = false,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    tabs: (@Composable () -> Unit)? = null,
) {
    if (behavior == null) {
        Row(
            Modifier.fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                .heightIn(min = TopAppBarDefaults.CollapsedHeight)
                .padding(start = TopAppBarDefaults.NavigationIconPadding, end = TopAppBarDefaults.ActionIconPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navigationIcon()
            Column(Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 4.dp)) {
                if (tabs != null) tabs() else if (!small) {
                    Text(title, style = MiuixTheme.textStyles.title2, maxLines = 1,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.semantics { heading() })
                    if (subtitle.isNotEmpty()) Text(subtitle, style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, content = actions)
        }
    } else if (small) {
        SmallTopAppBar(title = "", color = Color.Transparent, actions = actions, scrollBehavior = behavior)
    } else {
        TopAppBar(title = title, subtitle = subtitle, color = Color.Transparent, scrollBehavior = behavior,
            navigationIcon = navigationIcon, actions = actions, bottomContent = {
                if (tabs != null) Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) { tabs() }
            })
    }
}
