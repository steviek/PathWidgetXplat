package com.desaiwang.transit.path.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

suspend fun Flow<Boolean>.awaitTrue() = first { it }

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

inline fun <A, B, T> combineStates(
    flow1: StateFlow<A>,
    flow2: StateFlow<B>,
    crossinline block: (A, B) -> T
): StateFlow<T> {
    return object : StateFlow<T> {
        override val replayCache: List<T>
            get() = listOf(value)

        override val value: T
            get() = block(flow1.value, flow2.value)

        override suspend fun collect(collector: FlowCollector<T>): Nothing {
            combine(flow1, flow2) { a, b ->
                block(a, b)
            }
                .collect { collector.emit(it) }
            awaitCancellation()
        }
    }
}

inline fun <A, B, C, D, E, T> combineStates(
    flow1: StateFlow<A>,
    flow2: StateFlow<B>,
    flow3: StateFlow<C>,
    flow4: StateFlow<D>,
    flow5: StateFlow<E>,
    crossinline block: (A, B, C, D, E) -> T
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

inline fun <A, B, C, D, E, F, T> combineStates(
    flow1: StateFlow<A>,
    flow2: StateFlow<B>,
    flow3: StateFlow<C>,
    flow4: StateFlow<D>,
    flow5: StateFlow<E>,
    flow6: StateFlow<F>,
    crossinline block: (A, B, C, D, E, F) -> T
): StateFlow<T> {
    return combineStates(
        flow1,
        flow2,
        flow3,
        flow4,
        combineStates(flow5, flow6, ::Pair)
    ) { a, b, c, d, (e, f) -> block(a, b, c, d, e, f) }
}

inline fun <A, B, C, D, E, F, G, T> combineStates(
    flow1: StateFlow<A>,
    flow2: StateFlow<B>,
    flow3: StateFlow<C>,
    flow4: StateFlow<D>,
    flow5: StateFlow<E>,
    flow6: StateFlow<F>,
    flow7: StateFlow<G>,
    crossinline block: (A, B, C, D, E, F, G) -> T
): StateFlow<T> {
    return combineStates(
        flow1,
        flow2,
        flow3,
        combineStates(flow4, flow5, ::Pair),
        combineStates(flow6, flow7, ::Pair)
    ) { a, b, c, (d, e), (f, g) -> block(a, b, c, d, e, f, g) }
}

inline fun <A, B, C, D, E, F, G, H, T> combineStates(
    flow1: StateFlow<A>,
    flow2: StateFlow<B>,
    flow3: StateFlow<C>,
    flow4: StateFlow<D>,
    flow5: StateFlow<E>,
    flow6: StateFlow<F>,
    flow7: StateFlow<G>,
    flow8: StateFlow<H>,
    crossinline block: (A, B, C, D, E, F, G, H) -> T
): StateFlow<T> {
    return combineStates(
        flow1,
        flow2,
        combineStates(flow3, flow4, ::Pair),
        combineStates(flow5, flow6, ::Pair),
        combineStates(flow7, flow8, ::Pair)
    ) { a, b, (c, d), (e, f), (g, h) -> block(a, b, c, d, e, f, g, h) }
}


inline fun <A, B, C, D, E, F, G, H, I, T> combineStates(
    flow1: StateFlow<A>,
    flow2: StateFlow<B>,
    flow3: StateFlow<C>,
    flow4: StateFlow<D>,
    flow5: StateFlow<E>,
    flow6: StateFlow<F>,
    flow7: StateFlow<G>,
    flow8: StateFlow<H>,
    flow9: StateFlow<I>,
    crossinline block: (A, B, C, D, E, F, G, H, I) -> T
): StateFlow<T> {
    return combineStates(
        combineStates(flow1, flow2, ::Pair),
        combineStates(flow3, flow4, ::Pair),
        combineStates(flow5, flow6, ::Pair),
        combineStates(flow7, flow8, ::Pair),
        flow9,
    ) { (a, b), (c, d), (e, f), (g, h), i -> block(a, b, c, d, e, f, g, h, i) }
}

fun <T> Flow<T>.collectIn(scope: CoroutineScope, block: suspend (T) -> Unit) {
    scope.launch { collect { block(it) } }
}

fun <T> stateFlowOf(value: T): StateFlow<T> {
    return MutableStateFlow(value).asStateFlow()
}
