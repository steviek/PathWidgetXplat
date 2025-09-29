package com.desaiwang.transit.path

fun interface NonFatalReporter {
    fun report(e: Throwable)
}
