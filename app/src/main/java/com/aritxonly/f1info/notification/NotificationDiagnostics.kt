package com.aritxonly.f1info.notification

/** Application logging integration only; no authentication values enter logs. */
internal object NotificationDiagnostics {
    fun event(name: String, vararg fields: Pair<String, Any?>) { android.util.Log.d("F1Island", "$name ${fields.joinToString { it.first + "=" + it.second }}") }
    fun failure(name: String, error: Throwable) { android.util.Log.w("F1Island", name, error) }
}
