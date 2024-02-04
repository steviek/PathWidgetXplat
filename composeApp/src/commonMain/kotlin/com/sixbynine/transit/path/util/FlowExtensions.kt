package com.sixbynine.transit.path.util

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

fun <T, R> StateFlow<T>.mapState(block: (T) -> R): StateFlow<R> {
    val delegate = this
    return object : StateFlow<R> {
        override val replayCache: List<R>
            get() = delegate.replayCache.map(block)

        override val value: R
            get() = block(delegate.value)

        override suspend fun collect(collector: FlowCollector<R>): Nothing {
            delegate.collect { value -> collector.emit(block(value)) }
        }
    }
}

fun <A, B, C, D, E, T> combineStates(
    flow1: StateFlow<A>,
    flow2: StateFlow<B>,
    flow3: StateFlow<C>,
    flow4: StateFlow<D>,
    flow5: StateFlow<E>,
    block: (A, B, C, D, E) -> T
): StateFlow<T> {
    return object : StateFlow<T> {
        override val replayCache: List<T>
            get() = listOf(value)

        override val value: T
            get() = block(flow1.value, flow2.value, flow3.value, flow4.value, flow5.value)

        override suspend fun collect(collector: FlowCollector<T>): Nothing {
            combine(flow1, flow2, flow3, flow4, flow5) { a, b, c, d, e ->
                block(a, b, c, d, e)
            }
                .collect { collector.emit(it) }
            awaitCancellation()
        }
    }
}
