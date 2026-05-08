package com.bandlab.metro.extensions.checker

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import com.bandlab.metro.extensions.component.ContributesComponentIds as Ids

internal class ContributesInjectorBaselineChecker(
    private val baseline: Set<String>,
) : FirDeclarationChecker<FirClass>(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirClass) {
        val symbol = declaration.symbol
        val session = context.session

        // Only check classes annotated @ContributesInjector
        val contributesInjectorAnnotation = symbol.getAnnotationByClassId(Ids.contributesInjector, session) ?: return

        val classFqName = declaration.symbol.classId.asSingleFqName().asString()
        if (classFqName !in baseline) {
            reporter.reportOn(
                source = contributesInjectorAnnotation.source,
                factory = MetroExtensionsDiagnostics.DEPRECATED_CONTRIBUTES_INJECTOR,
                context = context
            )
        }
    }
}