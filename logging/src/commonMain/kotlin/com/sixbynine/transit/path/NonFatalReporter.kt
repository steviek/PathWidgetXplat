package com.sixbynine.transit.path

fun interface NonFatalReporter {
    fun report(e: Throwable)
}
