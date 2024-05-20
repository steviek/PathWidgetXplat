package com.sixbynine.transit.path.util

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

interface TransformableDeferred<out T> {
    suspend fun await(): T
}

private class TransformableDeferredFromDeferred<T>(
    private val delegate: Deferred<T>
) : TransformableDeferred<T> {
    override suspend fun await(): T {
        return delegate.await()
    }
}

private class TransformableDeferredWithTransform<T, R>(
    private val delegate: TransformableDeferred<T>,
    private val transform: (T) -> R
) : TransformableDeferred<R> {

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
