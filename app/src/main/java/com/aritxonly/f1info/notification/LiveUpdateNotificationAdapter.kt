package com.aritxonly.f1info.notification

import android.os.Build

/** Platform capability rule copied from Deadliner LiveUpdateNotificationAdapter. */
internal object LiveUpdateNotificationAdapter {
    fun supportsIsland(): Boolean = Build.VERSION.SDK_INT >= 35 && listOf("xiaomi", "redmi", "poco").any {
        Build.MANUFACTURER.equals(it, true) || Build.BRAND.equals(it, true)
    }
}
