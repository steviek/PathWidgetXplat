package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.util.NonEmptyList

/**
 * A route is a journey taken by a train. It may be considered part of multiple lines.
 *
 * e.g. 'NWK-WTC' is both a line and a route. However, 'JSQ-33S via HOB' is only a route, and
 * includes the 'JSQ-33S' and 'HOB-33S' lines.
 */
data class Route(val lines: NonEmptyList<Line>, val stops: NonEmptyList<Station>)

val Route.origin: Station get() = stops.first()
val Route.destination: Station get() = stops.last()
