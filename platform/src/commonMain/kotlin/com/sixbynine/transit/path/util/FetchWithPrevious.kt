package com.sixbynine.transit.path.util

data class FetchWithPrevious<T>(
    val fetch: suspend () -> DataResult<T>,
    val previous: AgedValue<T>?,
)

fun <T, R> FetchWithPrevious<T>.map(transform: (T) -> R): FetchWithPrevious<R> {
    return FetchWithPrevious(
        fetch = { fetch().map(transform) },
        previous = previous?.map(transform),
    )
}
