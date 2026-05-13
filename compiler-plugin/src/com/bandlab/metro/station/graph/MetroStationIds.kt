package com.bandlab.metro.station.graph

import com.bandlab.metro.station.utils.asName
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

internal object MetroStationIds {
    private val metroStationPackage = FqName("com.bandlab.metro.station")
    private val commonAndroidDi = FqName("com.bandlab.common.android.di")

    // MetroStation
    val metroStation = ClassId(metroStationPackage, "MetroStation".asName())
    val metroStationFqName = metroStation.asSingleFqName()

    // StationEntry
    val stationEntry = ClassId(metroStationPackage, "StationEntry".asName())
    val stationEntryFqName = stationEntry.asSingleFqName()
    val parentScopeName = "parentScope".asName()
    val featureExtensionName = "FeatureExtension".asName()
    val featureBindingsName = "FeatureBindings".asName()
    val extensionFactoryContributionName = "ExtensionFactoryContribution".asName()
    val createName = "create".asName()
    val bindName = "bind".asName()
    val factoryParamName = "factory".asName()
    val provideFactoryName = "provideFactory".asName()

    val appDependenciesName = "appDependencies".asName()
    val graphMarkerName = "graphMarker".asName()
    val extraDependenciesName = "extraDependencies".asName()

    val graphName = "FeatureGraph".asName()
    val nestedFactoryName = "Factory".asName()
    val featureModuleName = "FeatureModule".asName()
    val featureServiceProviderName = "FeatureServiceProvider".asName()

    val featureName = "feature".asName()
    val serviceProviderName = "serviceProvider".asName()
    val provideBaseTypeName = "provideBaseType".asName()
    val provideParamName = "provideParam".asName()
    val provideParamFlowName = "provideParamFlow".asName()

    // Common classes
    private val androidCommon = FqName("com.bandlab.android.common")
    private val androidCommonActivity = FqName("com.bandlab.android.common.activity")
    val membersInjectorProvider = ClassId(commonAndroidDi, "MembersInjectorProvider".asName())
    val commonActivity = ClassId(androidCommonActivity, "CommonActivity".asName())
    val commonActivityServiceProvider = commonActivity.createNestedClassId("ServiceProvider".asName())
    val defaultScreenServiceProvider = ClassId(androidCommon, "DefaultScreenServiceProvider".asName())
    val defaultActivityDeps = ClassId(androidCommonActivity, "DefaultActivityDependencies".asName())
    val fragment = ClassId(FqName("androidx.fragment.app"), "Fragment".asName())
    val commonFragment = ClassId(FqName("com.bandlab.android.common.fragment"), "CommonFragment".asName())
    val commonDialogFragment = ClassId(FqName("com.bandlab.android.common.fragment"), "CommonDialogFragment".asName())
    val defaultFragmentDeps =
        ClassId(FqName("com.bandlab.android.common.fragment"), "DefaultFragmentDependencies".asName())
    val graphFactory = ClassId(commonAndroidDi, "GraphFactory".asName())
    val emptyExtraDependencies = ClassId(commonAndroidDi, "EmptyExtraDependencies".asName())

    // Page
    private val commonPageDi = FqName("com.bandlab.common.android.pager.screen.di")
    val page = ClassId(FqName("com.bandlab.uikit.api.page"), "Page".asName())
    val pageInjector = ClassId(commonPageDi, "PageInjector".asName())
    val pageGraphFactory = ClassId(commonPageDi, "PageGraphFactory".asName())
    val pageGraphDependencies = ClassId(commonPageDi, "PageGraphDependencies".asName())
    val pageGraphDependenciesModule = ClassId(commonPageDi, "PageGraphDependenciesModule".asName())
    val navPageDependencies = ClassId(commonPageDi, "NavPageDependencies".asName())
    val defaultPageDependencies = ClassId(commonPageDi, "DefaultPageDependencies".asName())

    // ParamPage
    private val commonPage = FqName("com.bandlab.common.android.pager.screen")
    val paramPage = ClassId(commonPage, "ParamPage".asName())
    val pageParamFlowProvider = ClassId(commonPage, "PageParamFlowProvider".asName())
    val coroutinesFlow = ClassId(FqName("kotlinx.coroutines.flow"), "Flow".asName())
    val initialParamName = "initialParam".asName()

    // Custom feature scopes
    val activityScope = ClassId(androidCommon, "ActivityScope".asName())
    val fragmentScope = ClassId(androidCommon, "FragmentScope".asName())
    val pageScope = ClassId(androidCommon, "PageScope".asName())

    // Graph resolution and supertypes
    val hasServiceProvider = ClassId(commonAndroidDi, "HasServiceProvider".asName())
    val resolveName = "resolve".asName()
    val resolveFromName = "resolveFrom".asName()
    val graphPropertyName = "graph".asName()
    val graphCreatorPropertyName = "graphCreator".asName()
    val pageGraphCreator = ClassId(commonPageDi, "PageGraphCreator".asName())
    val pageGraphCreatorExtension = ClassId(commonPageDi, "graphCreator".asName())
    val resolveServiceProvider = ClassId(commonAndroidDi, "resolveServiceProvider".asName())
    val context = ClassId(FqName("android.content"), "Context".asName())
    val componentActivity = ClassId(FqName("androidx.activity"), "ComponentActivity".asName())

    val metroStationPredicate = LookupPredicate.create {
        annotated(metroStationFqName)
    }

    val stationEntryPredicate = LookupPredicate.create {
        annotated(stationEntryFqName)
    }
}