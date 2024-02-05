package com.sixbynine.transit.path.util

sealed interface DataResult<T> {
    val data: T?

    data class Success<T>(override val data: T) : DataResult<T>
    data class Error<T>(val error: Throwable, override val data: T?) : DataResult<T>
    data class Loading<T>(override val data: T?) : DataResult<T>

    companion object {
        fun <T> success(data: T): DataResult<T> = Success(data)
        fun <T> error(error: Throwable, data: T? = null): DataResult<T> = Error(error, data)
        fun <T> loading(data: T? = null): DataResult<T> = Loading(data)
    }
}
