package com.sixbynine.transit.path.api

enum class State {
    NewJersey, NewYork
}

val Station.state: State
    get() = if (coordinates.longitude > -74.020) State.NewYork else State.NewJersey

infix fun Station.isEastOf(other: Station): Boolean {
    return coordinates.longitude > other.coordinates.longitude
}

infix fun Station.isWestOf(other: Station): Boolean {
    return coordinates.longitude < other.coordinates.longitude
}

val Station.isInNewJersey get() = state == State.NewJersey
val Station.isInNewYork get() = state == State.NewYork