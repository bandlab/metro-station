package com.bandlab.metro.extensions.component

import com.bandlab.metro.extensions.utils.findSuperTypeRef
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.extensions.FirSupertypeGenerationExtension
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import com.bandlab.metro.extensions.component.ContributesComponentIds as Ids

/**
 * If a feature is annotated with @ContributesComponent, we extend `HasServiceProvider` for Activities and Pages.
 */
public class ContributesComponentSupertypeGenerator(session: FirSession) : FirSupertypeGenerationExtension(session) {

    override fun needTransformSupertypes(declaration: FirClassLikeDeclaration): Boolean {
        return declaration.getAnnotationByClassId(Ids.contributesComponent, session) != null
    }

    override fun computeAdditionalSupertypes(
        classLikeDeclaration: FirClassLikeDeclaration,
        resolvedSupertypes: List<FirResolvedTypeRef>,
        typeResolver: TypeResolveService
    ): List<ConeKotlinType> {
        val symbol = classLikeDeclaration.symbol as? FirClassSymbol<*> ?: return emptyList()
        return if (
            symbol.findSuperTypeRef(Ids.commonActivity) != null ||
            symbol.findSuperTypeRef(Ids.paramPage) != null ||
            symbol.findSuperTypeRef(Ids.page) != null
        ) {
            // Skip if the class already extends HasServiceProvider
            if (symbol.findSuperTypeRef(Ids.hasServiceProvider) == null) {
                listOf(Ids.hasServiceProvider.constructClassLikeType())
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
}