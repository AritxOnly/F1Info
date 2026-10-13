package com.aritxonly.f1info

import com.aritxonly.f1info.widgets.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class WidgetRulesTest {
    @Test fun widgetUsesCoarseCountdownForPeriodicUpdates() {
        assertEquals("正赛 · 约 1 天 2 小时", widgetCountdown(Instant.EPOCH.plusSeconds(26 * 3600 + 1800), Instant.EPOCH))
        assertEquals("正赛 · 不足 1 小时", widgetCountdown(Instant.EPOCH.plusSeconds(600), Instant.EPOCH))
    }
    @Test fun widgetDoesNotInventUnpublishedTime() {
        assertEquals("正赛时间待公布", widgetCountdown(null, Instant.EPOCH))
        assertEquals("正赛已开始", widgetCountdown(Instant.EPOCH, Instant.EPOCH))
    }
    @Test fun scheduleRespondsToRealCellDimensions() {
        assertEquals(WidgetLayout.Compact, widgetLayout(160f, 170f))
        assertEquals(WidgetLayout.Wide, widgetLayout(340f, 170f))
        assertEquals(WidgetLayout.Expanded, widgetLayout(340f, 350f))
    }
    @Test fun onlyLargeWidthAndHeightUseExpandedLayout() {
        assertEquals(WidgetLayout.Compact, widgetLayout(160f, 350f))
        assertEquals(WidgetLayout.Wide, widgetLayout(230f, 229f))
        assertEquals(WidgetLayout.Expanded, widgetLayout(230f, 230f))
    }
    @Test fun seasonEndHasNoFabricatedNearbyRaces() {
        assertEquals(emptyList<Any>(), nearbyRaces(emptyList(), null))
    }
}
