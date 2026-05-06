package com.bandlab.metro.extensions.utils

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

internal object ClassIds {
    private val metroPackageFqName = FqName("dev.zacsweers.metro")
    private val metroInternalPackageFqName = FqName("dev.zacsweers.metro.internal")

    val contributesTo = ClassId(metroPackageFqName, "ContributesTo".asName())
    val appScope = ClassId(metroPackageFqName, "AppScope".asName())
    val binds = ClassId(metroPackageFqName, "Binds".asName())
    val intoSet = ClassId(metroPackageFqName, "IntoSet".asName())
    val dependencyGraph = ClassId(metroPackageFqName, "DependencyGraph".asName())
    val dependencyGraphFactory = dependencyGraph.createNestedClassId("Factory".asName())
    val graphExtension = ClassId(metroPackageFqName, "GraphExtension".asName())
    val graphExtensionFactory = graphExtension.createNestedClassId("Factory".asName())
    val provides = ClassId(metroPackageFqName, "Provides".asName())
    val includes = ClassId(metroPackageFqName, "Includes".asName())
    val intoMap = ClassId(metroPackageFqName, "IntoMap".asName())
    val classKey = ClassId(metroPackageFqName, "ClassKey".asName())
    val bindingContainer = ClassId(metroPackageFqName, "BindingContainer".asName())

    val irOnlyFactories = ClassId(metroInternalPackageFqName, "IROnlyFactories".asName())

    val scopeName = "scope".asName()
    val bindingContainersName = "bindingContainers".asName()
    val valueName = "value".asName()
}