package com.desaiwang.transit.path.widget.configuration

import com.desaiwang.transit.path.util.JsonFormat
import org.junit.Test
import kotlin.test.assertEquals

class StoredWidgetConfigurationTest {
    @Test
    fun `upgrade from v17`() {
        val configuration = JsonFormat.decodeFromString<StoredWidgetConfiguration>(V17_Data)
            .migrateToCurrentVersion()

        assertEquals(5, configuration.version)
        assertEquals(setOf("NEW", "EXP", "WTC"), configuration.fixedStations)
    }

    private companion object {
        const val V17_Data = """
            {
              "loadedData": {
                "stationAndTrains": [
                  {
                    "first": {
                      "id": -1,
                      "mRazzaApiName": "NEWPORT",
                      "pathApiName": "NEW",
                      "displayName": "Newport",
                      "coordinates": {
                        "latitude": 40.72699,
                        "longitude": -74.03383
                      }
                    },
                    "second": [
                      {
                        "headsign": "Journal Square via Hoboken",
                        "projectedArrival": 1708352832434,
                        "lineColors": [
                          -11693317,
                          -26368
                        ]
                      },
                      {
                        "headsign": "Journal Square via Hoboken",
                        "projectedArrival": 1708353431434,
                        "lineColors": [
                          -11693317,
                          -26368
                        ]
                      },
                      {
                        "headsign": "33rd Street via Hoboken",
                        "projectedArrival": 1708353170201,
                        "lineColors": [
                          -11693317,
                          -26368
                        ]
                      },
                      {
                        "headsign": "33rd Street via Hoboken",
                        "projectedArrival": 1708353882201,
                        "lineColors": [
                          -11693317,
                          -26368
                        ]
                      }
                    ]
                  }
                ],
                "updateTimeMillis": 1708352791648
              },
              "lastRefresh": {
                "time": {
                  "elapsedSinceBootMillis": 359452259,
                  "bootCount": 4
                }
              },
              "fixedStations": [
                "NEWPORT",
                "EXCHANGE_PLACE",
                "WORLD_TRADE_CENTER"
              ],
              "useClosestStation": true,
              "sortOrder": "Alphabetical"
            }
        """
    }
}