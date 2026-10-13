package com.aritxonly.f1info.platform

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Read-only public provider contract from HyperModifier f829eb3 ModifierSettingsProvider. */
object HyperModifierNavigation {
    const val AUTHORITY = "com.aritxonly.myhypermodifier.settings"
    const val KEY = "hyper_glassify_hidden_navigation_lift"
    fun read(context: Context): Float? = runCatching {
        val bundle = context.contentResolver.call(Uri.parse("content://$AUTHORITY"), "get_settings", null, null)
        bundle?.takeIf { it.containsKey(KEY) }?.getFloat(KEY)?.takeIf { it.isFinite() }?.coerceIn(0f, 48f)
    }.getOrNull()
}

@Composable
fun rememberHyperModifierLift(): Float {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var lift by remember { mutableFloatStateOf(0f) }
    LifecycleResumeEffect(Unit) {
        val job = scope.launch { lift = withContext(Dispatchers.IO) { HyperModifierNavigation.read(context) ?: 0f } }
        onPauseOrDispose { job.cancel() }
    }
    return lift
}
