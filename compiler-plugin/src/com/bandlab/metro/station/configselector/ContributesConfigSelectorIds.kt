package com.bandlab.metro.station.configselector

import com.bandlab.metro.station.utils.asName
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

internal object ContributesConfigSelectorIds {
    val contributesConfigSelector = ClassId(
        FqName("com.bandlab.metro.station"),
        "ContributesConfigSelector".asName()
    )
    val contributesConfigSelectorFqName = contributesConfigSelector.asSingleFqName()
    val debuggableConfigSelectorClassId = ClassId(
        FqName("com.bandlab.config.api"),
        "DebuggableConfigSelector".asName()
    )

    val nestedContributionName = "MultibindingContribution".asName()
    val implName = "impl".asName()
    val bindName = "bind".asName()

    val predicate = LookupPredicate.create {
        annotated(contributesConfigSelectorFqName)
    }
}