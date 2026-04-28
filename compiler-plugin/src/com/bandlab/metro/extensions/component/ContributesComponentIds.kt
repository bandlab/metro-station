package com.bandlab.metro.extensions.component

import com.bandlab.metro.extensions.utils.asName
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

internal object ContributesComponentIds {
    private val commonAndroidDi = FqName("com.bandlab.common.android.di")
    val contributesComponent = ClassId(commonAndroidDi, "ContributesComponent".asName())
    val contributesComponentFqName = contributesComponent.asSingleFqName()

    val appDependenciesName = "appDependencies".asName()
    val graphMarkerName = "graphMarker".asName()
    val extraDependenciesName = "extraDependencies".asName()

    val graphName = "FeatureGraph".asName()
    val nestedFactoryName = "Factory".asName()
    val featureModuleName = "FeatureModule".asName()
    val featureServiceProviderName = "FeatureServiceProvider".asName()

    val featureName = "feature".asName()
    val serviceProviderName = "serviceProvider".asName()
    val createName = "create".asName()
    val injectName = "inject".asName()
    val provideParamName = "provideParam".asName()

    // Common classes
    private val androidCommon = FqName("com.bandlab.android.common")
    val membersInjectorProvider = ClassId(commonAndroidDi, "MembersInjectorProvider".asName())
    val commonActivity = ClassId(androidCommon, "CommonActivity".asName())
    val commonActivityServiceProvider = commonActivity.createNestedClassId("ServiceProvider".asName())
    val defaultScreenServiceProvider = ClassId(androidCommon, "DefaultScreenServiceProvider".asName())

    // Custom feature scopes
    val activityScope = ClassId(androidCommon, "ActivityScope".asName())
    val fragmentScope = ClassId(androidCommon, "FragmentScope".asName())
    val pageScope = ClassId(androidCommon, "PageScope".asName())
    val serviceScope = ClassId(androidCommon, "ServiceScope".asName())
    val workerScope = ClassId(androidCommon, "WorkerScope".asName())
    val broadcastReceiverScope = ClassId(androidCommon, "BroadcastReceiverScope".asName())

    val predicate = LookupPredicate.create {
        annotated(contributesComponentFqName)
    }
}