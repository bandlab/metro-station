package com.bandlab.metro.station.utils

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.name.ClassId

/**
 * Checks whether [IrClass] is a subclass of the class identified by [classId]
 * by traversing the supertype hierarchy.
 */
internal fun IrClass.isSubclassOf(classId: ClassId): Boolean {
    val queue = ArrayDeque<IrClass>()
    queue.add(this)
    val visited = mutableSetOf<IrClass>()
    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        if (!visited.add(current)) continue
        if (current.classId == classId) return true
        for (superType in current.superTypes) {
            superType.classOrNull?.owner?.let { queue.add(it) }
        }
    }
    return false
}