package com.bandlab.metro.station.checker

import com.bandlab.metro.station.utils.findSuperTypeRef
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.primaryConstructorIfAny
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds
import com.bandlab.metro.station.graph.MetroStationIds as Ids

/**
 * Validates that classes annotated with `@MetroStation` have the required constructor parameters:
 * - If `extraDependencies` is specified (not `Nothing::class`), there must be a constructor parameter
 *   whose type matches the specified extra dependencies class.
 */
internal object MetroStationChecker : FirDeclarationChecker<FirClass>(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirClass) {
        val symbol = declaration.symbol
        val session = context.session

        val annotation = symbol.getAnnotationByClassId(Ids.metroStation, session) ?: return
        val extraDepsClassId = resolveExtraDependenciesClassId(annotation)

        val isPage = symbol.findSuperTypeRef(Ids.page) != null ||
            symbol.findSuperTypeRef(Ids.paramPage) != null

        if (!isPage) {
            // We still use extraDependencies on Fragments for legacy code
            if (extraDepsClassId != null && symbol.findSuperTypeRef(Ids.commonFragment) == null) {
                // extraDependencies is supported on Pages only (for now)
                reporter.reportOn(
                    declaration.source,
                    MetroStationDiagnostics.EXTRA_DEPENDENCIES_UNSUPPORTED,
                    "extraDependencies feature is supported only on Pages."
                )
            }
            return
        }

        val primaryConstructor = symbol.primaryConstructorIfAny(session) ?: return
        val constructorParams = primaryConstructor.valueParameterSymbols

        // If extraDependencies is specified, there must be a matching constructor parameter
        if (extraDepsClassId != null) {
            val hasExtraDepsParam = constructorParams.any { param ->
                param.resolvedReturnType.classId == extraDepsClassId
            }
            if (!hasExtraDepsParam) {
                reporter.reportOn(
                    declaration.source,
                    MetroStationDiagnostics.MISSING_EXTRA_DEPENDENCIES_PARAMETER,
                    "${declaration.classId.shortClassName.asString()} must have a ${extraDepsClassId.shortClassName.asString()} in its constructor for the compiler to create the graph."
                )
            }
        }
    }

    /**
     * Extracts the ClassId of the extraDependencies parameter from the annotation,
     * returning null if not specified or if it's Nothing::class.
     */
    private fun resolveExtraDependenciesClassId(annotation: FirAnnotation): ClassId? {
        val rawExpr = annotation.argumentMapping.mapping[Ids.extraDependenciesName]
            ?: (annotation as? FirAnnotationCall)?.argumentList?.arguments
                ?.filterIsInstance<FirNamedArgumentExpression>()
                ?.find { it.name == Ids.extraDependenciesName }
            ?: return null

        val expr = if (rawExpr is FirNamedArgumentExpression) rawExpr.expression else rawExpr

        val getClassCall = expr as? FirGetClassCall ?: return null

        val classId = when (val argument = getClassCall.argument) {
            is FirResolvedQualifier -> argument.classId
            else -> return null
        } ?: return null

        return if (classId == StandardClassIds.Nothing) null else classId
    }
}