package com.bandlab.metro.extensions.utils

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.ConeClassLikeLookupTagImpl
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.types.ConeTypeProjection
import org.jetbrains.kotlin.fir.types.FirTypeProjectionWithVariance
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.FirUserTypeRef
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

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
        ?: (annotation as? FirAnnotationCall)?.argumentList?.arguments?.let { args ->
            // First try named argument lookup
            args.filterIsInstance<FirNamedArgumentExpression>()
                .find { it.name == ClassIds.scopeName }
            // Then try positional: first argument that is not a named argument (positional scope)
                ?: args.firstOrNull { it !is FirNamedArgumentExpression }
        }
        ?: return defaultScope

    val expr = if (rawExpr is FirNamedArgumentExpression) rawExpr.expression else rawExpr

    val getClassCall = expr as? FirGetClassCall
        ?: return defaultScope

    return when (val argument = getClassCall.argument) {
        is FirResolvedQualifier -> argument.classId ?: defaultScope
        else -> {
            // Try to resolve via extractClassId (handles same-package lookups)
            argument.extractClassId(owner.classId, session)
                ?: resolveClassIdFromUserTypeRef(getClassCall, owner, annotationClassId, session)
                ?: error(
                    "Cannot resolve scope class for ${owner.classId}. " +
                        "The scope class may be in an external module that is not yet resolved. " +
                        "Ensure the scope class is available on the compilation classpath.",
                )
        }
    }
}

/**
 * Resolves the first type argument (ViewModel type) from a Page/ParamPage supertype.
 * Falls back to session-based symbol resolution if [unwrapType] returns null due to unresolved type refs.
 */
internal fun resolvePageViewModelType(
    superTypeRef: FirTypeRef,
    owner: FirClassSymbol<*>,
    session: FirSession,
): ConeTypeProjection {
    return superTypeRef.unwrapType()
        ?: resolveUnresolvedTypeArg(superTypeRef, owner, session)
        ?: error("Cannot resolve Page type argument for ${owner.classId}")
}

/**
 * Resolves a type argument from an unresolved [FirUserTypeRef] by looking up the type name
 * via the session's symbol provider using the owner's package as context.
 */
private fun resolveUnresolvedTypeArg(
    typeRef: FirTypeRef?,
    owner: FirClassSymbol<*>,
    session: FirSession,
): ConeTypeProjection? {
    if (typeRef == null) return null
    if (typeRef !is FirUserTypeRef) return null
    val typeArg = typeRef.qualifier.lastOrNull()?.typeArgumentList?.typeArguments?.firstOrNull()
    if (typeArg !is FirTypeProjectionWithVariance) return null
    val innerRef = typeArg.typeRef
    if (innerRef !is FirUserTypeRef) return null

    val name = innerRef.qualifier.lastOrNull()?.name?.asString() ?: return null
    val packageFqName = owner.classId.packageFqName

    // Try as a top-level class in the same package
    val classId = ClassId(packageFqName, Name.identifier(name))
    val symbol = session.symbolProvider.getClassLikeSymbolByClassId(classId)
    if (symbol != null) {
        return ConeClassLikeTypeImpl(
            ConeClassLikeLookupTagImpl(classId),
            emptyArray(),
            isMarkedNullable = false,
        )
    }
    return null
}

/**
 * Resolves a [ClassId] from the [FirGetClassCall]'s argument when it is an unresolved
 * [FirPropertyAccessExpression] (external module scope classes that show a `#` suffix).
 *
 * Since the expression only contains the simple name (no package info), we resolve it by:
 * 1. Checking the owner's package (already handled by extractClassId)
 * 2. Checking the annotation class's package as a hint
 * 3. Walking the owner's source file imports to find a matching import
 */
@OptIn(SymbolInternals::class)
private fun resolveClassIdFromUserTypeRef(
    getClassCall: FirGetClassCall,
    owner: FirClassSymbol<*>,
    annotationClassId: ClassId,
    session: FirSession,
): ClassId? {
    val argument = getClassCall.argument
    if (argument !is FirPropertyAccessExpression) return null

    val simpleName = argument.calleeReference.name.asString()

    // Try the annotation's package (scope classes often live near the annotation)
    val annotationPackage = annotationClassId.packageFqName
    val candidateInAnnotationPkg = ClassId(annotationPackage, Name.identifier(simpleName))
    if (session.symbolProvider.getClassLikeSymbolByClassId(candidateInAnnotationPkg) != null) {
        return candidateInAnnotationPkg
    }

    // Try parent packages of the annotation package
    var pkg = annotationPackage
    while (!pkg.isRoot) {
        pkg = pkg.parent()
        val candidate = ClassId(pkg, Name.identifier(simpleName))
        if (session.symbolProvider.getClassLikeSymbolByClassId(candidate) != null) {
            return candidate
        }
    }

    // Try to find via the owner's containing file imports
    val containingFile = findContainingFile(owner, session)
    if (containingFile != null) {
        for (import in containingFile.imports) {
            val importedFqName = import.importedFqName ?: continue
            if (import.isAllUnder) {
                // Star import: try package + simpleName
                val candidate = ClassId(importedFqName, Name.identifier(simpleName))
                if (session.symbolProvider.getClassLikeSymbolByClassId(candidate) != null) {
                    return candidate
                }
            } else {
                // Explicit import: check if the last segment matches
                if (importedFqName.shortName().asString() == simpleName) {
                    val candidate = ClassId(importedFqName.parent(), Name.identifier(simpleName))
                    if (session.symbolProvider.getClassLikeSymbolByClassId(candidate) != null) {
                        return candidate
                    }
                }
            }
        }
    }

    return null
}

/**
 * Attempts to find the [FirFile] containing the given [owner].
 */
private fun findContainingFile(owner: FirClassSymbol<*>, session: FirSession): FirFile? {
    return try {
        session.firProvider.getFirClassifierContainerFileIfAny(owner.classId)
    } catch (_: Exception) {
        null
    }
}