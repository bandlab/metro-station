package com.bandlab.metro.station

import org.jetbrains.kotlin.name.Name

internal fun <T, R> Iterable<T>.mapToSet(transform: (T) -> R): Set<R> {
    return mapTo(mutableSetOf(), transform)
}

internal fun <T, R> Sequence<T>.mapToSet(transform: (T) -> R): Set<R> {
    return mapTo(mutableSetOf(), transform)
}

internal fun String.asName(): Name = Name.identifier(this)

internal infix operator fun Name.plus(other: String) = (asString() + other).asName()
internal infix operator fun Name.plus(other: Name) = (asString() + other.asString()).asName()