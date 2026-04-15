package com.bandlab.compiler.utils

import org.jetbrains.kotlin.name.Name

internal fun String.asName(): Name = Name.identifier(this)

internal infix operator fun Name.plus(other: String) = (asString() + other).asName()
internal infix operator fun Name.plus(other: Name) = (asString() + other.asString()).asName()