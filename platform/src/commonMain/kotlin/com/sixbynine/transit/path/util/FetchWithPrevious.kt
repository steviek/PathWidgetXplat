package com.sixbynine.transit.path.util

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlin.time.Duration.Companion.seconds

data class FetchWithPrevious<T>(
    val fetch: TransformableDeferred<DataResult<T>>,
    val previous: AgedValue<T>?,
) {
    constructor(
        fetch: Deferred<DataResult<T>>,
        previous: AgedValue<T>?,
    ) : this(
        fetch = fetch.transformable(),
        previous = previous,
    )

    constructor(validValue: AgedValue<T>) : this(
        fetch = CompletableDeferred(DataResult.success(validValue.value)),
        previous = validValue,
    )

    companion object {
        fun <T> create(
            previous: AgedValue<T>?,
            fetch: () -> Deferred<DataResult<T>>,
            staleness: Staleness,
        ): FetchWithPrevious<T> {
            if (previous == null || previous.age >= staleness.invalidAfter) {
                return FetchWithPrevious(
                    fetch = fetch(),
                    previous = null,
                )
            }

            if (previous.age >= staleness.staleAfter || staleness.staleAfter <= 0.seconds) {
                return FetchWithPrevious(
                    fetch = fetch(),
                    previous = previous,
                )
            }

            return FetchWithPrevious(
                fetch = CompletableDeferred(DataResult.success(previous.value)),
                previous = previous,
            )
        }
    }
}

fun <T, R> FetchWithPrevious<T>.map(
    transform: (T) -> R
): FetchWithPrevious<R> {
    return FetchWithPrevious(
        fetch = fetch.map { it.map(transform) },
        previous = previous?.map(transform),
    )
}

suspend fun <T> FetchWithPrevious<T>.await(): DataResult<T> {
    return fetch.await()
}
