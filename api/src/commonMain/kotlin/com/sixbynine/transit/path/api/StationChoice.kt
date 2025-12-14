package com.sixbynine.transit.path.api

sealed interface StationChoice {
    data class Fixed(val station: Station) : StationChoice
    data object Closest : StationChoice
}
