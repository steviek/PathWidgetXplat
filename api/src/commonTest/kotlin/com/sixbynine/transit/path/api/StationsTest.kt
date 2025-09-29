package com.desaiwang.transit.path.api

import com.desaiwang.transit.path.location.Location
import kotlin.test.Test
import kotlin.test.assertEquals

class StationsTest {
    @Test
    fun `random pin`() {
        val location = Location(40.742114, -73.990503)
        val closestStations = Stations.byProximityTo(location)

        assertEquals(Stations.TwentyThirdStreet, closestStations.first())
    }
}
