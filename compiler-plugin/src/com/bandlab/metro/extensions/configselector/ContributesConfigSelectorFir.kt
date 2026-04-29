package com.bandlab.metro.extensions.configselector

import com.bandlab.metro.extensions.utils.*
import com.fueledbycaffeine.autoservice.AutoService
import dev.zacsweers.metro.compiler.MetroOptions
import dev.zacsweers.metro.compiler.api.fir.MetroFirDeclarationGenerationExtension
import dev.zacsweers.metro.compiler.compat.CompatContext
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.builder.buildNamedFunction
import org.jetbrains.kotlin.fir.declarations.builder.buildRegularClass
import org.jetbrains.kotlin.fir.declarations.builder.buildValueParameter
import org.jetbrains.kotlin.fir.declarations.hasAnnotationWithClassId
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.declarations.utils.visibility
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
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import com.bandlab.metro.extensions.configselector.ContributesConfigSelectorIds as Ids

/**
 * This FIR declaration generator generates a multibinding contribution for config selectors that are annotated with
 * [Ids.contributesConfigSelectorFqName].
 *
 * ```kotlin
 * @ContributesConfigSelector
 * object MyConfigSelector : BooleanConfigSelector {
 *
 *   // This extension generates:
 *   @ContributesTo(AppScope::class)
 *   interface MultibindingContribution {
 *     @Binds @IntoSet
 *     fun bind(impl: MyConfigSelector): DebuggableConfigSelector
 *   }
 * }
 * ```
 */
public class ContributesConfigSelectorFir(session: FirSession) : MetroFirDeclarationGenerationExtension(session) {

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(Ids.predicate)
    }

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext,
    ): Set<Name> {
        return if (classSymbol.hasAnnotationWithClassId(Ids.contributesConfigSelector, session)) {
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
        if (!owner.hasAnnotationWithClassId(Ids.contributesConfigSelector, session)) {
            return null
        }

        if (!owner.visibility.isPublicAPI) {
            error("${owner.classId.asString()} must be public to be contributed properly.")
        }

        val nestedClassId = owner.classId.createNestedClassId(name)
        val classSymbol = FirRegularClassSymbol(nestedClassId)

        // Build the @Binds function and add it directly to the class declarations.
        // This makes it visible to Metro's getNestedClassifiersNames (which checks for @Binds
        // functions to decide whether to generate BindsMirror).
        val bindsFunction = buildBindsFunction(nestedClassId, owner)

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

    private fun ClassId.firTypeRef(): FirTypeRef = buildResolvedTypeRef {
        coneType = constructClassLikeType()
    }

    private fun buildBindsFunction(
        classId: ClassId,
        outerOwner: FirClassSymbol<*>,
    ): FirDeclaration {
        val callableId = CallableId(classId, "bind".asName())

        val outerClassType = outerOwner.defaultType()
        // Build the dispatch receiver type manually since classSymbol isn't bound to FIR yet
        val dispatchType = ConeClassLikeTypeImpl(
            ConeClassLikeLookupTagImpl(classId),
            emptyArray(),
            isMarkedNullable = false,
        )

        val functionSymbol = FirNamedFunctionSymbol(callableId)

        return buildNamedFunction {
            resolvePhase = FirResolvePhase.BODY_RESOLVE
            moduleData = session.moduleData
            origin = Key.origin
            symbol = functionSymbol
            name = callableId.callableName
            isLocal = false
            returnTypeRef = Ids.debuggableConfigSelectorClassId.firTypeRef()
            dispatchReceiverType = dispatchType
            status = FirResolvedDeclarationStatusImpl(
                Visibilities.Public,
                Modality.ABSTRACT,
                Visibilities.Public.toEffectiveVisibility(outerOwner, forClass = true),
            )
            this.valueParameters += buildValueParameter {
                resolvePhase = FirResolvePhase.BODY_RESOLVE
                moduleData = session.moduleData
                origin = Key.origin
                returnTypeRef = outerClassType.toFirResolvedTypeRef()
                this.name = Ids.implName
                symbol = FirValueParameterSymbol()
                containingDeclarationSymbol = functionSymbol
            }
            annotations += buildSimpleAnnotationCall(session, ClassIds.binds, functionSymbol)
            annotations += buildSimpleAnnotationCall(session, ClassIds.intoSet, functionSymbol)
        }
    }

    private object Key : GeneratedDeclarationKey()

    @AutoService
    public class Factory : MetroFirDeclarationGenerationExtension.Factory {
        override fun create(
            session: FirSession,
            options: MetroOptions,
            compatContext: CompatContext,
        ): MetroFirDeclarationGenerationExtension = ContributesConfigSelectorFir(session)
    }
}