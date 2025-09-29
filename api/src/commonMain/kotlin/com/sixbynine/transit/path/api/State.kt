package com.desaiwang.transit.path.api

import com.desaiwang.transit.path.api.State.NewJersey
import com.desaiwang.transit.path.api.State.NewYork
import com.desaiwang.transit.path.location.Location

enum class State {
    NewJersey, NewYork
}

fun State.other(): State = when(this) {
    NewJersey -> NewYork
    NewYork -> NewJersey
}

val Station.state: State
    get() = if (coordinates.longitude > -74.020) State.NewYork else State.NewJersey

val Location.state: State
    get() = if (longitude > -74.020) State.NewYork else State.NewJersey

infix fun Station.isEastOf(other: Station): Boolean {
    return coordinates.longitude > other.coordinates.longitude
}

infix fun Station.isWestOf(other: Station): Boolean {
    return coordinates.longitude < other.coordinates.longitude
}

val Station.isInNewJersey get() = state == State.NewJersey
val Station.isInNewYork get() = state == State.NewYork