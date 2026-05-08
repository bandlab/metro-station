package com.bandlab.metro.extensions.component

import com.bandlab.metro.extensions.utils.asName
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

internal object ContributesComponentIds {
    private val commonAndroidDi = FqName("com.bandlab.common.android.di")

    // ContributesComponent
    val contributesComponent = ClassId(commonAndroidDi, "ContributesComponent".asName())
    val contributesComponentFqName = contributesComponent.asSingleFqName()

    // ContributesInjector
    val contributesInjector = ClassId(commonAndroidDi, "ContributesInjector".asName())
    val contributesInjectorFqName = contributesInjector.asSingleFqName()
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

    // Component base types
    val fragmentBaseType = ClassId(FqName("com.bandlab.android.common.fragment"), "CommonFragment".asName())
    val dialogFragmentBaseType = ClassId(FqName("com.bandlab.android.common.fragment"), "CommonDialogFragment".asName())
    val serviceBaseType = ClassId(FqName("android.app"), "Service".asName())
    val workerBaseType = ClassId(FqName("androidx.work"), "CoroutineWorker".asName())
    val broadcastReceiverBaseType = ClassId(FqName("android.content"), "BroadcastReceiver".asName())

    // Custom feature scopes
    val activityScope = ClassId(androidCommon, "ActivityScope".asName())
    val fragmentScope = ClassId(androidCommon, "FragmentScope".asName())
    val pageScope = ClassId(androidCommon, "PageScope".asName())
    val serviceScope = ClassId(androidCommon, "ServiceScope".asName())
    val workerScope = ClassId(androidCommon, "WorkerScope".asName())
    val broadcastReceiverScope = ClassId(androidCommon, "BroadcastReceiverScope".asName())

    val componentPredicate = LookupPredicate.create {
        annotated(contributesComponentFqName)
    }

    val injectorPredicate = LookupPredicate.create {
        annotated(contributesInjectorFqName)
    }
}