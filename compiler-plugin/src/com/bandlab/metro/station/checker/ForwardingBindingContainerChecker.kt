// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station.checker

import com.bandlab.metro.station.utils.ClassIds
import com.bandlab.metro.station.utils.findArgument
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.processAllDeclaredCallables
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.expressions.FirCall
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirNamedArgumentExpression
import org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol

/**
 * Reports `@BindingContainer` declarations that only forward other containers through `includes`
 * and declare no members of their own (neither in the class body nor in its companion object). Such
 * containers should be replaced by listing the included containers directly in
 * `@MetroStation(bindingContainers = ...)` or `@DependencyGraph(bindingContainers = ...)`.
 */
internal object ForwardingBindingContainerChecker :
    FirDeclarationChecker<FirClass>(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirClass) {
        val session = context.session
        val symbol = declaration.symbol

        val annotation = symbol.getAnnotationByClassId(ClassIds.bindingContainer, session) ?: return
        if (annotation.includesElements().isEmpty()) return

        if (symbol.hasDeclaredCallables(session)) return
        val companion = (declaration as? FirRegularClass)?.companionObjectSymbol
        if (companion != null && companion.hasDeclaredCallables(session)) return

        reporter.reportOn(declaration.source, MetroStationDiagnostics.FORWARDING_BINDING_CONTAINER)
    }

    /** Returns the element expressions of the `includes` array argument, or empty if absent. */
    private fun FirAnnotation.includesElements(): List<FirExpression> {
        val rawExpr =
            argumentMapping.mapping[ClassIds.includesName]
                ?: (this as? FirAnnotationCall)?.findArgument(ClassIds.includesName)
                ?: return emptyList()
        val expr = if (rawExpr is FirNamedArgumentExpression) rawExpr.expression else rawExpr
        return when (expr) {
            is FirVarargArgumentsExpression -> expr.arguments
            is FirCall -> expr.arguments
            else -> listOf(expr)
        }
    }

    /** True if the class directly declares at least one function or property. */
    private fun FirClassSymbol<*>.hasDeclaredCallables(session: FirSession): Boolean {
        var found = false
        processAllDeclaredCallables(session) { found = true }
        return found
    }
}
