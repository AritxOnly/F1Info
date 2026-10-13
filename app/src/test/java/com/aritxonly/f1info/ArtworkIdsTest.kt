package com.aritxonly.f1info

import com.aritxonly.f1info.data.driverArtworkId
import org.junit.Assert.assertEquals
import org.junit.Test

class ArtworkIdsTest {
    @Test fun lindbladApiIdentityResolvesToBundledAndCachedArtwork() {
        assertEquals("lindblad", driverArtworkId("arvid_lindblad"))
        assertEquals("lindblad", driverArtworkId("lindblad"))
    }
    @Test fun tsunodaAndUnrelatedDriverIdentitiesRemainAvailable() {
        assertEquals("tsunoda", driverArtworkId("tsunoda"))
        assertEquals("tsunoda", driverArtworkId("yuki_tsunoda"))
        assertEquals("max_verstappen", driverArtworkId("max_verstappen"))
        assertEquals("unknown", driverArtworkId("unknown"))
    }
}
