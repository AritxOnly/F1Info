package com.aritxonly.f1info

import com.aritxonly.f1info.widgets.WidgetConfiguration
import com.aritxonly.f1info.widgets.WidgetMarginPreset
import org.junit.Assert.*
import org.junit.Test

class WidgetConfigurationTest {
    @Test fun editorBoundsProtectSmallWidgets() {
        val c = WidgetConfiguration(-1f, 100, -3, "leclerc", false).normalized()
        assertEquals(0f, c.opacity, 0f)
        assertEquals(28, c.topPadding)
        assertEquals(0, c.bottomPadding)
        assertEquals("leclerc", c.driverId)
        assertFalse(c.lightText)
    }
    @Test fun transparencyAndZeroInsetsRemainPossible() {
        val c = WidgetConfiguration(0f, 0, 0).normalized()
        assertEquals(0f, c.opacity, 0f)
        assertEquals(0, c.topPadding + c.bottomPadding)
    }
    @Test fun invalidOpacityFallsBack() {
        assertEquals(.96f, WidgetConfiguration(Float.NaN).normalized().opacity, 0f)
        assertEquals(1f, WidgetConfiguration(2f).normalized().opacity, 0f)
    }
    @Test fun namedPresetsResolveCardMarginsIndependentOfCustomDrafts() {
        val expected = listOf(0 to 0, 16 to 2, 12 to 0, 18 to 2)
        WidgetMarginPreset.entries.take(4).forEachIndexed { i, preset ->
            val c = WidgetConfiguration(topPadding = 25, bottomPadding = 10, marginPreset = preset)
            assertEquals(expected[i].first, c.topMarginDp())
            assertEquals(expected[i].second, c.bottomMarginDp())
        }
    }
    @Test fun customMarginsCannotConsumeMoreThanFortyDp() {
        val c = WidgetConfiguration(topPadding = 90, bottomPadding = 90,
            marginPreset = WidgetMarginPreset.Custom, cornerRadiusDp = 100).normalized()
        assertEquals(28, c.topMarginDp()); assertEquals(12, c.bottomMarginDp())
        assertEquals(32, c.cornerRadiusDp)
        assertEquals(8, c.copy(cornerRadiusDp = -5).normalized().cornerRadiusDp)
    }
    @Test fun switchingPresetsPreservesCustomMarginDrafts() {
        val custom = WidgetConfiguration(topPadding = 18, bottomPadding = 7, marginPreset = WidgetMarginPreset.Custom)
        val standard = custom.copy(marginPreset = WidgetMarginPreset.Default).normalized()
        assertEquals(0, standard.topMarginDp() + standard.bottomMarginDp())
        assertEquals(custom, standard.copy(marginPreset = WidgetMarginPreset.Custom))
    }
    @Test fun legacyOrUnknownPresetUsesDefaultRatherThanOldContentPadding() {
        for (stored in listOf(null, "unknown")) {
            val legacy = WidgetConfiguration(topPadding = 12, bottomPadding = 12,
                marginPreset = WidgetMarginPreset.fromStoredValue(stored)).normalized()
            assertEquals(0, legacy.topMarginDp() + legacy.bottomMarginDp())
        }
        assertEquals(22, WidgetConfiguration().cornerRadiusDp)
    }
}
