package com.pompi.emoji_sarj

import org.junit.Assert.*
import org.junit.Test

class HoseGeometryTest {
    @Test fun `hose mouth follows each sprite during bob and resize`() {
        for (frame in 0..3) {
            val small = HoseGeometry.mouth(frame, 0f, 0f, 48f, 48f)
            assertTrue("Connector must be on the right side, not over the face", small.x > 43f && small.x < 48f)
            assertTrue("Connector must be above the floor hose", small.y > 33f && small.y < 39f)
            val bobbed = HoseGeometry.mouth(frame, 0f, -3f, 48f, 48f)
            assertEquals(small.x, bobbed.x, .001f)
            assertEquals(small.y - 3f, bobbed.y, .001f)
            val large = HoseGeometry.mouth(frame, 0f, 160f, 219f, 219f)
            assertTrue("Large battery socket must remain to the right of the mouth", large.x < 222f)
            assertTrue("Mouth and battery socket are inside the same scene", large.y in 300f..350f)
        }
    }
}
