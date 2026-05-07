package com.bandlab.metro.extensions.checker

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension

internal class MetroExtensionsFirCheckers(
    session: FirSession,
    private val includeBaselineChecker: Boolean = true,
    private val contributesInjectorBaseline: Set<String> = emptySet(),
) : FirAdditionalCheckersExtension(session) {

    override val declarationCheckers = object : DeclarationCheckers() {
        override val classCheckers = buildSet {
            add(ParamTypeClassChecker)
            add(TargetVisibilityChecker)

            if (includeBaselineChecker) {
                add(ContributesInjectorBaselineChecker(contributesInjectorBaseline))
            }
        }
    }
}