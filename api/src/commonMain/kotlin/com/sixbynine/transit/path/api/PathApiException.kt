package com.sixbynine.transit.path.api

abstract class PathApiException : RuntimeException() {
    data object NoResults : PathApiException()
}
