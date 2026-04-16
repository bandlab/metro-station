package com.bandlab.metro.extensions.utils

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

internal object ClassIds {
    private val metroPackageFqName = FqName("dev.zacsweers.metro")

    val contributesTo = ClassId(metroPackageFqName, "ContributesTo".asName())
    val appScope = ClassId(metroPackageFqName, "AppScope".asName())
    val binds = ClassId(metroPackageFqName, "Binds".asName())
    val intoSet = ClassId(metroPackageFqName, "IntoSet".asName())

    val scopeName = "scope".asName()
}