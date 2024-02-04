package com.sixbynine.transit.path.preferences

import kotlin.enums.enumEntries

interface IntPersistable {
    val number: Int

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        inline fun <reified E> fromPersistence(
            number: Int
        ): E? where E : Enum<E>, E : IntPersistable {
            return enumEntries<E>().find { it.number == number }
        }
    }
}

