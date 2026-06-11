package com.bandlab.metro.station

import dev.zacsweers.metro.AppScope
import kotlin.reflect.KClass

/**
 * ## This annotation generates a Graph Extension for the feature.
 * > [StationEntry] is in maintenance mode as we're shifting towards graph-per-feature, please use [MetroStation] instead.
 *
 * Annotate your feature with [StationEntry], and the compiler plugin will contribute a graph extension towards
 * the [parentScope] for you, and contribute the factory to a multibinding to the AppGraph for runtime use.
 *
 * ```kotlin
 * @StationEntry
 * class MyPage : Page<MyViewModel> {
 *
 *   @Composable
 *   override fun Content(viewModel: MyViewModel) {
 *     // MyViewModel is already injected at this point
 *     println(viewModel.greeting)
 *   }
 * }
 *
 * @ContributesTo(MyPage::class)
 * interface MyPageProviders {
 *   @Provides
 *   fun provideGreeting(): String = "Hello, Station Entry!"
 * }
 * ```
 * Supported types: Page, Activity, Fragment
 *
 * ## Under the hood
 *
 * The compiler plugin will generate such FIR structures for you:
 * ```kotlin
 * @StationEntry
 * class MyPage : Page<MyViewModel> {
 *
 *   // This extension generates:
 *   @GeneratedByMetroStation
 *   override fun injectViewModel(deps: PageGraphDependencies): MyViewModel {
 *     val factory = deps.activity.resolveServiceProvider<FeatureExtension.Factory>()
 *     return factory.create(this, Unit, deps).getPageViewModel()
 *   }
 *
 *   @PageScope
 *   @GraphExtension(
 *     scope = MyPage::class,
 *     bindingContainers = [DefaultPageDependencies::class, FeatureBindings::class]
 *   )
 *   interface FeatureExtension : PageInjector<MyViewModel> {
 *
 *     @ContributesTo(AppScope::class)
 *     @GraphExtension.Factory
 *     interface Factory {
 *       fun create(
 *         @Provides feature: MyPage,
 *         @Provides param: Unit, // or the actual param type if you're using ParamPage
 *         @Includes pageGraphDependencies: PageGraphDependencies,
 *       ): FeatureExtension
 *     }
 *   }
 *
 *   @IROnlyFactories
 *   @BindingContainer
 *   object FeatureBindings {
 *     @Provides
 *     fun provideBaseType(feature: MyPage): Page<*> = feature
 *   }
 * }
 * ```
 *
 * Same as @MetroStation, we will also provide default dependencies and generate param providers in FeatureBindings if the feature has a param.
 *
 * For classes extend `CommonActivity`, we will override the `inject` method like this:
 * ```kotlin
 * class MyActivity : CommonActivity<Unit>() {
 *   @GeneratedByMetroStation
 *   override fun inject() {
 *     val factory = resolveServiceProvider<FeatureExtension.Factory>()
 *     factory.create(this).injector.injectMembers(this)
 *   }
 * }
 * ```
 *
 *  @param parentScope The dependency graph marker to contribute the extension towards, default to [AppScope].
 *  @param graphMarker A marker to aggregate the extension, default to the feature class itself (ex. MyPage).
 *
 *  @see [dev.zacsweers.metro.GraphExtension]
 */
@Target(AnnotationTarget.CLASS)
public annotation class StationEntry(
    val parentScope: KClass<*> = AppScope::class,
    val graphMarker: KClass<*> = Nothing::class,
)
