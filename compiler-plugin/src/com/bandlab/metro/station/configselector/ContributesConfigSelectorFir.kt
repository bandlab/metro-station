// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station.configselector

import com.bandlab.metro.station.configselector.ContributesConfigSelectorIds as Ids
import com.bandlab.metro.station.utils.ClassIds
import com.bandlab.metro.station.utils.buildSimpleAnnotation
import com.bandlab.metro.station.utils.buildSimpleAnnotationCall
import com.bandlab.metro.station.utils.getClassCall
import dev.zacsweers.metro.compiler.MetroOptions
import dev.zacsweers.metro.compiler.api.fir.MetroContributionHintExtension
import dev.zacsweers.metro.compiler.api.fir.MetroContributionHintExtension.ContributionHint
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
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
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

/**
 * This FIR declaration generator generates a multibinding contribution for config selectors that
 * are annotated with [Ids.contributesConfigSelector].
 */
public class ContributesConfigSelectorFir(session: FirSession, compatContext: CompatContext) :
    MetroFirDeclarationGenerationExtension(session),
    MetroContributionHintExtension,
    CompatContext by compatContext {

    private val annotatedClasses by lazy {
        session.predicateBasedProvider
            .getSymbolsByPredicate(Ids.predicate)
            .filterIsInstance<FirRegularClassSymbol>()
            .toList()
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(Ids.predicate)
    }

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext,
    ): Set<Name> {
        return if (classSymbol in annotatedClasses) {
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
        if (owner !in annotatedClasses) {
            return null
        }

        val nestedClassId = owner.classId.createNestedClassId(name)
        val classSymbol = FirRegularClassSymbol(nestedClassId)

        val contribution = buildRegularClass {
            resolvePhase = FirResolvePhase.BODY_RESOLVE
            moduleData = session.moduleData
            origin = Key.origin
            source = owner.source
            classKind = ClassKind.INTERFACE
            scopeProvider = session.kotlinScopeProvider
            this.name = nestedClassId.shortClassName
            symbol = classSymbol
            status =
                FirResolvedDeclarationStatusImpl(
                    Visibilities.Public,
                    Modality.ABSTRACT,
                    Visibilities.Public.toEffectiveVisibility(owner, forClass = true),
                )
            superTypeRefs += session.builtinTypes.anyType
            val appScopeSymbol =
                session.symbolProvider.getClassLikeSymbolByClassId(ClassIds.appScope)!!
            annotations +=
                buildSimpleAnnotation(
                    classId = ClassIds.contributesTo,
                    argumentMapping =
                        buildAnnotationArgumentMapping {
                            mapping[ClassIds.scopeName] = appScopeSymbol.getClassCall()
                        },
                )
        }
        return contribution.symbol
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): Set<Name> {
        return if (
            classSymbol.origin == Key.origin &&
                classSymbol.classId.shortClassName == Ids.nestedContributionName
        ) {
            setOf(Ids.bindName)
        } else {
            emptySet()
        }
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirNamedFunctionSymbol> {
        val owner = context?.owner ?: return emptyList()
        if (
            owner.origin != Key.origin ||
                owner.classId.shortClassName != Ids.nestedContributionName ||
                callableId.callableName != Ids.bindName
        ) {
            return emptyList()
        }

        val selectorSymbol =
            session.symbolProvider.getClassLikeSymbolByClassId(owner.classId.outerClassId!!)
                as? FirRegularClassSymbol ?: return emptyList()
        val bindsFunction =
            buildMemberFunction(
                owner = owner,
                returnTypeProvider = {
                    Ids.debuggableConfigSelectorClassId.constructClassLikeType()
                },
                callableId = callableId,
                origin = Key.origin,
                visibility = Visibilities.Public,
                modality = Modality.ABSTRACT,
            ) {
                valueParameters += buildValueParameter {
                    resolvePhase = FirResolvePhase.BODY_RESOLVE
                    moduleData = session.moduleData
                    origin = Key.origin
                    returnTypeRef = selectorSymbol.defaultType().toFirResolvedTypeRef()
                    name = Ids.implName
                    symbol = FirValueParameterSymbol()
                    containingDeclarationSymbol = this@buildMemberFunction.symbol
                }
            }
        bindsFunction.replaceAnnotations(
            listOf(
                buildSimpleAnnotationCall(session, ClassIds.binds, bindsFunction.symbol),
                buildSimpleAnnotationCall(session, ClassIds.intoSet, bindsFunction.symbol),
            )
        )
        return listOf(bindsFunction.symbol as FirNamedFunctionSymbol)
    }

    override fun getContributionTargets(): List<ContributionTarget> {
        return annotatedClasses.map { classSymbol ->
            val nestedInterfaceClassId =
                classSymbol.classId.createNestedClassId(Ids.nestedContributionName)
            ContributionTarget(
                contributingClassId = nestedInterfaceClassId,
                scope = ClassIds.appScope,
            )
        }
    }

    override fun getContributionHints(): List<ContributionHint> {
        return annotatedClasses.map { classSymbol ->
            val nestedInterfaceClassId =
                classSymbol.classId.createNestedClassId(Ids.nestedContributionName)
            ContributionHint(
                contributingClassId = nestedInterfaceClassId,
                scope = ClassIds.appScope,
            )
        }
    }

    private object Key : GeneratedDeclarationKey()

    public class Factory :
        MetroFirDeclarationGenerationExtension.Factory, MetroContributionHintExtension.Factory {
        override fun create(
            session: FirSession,
            options: MetroOptions,
            compatContext: CompatContext,
        ): ContributesConfigSelectorFir = ContributesConfigSelectorFir(session, compatContext)
    }
}
