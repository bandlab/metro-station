package com.bandlab.metro.extensions.checker

import com.bandlab.metro.extensions.utils.findSuperTypeRef
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.primaryConstructorIfAny
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.expressions.FirNamedArgumentExpression
import org.jetbrains.kotlin.fir.expressions.FirResolvedQualifier
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds
import com.bandlab.metro.extensions.component.ContributesComponentIds as Ids

/**
 * Validates that classes annotated with `@ContributesComponent` have the required constructor parameters:
 * - Pages must have a `Context` or `ComponentActivity` parameter in their primary constructor.
 * - If `extraDependencies` is specified (not `Nothing::class`), there must be a constructor parameter
 *   whose type matches the specified extra dependencies class.
 */
internal object ContributesComponentChecker : FirDeclarationChecker<FirClass>(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirClass) {
        val symbol = declaration.symbol
        val session = context.session

        val annotation = symbol.getAnnotationByClassId(Ids.contributesComponent, session) ?: return

        val isPage = symbol.findSuperTypeRef(Ids.page) != null ||
            symbol.findSuperTypeRef(Ids.paramPage) != null
        if (!isPage) return

        val primaryConstructor = symbol.primaryConstructorIfAny(session) ?: return
        val constructorParams = primaryConstructor.valueParameterSymbols

        // Pages must have a Context or ComponentActivity constructor parameter
        val hasContextParam = constructorParams.any { param ->
            val paramClassId = param.resolvedReturnType.classId
            paramClassId == Ids.context || paramClassId == Ids.componentActivity
        }
        if (!hasContextParam) {
            reporter.reportOn(
                declaration.source,
                MetroExtensionsDiagnostics.MISSING_CONTEXT_PARAMETER,
                "${declaration.classId.shortClassName.asString()} must have a Context or ComponentActivity parameter in its constructor for the compiler to create the graph."
            )
        }

        // If extraDependencies is specified, there must be a matching constructor parameter
        val extraDepsClassId = resolveExtraDependenciesClassId(annotation)
        if (extraDepsClassId != null) {
            val hasExtraDepsParam = constructorParams.any { param ->
                param.resolvedReturnType.classId == extraDepsClassId
            }
            if (!hasExtraDepsParam) {
                reporter.reportOn(
                    declaration.source,
                    MetroExtensionsDiagnostics.MISSING_EXTRA_DEPENDENCIES_PARAMETER,
                    "${declaration.classId.shortClassName.asString()} must have a ${extraDepsClassId.shortClassName.asString()} in its constructor for the compiler to create the graph."
                )
            }
        }
    }

    /**
     * Extracts the ClassId of the extraDependencies parameter from the annotation,
     * returning null if not specified or if it's Nothing::class.
     */
    private fun resolveExtraDependenciesClassId(annotation: org.jetbrains.kotlin.fir.expressions.FirAnnotation): ClassId? {
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