package com.bandlab.metro.station

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

object Symbols {
    object StringNames {
        const val METRO_RUNTIME_PACKAGE = "dev.zacsweers.metro"
        const val METRO_STATION_RUNTIME_PACKAGE = "com.bandlab.metro.station"
    }

    object FqNames {
        val metroRuntimePackage = FqName(StringNames.METRO_RUNTIME_PACKAGE)
        val metroStationRuntimePackage = FqName(StringNames.METRO_STATION_RUNTIME_PACKAGE)
    }

    object ClassIds {
        val metroStation = ClassId(FqNames.metroStationRuntimePackage, "MetroStation".asName())
        val stationEntry = ClassId(FqNames.metroStationRuntimePackage, "StationEntry".asName())

        val dependencyGraph = ClassId(FqNames.metroRuntimePackage, "DependencyGraph".asName())
        val dependencyGraphFactory = dependencyGraph.createNestedClassId(Names.FactoryClass)
        val graphExtension = ClassId(FqNames.metroRuntimePackage, "GraphExtension".asName())
        val graphExtensionFactory = graphExtension.createNestedClassId(Names.FactoryClass)
        val contributesTo = ClassId(FqNames.metroRuntimePackage, "ContributesTo".asName())
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

        val InjectMethod = "inject".asName()
    }
}