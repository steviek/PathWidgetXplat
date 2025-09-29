package com.desaiwang.transit.path.util

import com.desaiwang.transit.path.util.DataResult.Failure
import com.desaiwang.transit.path.util.DataResult.Success
import kotlin.contracts.contract
import kotlin.jvm.JvmName

sealed interface DataResult<T> {
    val data: T?

    data class Success<T>(override val data: T) : DataResult<T>
    data class Failure<T>(
        val error: Throwable,
        val hadInternet: Boolean,
        override val data: T?
    ) : DataResult<T>

    data class Loading<T>(override val data: T?) : DataResult<T>

    companion object {
        fun <T> success(data: T): DataResult<T> = Success(data)
        fun <T> failure(error: Throwable, hadInternet: Boolean, data: T? = null): DataResult<T> {
            return Failure(error, hadInternet, data)
        }

        fun <T> loading(data: T? = null): DataResult<T> = Loading(data)
    }
}

fun <T> DataResult<T>.isSuccess(): Boolean {
    contract { returns(true) implies (this@isSuccess is Success<T>) }
    return this is Success
}

fun <T> DataResult<T>.isLoading(): Boolean {
    contract { returns(true) implies (this@isLoading is DataResult.Loading<T>) }
    return this is DataResult.Loading
}

fun <T> DataResult<T>.isFailure(): Boolean {
    contract { returns(true) implies (this@isFailure is Failure<T>) }
    return this is Failure
}

inline fun <T, R> DataResult<T>.fold(
    onSuccess: (T) -> R,
    onError: (error: Throwable, hadInternet: Boolean, data: T?) -> R,
    onLoading: (T?) -> R
): R {
    return when (this) {
        is DataResult.Success -> onSuccess(data)
        is DataResult.Failure -> onError(error, hadInternet, data)
        is DataResult.Loading -> onLoading(data)
    }
}

inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> {
    return when (this) {
        is DataResult.Success -> DataResult.success(transform(data))
        is DataResult.Failure -> DataResult.failure(error, hadInternet, data?.let(transform))
        is DataResult.Loading -> DataResult.loading(data?.let(transform))
    }
}

inline fun <T> DataResult<T>.onSuccess(action: (T) -> Unit): DataResult<T> {
    if (this is DataResult.Success) {
        action(data)
    }
    return this
}

inline fun <T> DataResult<T>.onFailure(
    action: (error: Throwable, hadInternet: Boolean, data: T?) -> Unit
): DataResult<T> {
    if (this is DataResult.Failure) {
        action(error, hadInternet, data)
    }
    return this
}

inline fun <T> DataResult<T>.onLoading(action: (T?) -> Unit): DataResult<T> {
    if (this is DataResult.Loading) {
        action(data)
    }
    return this
}

fun <T> Result<T>.toDataResult(): DataResult<T> {
    return fold(
        onSuccess = { DataResult.success(it) },
        onFailure = { DataResult.failure(it, hadInternet = true, data = null) }
    )
}

@JvmName("combineDataResults")
inline fun <A, B, C> combine(
    first: DataResult<A>,
    second: DataResult<B>,
    transform: (A, B) -> C
): DataResult<C> {
    if (first is Success && second is Success) {
        return DataResult.success(transform(first.data, second.data))
    }

    val data = ifNotNull(first.data, second.data, transform)
    return when {
        first is Failure -> DataResult.failure(first.error, first.hadInternet, data)
        second is Failure -> DataResult.failure(second.error, second.hadInternet, data)
        else -> DataResult.loading(data)
    }
}

inline fun <A, B, C> DataResult<A>.combine(
    other: DataResult<B>,
    transform: (A, B) -> C
): DataResult<C> {
    return combine(this, other, transform)
}

fun <T> DataResult<DataResult<T>>.flatten(): DataResult<T> {
    return fold(
        onSuccess = { it },
        onError = { error, hadInternet, data ->
            DataResult.failure(error, hadInternet, data?.data)
        },
        onLoading = { data ->
            DataResult.loading(data?.data)
        }
    )
}
