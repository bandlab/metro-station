package com.bandlab.metro.station.configselector

import com.bandlab.metro.station.utils.ClassIds
import com.bandlab.metro.station.utils.buildSimpleAnnotation
import com.bandlab.metro.station.utils.buildSimpleAnnotationCall
import com.bandlab.metro.station.utils.getClassCall
import com.fueledbycaffeine.autoservice.AutoService
import dev.zacsweers.metro.compiler.MetroOptions
import dev.zacsweers.metro.compiler.api.fir.MetroFirDeclarationGenerationExtension
import dev.zacsweers.metro.compiler.compat.CompatContext
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.builder.buildRegularClass
import org.jetbrains.kotlin.fir.declarations.builder.buildValueParameter
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.NestedClassGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.scopes.kotlinScopeProvider
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import com.bandlab.metro.station.configselector.ContributesConfigSelectorIds as Ids

/**
 * This FIR declaration generator generates a multibinding contribution for config selectors that are annotated with
 * [Ids.contributesConfigSelectorFqName].
 */
public class ContributesConfigSelectorFir(session: FirSession, compatContext: CompatContext) :
    MetroFirDeclarationGenerationExtension(session), CompatContext by compatContext {

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(Ids.predicate)
    }

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext,
    ): Set<Name> {
        return if (session.predicateBasedProvider.matches(Ids.predicate, classSymbol)) {
            setOf(Ids.nestedContributionName)
        } else {
            emptySet()
        }
    }

    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext,
    ): FirClassLikeSymbol<*>? {
        if (name != Ids.nestedContributionName) return null
        if (!session.predicateBasedProvider.matches(Ids.predicate, owner)) {
            return null
        }

        val nestedClassId = owner.classId.createNestedClassId(name)
        val classSymbol = FirRegularClassSymbol(nestedClassId)

        // Build the @Binds function and add it directly to the class declarations.
        // This makes it visible to Metro's getNestedClassifiersNames (which checks for @Binds
        // functions to decide whether to generate BindsMirror).
        val functionSymbol = FirNamedFunctionSymbol(CallableId(nestedClassId, Ids.bindName))
        val bindsFunction = buildMemberFunction(
            owner = classSymbol,
            returnTypeProvider = { Ids.debuggableConfigSelectorClassId.constructClassLikeType() },
            callableId = functionSymbol.callableId,
            origin = Key.origin,
            visibility = Visibilities.Public,
            modality = Modality.ABSTRACT
        ) {
            valueParameters += buildValueParameter {
                resolvePhase = FirResolvePhase.BODY_RESOLVE
                moduleData = session.moduleData
                origin = Key.origin
                returnTypeRef = owner.defaultType().toFirResolvedTypeRef()
                this.name = Ids.implName
                symbol = FirValueParameterSymbol()
                containingDeclarationSymbol = this@buildMemberFunction.symbol
            }
        }
        bindsFunction.replaceAnnotations(
            listOf(
                buildSimpleAnnotationCall(session, ClassIds.binds, functionSymbol),
                buildSimpleAnnotationCall(session, ClassIds.intoSet, functionSymbol),
            )
        )

        val contribution = buildRegularClass {
            resolvePhase = FirResolvePhase.BODY_RESOLVE
            moduleData = session.moduleData
            origin = Key.origin
            source = owner.source
            classKind = ClassKind.INTERFACE
            scopeProvider = session.kotlinScopeProvider
            this.name = nestedClassId.shortClassName
            symbol = classSymbol
            status = FirResolvedDeclarationStatusImpl(
                Visibilities.Public,
                Modality.ABSTRACT,
                Visibilities.Public.toEffectiveVisibility(owner, forClass = true),
            )
            superTypeRefs += session.builtinTypes.anyType
            val appScopeSymbol = session.symbolProvider.getClassLikeSymbolByClassId(ClassIds.appScope)!!
            annotations += buildSimpleAnnotation(
                classId = ClassIds.contributesTo,
                argumentMapping = buildAnnotationArgumentMapping {
                    mapping[ClassIds.scopeName] = appScopeSymbol.getClassCall()
                }
            )
            // Add the function directly to the class declarations
            declarations += bindsFunction
        }
        return contribution.symbol
    }

    override fun getContributionHints(): List<ContributionHint> {
        return session.predicateBasedProvider
            .getSymbolsByPredicate(Ids.predicate)
            .filterIsInstance<FirRegularClassSymbol>()
            .map { classSymbol ->
                val nestedInterfaceClassId = classSymbol.classId
                    .createNestedClassId(Ids.nestedContributionName)
                ContributionHint(contributingClassId = nestedInterfaceClassId, scope = ClassIds.appScope)
            }
    }

    private object Key : GeneratedDeclarationKey()

    @AutoService
    public class Factory : MetroFirDeclarationGenerationExtension.Factory {
        override fun create(
            session: FirSession,
            options: MetroOptions,
            compatContext: CompatContext,
        ): MetroFirDeclarationGenerationExtension = ContributesConfigSelectorFir(session, compatContext)
    }
}