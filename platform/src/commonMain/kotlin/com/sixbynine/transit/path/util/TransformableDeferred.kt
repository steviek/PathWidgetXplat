package com.desaiwang.transit.path.util

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlin.jvm.JvmName

interface TransformableDeferred<out T> {
    fun start()

    suspend fun await(): T
}

private class TransformableDeferredFromDeferred<T>(
    private val delegate: Deferred<T>
) : TransformableDeferred<T> {
    override fun start() {
        delegate.start()
    }

    override suspend fun await(): T {
        return delegate.await()
    }
}

private class TransformableDeferredWithTransform<T, R>(
    private val delegate: TransformableDeferred<T>,
    private val transform: (T) -> R
) : TransformableDeferred<R> {

    override fun start() {
        delegate.start()
    }

    private val completed = CompletableDeferred<R>()

    override suspend fun await(): R {
        if (!completed.isCompleted) {
            val result = delegate.await()
            completed.complete(transform(result))
        }
        return completed.getCompleted()
    }
}

fun <T> Deferred<T>.transformable(): TransformableDeferred<T> {
    return TransformableDeferredFromDeferred(this)
}

fun <T, R> TransformableDeferred<T>.map(transform: (T) -> R): TransformableDeferred<R> {
    return TransformableDeferredWithTransform(this, transform)
}

fun <T, R> Deferred<T>.map(transform: (T) -> R): TransformableDeferred<R> {
    return transformable().map(transform)
}

@JvmName("combineTransformableDeferreds")
inline fun <A, B, C> combine(
    first: TransformableDeferred<A>,
    second: TransformableDeferred<B>,
    crossinline transform: (A, B) -> C
): TransformableDeferred<C> {
    return object : TransformableDeferred<C> {
        override fun start() {
            first.start()
            second.start()
        }

        override suspend fun await(): C {
            return transform(first.await(), second.await())
        }
    }
}

fun <A, B, C> TransformableDeferred<A>.combine(
    other: TransformableDeferred<B>,
    transform: (A, B) -> C
): TransformableDeferred<C> {
    return combine(this, other, transform)
}
