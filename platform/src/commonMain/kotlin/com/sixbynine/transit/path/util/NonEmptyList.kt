package com.sixbynine.transit.path.util

class NonEmptyList<T> internal constructor(private val list: List<T>) : List<T> by list {
    init {
        require(list.isNotEmpty())
    }

    override fun equals(other: Any?): Boolean {
        if (other is NonEmptyList<*>) {
            return list == other.list
        } else {
            return list == other
        }
    }

    override fun hashCode(): Int {
        return list.hashCode()
    }

    override fun toString(): String {
        return list.toString()
    }
}

fun <T> List<T>.toNonEmptyList(): NonEmptyList<T>? {
    return if (this.isNotEmpty()) NonEmptyList(this) else null
}

fun <T> nonEmptyListOf(first: T, vararg others: T): NonEmptyList<T> {
    val list = listOf(first) + others
    return NonEmptyList(list)
}
