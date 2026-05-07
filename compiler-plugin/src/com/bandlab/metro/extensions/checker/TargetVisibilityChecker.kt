package com.bandlab.metro.extensions.checker

import com.bandlab.metro.extensions.component.ContributesComponentIds
import com.bandlab.metro.extensions.configselector.ContributesConfigSelectorIds
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.declarations.utils.visibility

/**
 * A `FirDeclarationChecker` responsible for verifying the visibility requirements of certain
 * annotated classes in the module to ensure they are properly contributed.
 *
 * This checker inspects classes annotated with any of the following annotations:
 * - `@ContributesConfigSelector`
 * - `@ContributesComponent`
 * - `@ContributesInjector`
 *
 * If the class is annotated with any of these annotations, it must have public visibility.
 */
internal object TargetVisibilityChecker : FirDeclarationChecker<FirClass>(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirClass) {
        val symbol = declaration.symbol
        val session = context.session

        val hasConfigSelector =
            symbol.getAnnotationByClassId(ContributesConfigSelectorIds.contributesConfigSelector, session) != null
        val hasComponent = symbol.getAnnotationByClassId(ContributesComponentIds.contributesComponent, session) != null
        val hasInjector = symbol.getAnnotationByClassId(ContributesComponentIds.contributesInjector, session) != null
        if (!hasConfigSelector && !hasComponent && !hasInjector) return

        if (!declaration.visibility.isPublicAPI) {
            reporter.reportOn(
                declaration.source,
                MetroExtensionsDiagnostics.TARGET_MUST_BE_PUBLIC,
                "${declaration.classId.shortClassName.asString()} must be public to be contributed properly."
            )
        }
    }
}