package com.pompi.emoji_sarj

import org.junit.Assert.*
import org.junit.Test

class ChargingPolicyTest {
    @Test fun `charging session hides during sleep lock and full screen`() {
        assertTrue("Unlocked charging", ChargingPolicy.showSmall(true, true, false, false))
        assertFalse("Screen switched off", ChargingPolicy.showSmall(true, false, false, false))
        assertFalse("Woken but still locked", ChargingPolicy.showSmall(true, true, true, false))
        assertTrue("User unlocked", ChargingPolicy.showSmall(true, true, false, false))
        assertFalse("Large scene owns the character", ChargingPolicy.showSmall(true, true, false, true))
        assertTrue("Returned from preview", ChargingPolicy.showSmall(true, true, false, false))
        assertFalse("Cable removed", ChargingPolicy.showSmall(false, true, false, false))
    }
}
