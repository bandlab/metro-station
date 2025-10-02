package com.bandlab.metro.station

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

internal object Symbols {
    object StringNames {
        const val METRO_RUNTIME_PACKAGE = "dev.zacsweers.metro"
        const val METRO_STATION_RUNTIME_PACKAGE = "com.bandlab.metro.station"
    }

    object FqNames {
        val metroRuntimePackage = FqName(StringNames.METRO_RUNTIME_PACKAGE)
        val metroStationRuntimePackage = FqName(StringNames.METRO_STATION_RUNTIME_PACKAGE)
    }

    object ClassIds {
        val MetroStation = ClassId(FqNames.metroStationRuntimePackage, "MetroStation".asName())
        val StationEntry = ClassId(FqNames.metroStationRuntimePackage, "StationEntry".asName())

        val DependencyGraph = ClassId(FqNames.metroRuntimePackage, "DependencyGraph".asName())
        val DependencyGraphFactory = DependencyGraph.createNestedClassId(Names.FactoryClass)
        val GraphExtension = ClassId(FqNames.metroRuntimePackage, "GraphExtension".asName())
        val GraphExtensionFactory = GraphExtension.createNestedClassId(Names.FactoryClass)
        val ContributesTo = ClassId(FqNames.metroRuntimePackage, "ContributesTo".asName())
        val Provides = ClassId(FqNames.metroRuntimePackage, "Provides".asName())
    }

    object Names {
        val GraphExtensionClass = "GraphExtension".asName()
        val FactoryClass = "Factory".asName()

        val ParentScopeParam = "parentScope".asName()
        val ScopeParam = "scope".asName()
        val AdditionalScopesParam = "additionalScopes".asName()
        val ExcludesParam = "excludes".asName()
        val BindingContainersParam = "bindingContainers".asName()
        val TargetParam = "target".asName()

        val CreateMethod = "create".asName()
        val InjectMethod = "inject".asName()
    }
}