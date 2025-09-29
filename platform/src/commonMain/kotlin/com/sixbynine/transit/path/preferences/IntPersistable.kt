package com.desaiwang.transit.path.preferences

import kotlin.enums.EnumEntries
import kotlin.enums.enumEntries

interface IntPersistable {
    val number: Int

    companion object {
        fun <E> fromPersistence(
            number: Int,
            entries: EnumEntries<E>
        ): E? where E : Enum<E>, E : IntPersistable {
            return entries.find { it.number == number }
        }

        inline fun <reified E> fromPersistence(
            number: Int
        ): E? where E : Enum<E>, E : IntPersistable {
            return fromPersistence(number, enumEntries())
        }

        inline fun <reified E> createBitmask(
            values: Collection<E>
        ): Int where E : Enum<E>, E : IntPersistable {
            return values.fold(0) { acc, e -> acc or (1 shl e.number) }
        }

        inline fun <reified E> fromBitmask(
            mask: Int
        ): Set<E> where E : Enum<E>, E : IntPersistable {
            return enumEntries<E>().filter { (1 shl it.number) and mask != 0 }.toSet()
        }
    }
}

