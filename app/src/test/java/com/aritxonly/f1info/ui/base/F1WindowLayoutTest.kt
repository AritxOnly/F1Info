package com.aritxonly.f1info.ui.base

import org.junit.Assert.assertEquals
import org.junit.Test

class F1WindowLayoutTest {
    @Test fun wideWindowBoundariesMatchDeadliner() {
        assertEquals(F1WindowLayout.Phone, f1WindowLayout(399f, 600f))
        assertEquals(F1WindowLayout.Phone, f1WindowLayout(412f, 892f))
        assertEquals(F1WindowLayout.Phone, f1WindowLayout(720f, 399f))
        assertEquals(F1WindowLayout.WideCover, f1WindowLayout(400f, 660f))
        assertEquals(F1WindowLayout.Phone, f1WindowLayout(400f, 661f))
        assertEquals(F1WindowLayout.WideCover, f1WindowLayout(432f, 634f))
    }

    @Test fun onlyCompactCoversCollapseTheDock() {
        assertEquals(F1WindowLayout.WideCover, f1WindowLayout(599f, 700f))
        assertEquals(F1WindowLayout.WideFold, f1WindowLayout(600f, 700f))
        assertEquals(F1WindowLayout.WideFold, f1WindowLayout(839f, 700f))
        assertEquals(F1WindowLayout.Tablet, f1WindowLayout(840f, 700f))
        assertEquals(F1WindowLayout.Tablet, f1WindowLayout(900f, 1280f))
    }
}
