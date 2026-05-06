package com.bandlab.metro.extensions.injector

import com.bandlab.metro.extensions.utils.ClassIds
import com.bandlab.metro.extensions.utils.extractClassId
import com.bandlab.metro.extensions.utils.getClassCall
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.name.ClassId

/**
 * Resolves a scope [ClassId] from an annotation's `scope` argument.
 * Returns [defaultScope] if the annotation is missing or the scope argument is not specified.
 */
internal fun resolveScopeClassIdFromAnnotation(
    owner: FirClassSymbol<*>,
    annotationClassId: ClassId,
    session: FirSession,
    defaultScope: ClassId,
): ClassId {
    val annotation = owner.getAnnotationByClassId(annotationClassId, session)
        ?: return defaultScope

    val rawExpr = annotation.argumentMapping.mapping[ClassIds.scopeName]
        ?: (annotation as? FirAnnotationCall)?.argumentList?.arguments
            ?.filterIsInstance<FirNamedArgumentExpression>()
            ?.find { it.name == ClassIds.scopeName }
        ?: return defaultScope

    val expr = if (rawExpr is FirNamedArgumentExpression) rawExpr.expression else rawExpr

    val getClassCall = expr as? FirGetClassCall
        ?: return defaultScope

    return when (val argument = getClassCall.argument) {
        is FirResolvedQualifier -> argument.classId ?: defaultScope
        else -> argument.extractClassId(owner.classId, session) ?: defaultScope
    }
}

/**
 * Resolves a scope expression from an annotation's `scope` argument.
 * Returns a `GetClassCall` for [defaultScope] if the annotation is missing or scope is not specified.
 */
internal fun resolveScopeExprFromAnnotation(
    owner: FirClassSymbol<*>,
    annotationClassId: ClassId,
    session: FirSession,
    defaultScope: ClassId,
): FirExpression {
    val defaultExpr = { session.symbolProvider.getClassLikeSymbolByClassId(defaultScope)!!.getClassCall() }

    val annotation = owner.getAnnotationByClassId(annotationClassId, session)
        ?: return defaultExpr()

    val rawExpr = annotation.argumentMapping.mapping[ClassIds.scopeName]
        ?: (annotation as? FirAnnotationCall)?.argumentList?.arguments
            ?.filterIsInstance<FirNamedArgumentExpression>()
            ?.find { it.name == ClassIds.scopeName }
        ?: return defaultExpr()

    val expr = if (rawExpr is FirNamedArgumentExpression) rawExpr.expression else rawExpr

    val getClassCall = expr as? FirGetClassCall
        ?: return defaultExpr()

    val classId = when (val argument = getClassCall.argument) {
        is FirResolvedQualifier -> argument.classId
        else -> argument.extractClassId(owner.classId, session)
    } ?: return defaultExpr()

    val symbol = session.symbolProvider.getClassLikeSymbolByClassId(classId)
        ?: return defaultExpr()

    return symbol.getClassCall()
}
