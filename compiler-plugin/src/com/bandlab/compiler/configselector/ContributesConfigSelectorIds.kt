package com.bandlab.compiler.configselector

import com.bandlab.compiler.utils.asName
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

internal object ContributesConfigSelectorIds {
    val contributesConfigSelectorFqName = FqName("com.bandlab.config.api.ContributesConfigSelector")
    val debuggableConfigSelectorClassId = ClassId(
        FqName("com.bandlab.config.api"),
        "DebuggableConfigSelector".asName()
    )

    val nestedContributionName = "MultibindingContribution".asName()

    val predicate = LookupPredicate.create {
        annotated(contributesConfigSelectorFqName)
    }
}