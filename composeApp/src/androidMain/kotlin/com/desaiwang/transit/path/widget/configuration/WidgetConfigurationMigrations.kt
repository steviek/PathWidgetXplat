package com.desaiwang.transit.path.widget.configuration

import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.Stations
import com.desaiwang.transit.path.preferences.IntPersistable

fun StoredWidgetConfiguration.migrateToCurrentVersion(): StoredWidgetConfiguration {
    var configuration = this

    if (version < 2) {
        configuration = configuration.copy(
            version = 2,
            fixedStations = fixedStations?.mapNotNull { razzaId ->
                val station = when (razzaId) {
                    "NEWARK" -> Stations.Newark
                    "HARRISON" -> Stations.Harrison
                    "JOURNAL_SQUARE" -> Stations.JournalSquare
                    "GROVE_STREET" -> Stations.GroveStreet
                    "EXCHANGE_PLACE" -> Stations.ExchangePlace
                    "WORLD_TRADE_CENTER" -> Stations.WorldTradeCenter
                    "NEWPORT" -> Stations.Newport
                    "HOBOKEN" -> Stations.Hoboken
                    "CHRISTOPHER_STREET" -> Stations.ChristopherStreet
                    "NINTH_STREET" -> Stations.NinthStreet
                    "FOURTEENTH_STREET" -> Stations.FourteenthStreet
                    "TWENTY_THIRD_STREET" -> Stations.TwentyThirdStreet
                    "THIRTY_THIRD_STREET" -> Stations.ThirtyThirdStreet
                    else -> return@mapNotNull null
                }
                station.pathApiName
            }?.toSet()
        )
    }

    if (version < 3) {
        configuration = configuration.copy(
            version = 3,
            linesBitmask = IntPersistable.createBitmask(Line.permanentLines)
        )
    }

    if (version < 5) {
        configuration = configuration.copy(version = 5)
    }

    return configuration
}