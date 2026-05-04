package com.bandlab.metro.extensions.configselector

import com.bandlab.metro.extensions.utils.asName
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

internal object ContributesConfigSelectorIds {
    val contributesConfigSelector =
        ClassId(FqName("com.bandlab.config.api"), "ContributesConfigSelector".asName())
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