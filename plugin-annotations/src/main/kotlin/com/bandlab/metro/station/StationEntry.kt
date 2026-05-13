package com.bandlab.metro.station

import dev.zacsweers.metro.AppScope
import kotlin.reflect.KClass

/**
 *  ## This annotation generates a Graph Extension for the feature.
 *  > [StationEntry] is in maintence mode as we're shifting towards graph-per-feature, please use [MetroStation] instead.
 *
 *  Annotate your feature with [StationEntry], the compiler plugin will contribute a graph extension towards
 *  the [parentScope] for you, and contribute the factory to a multibinding to the AppGraph for runtime use.
 *
 *  ```kotlin
 *  @StationEntry
 *  class MyPage : Page<MyViewModel> {
 *
 *    @Composable
 *    override fun Content(viewModel: MyViewModel) {
 *      // MyViewModel is already injected at this point
 *      println(viewModel.greeting)
 *    }
 *  }
 *
 *  @ContributesTo(MyPage::class)
 *  interface MyPageProviders {
 *    @Provides
 *    fun provideGreeting(): String = "Hello, Station Entry!"
 *  }
 *  ```
 *  Supported type: Page, Activity, Fragment
 *
 * ## Under the hood
 *
 * The compiler plugin will generate such FIR structures for you:
 * ```kotlin
 * @StationEntry
 * class MyPage : Page<MyViewModel>() {
 *
 *   // This extension generates:
 *   @IROnlyFactories
 *   @MyPageScope
 *   @GraphExtension(
 *     scope = MyPage::class,
 *     bindingContainers = [DefaultPageDependencies::class, FeatureBindings::class]
 *   )
 *   interface FeatureExtension : PageInjector<MyPage> {
 *
 *     @ContributesTo(AppScope::class)
 *     @GraphExtension.Factory
 *     interface Factory {
 *       fun create(@Provides feature: MyPage): FeatureExtension
 *     }
 *   }
 *
 *   @IROnlyFactories
 *   @BindingContainer
 *   object FeatureBindings {
 *     @Provides
 *     fun provideBaseType(feature: MyPage): Page<*> = feature
 *   }
 *
 *   @IROnlyFactories
 *   @ContributesTo(scope = AppScope::class)
 *   interface ExtensionFactoryContribution {
 *     @Provides
 *     @IntoMap
 *     @ClassKey(MyPage::class)
 *     fun provideFactory(factory: FeatureExtension.Factory): Any = factory
 *   }
 * }
 * ```
 *
 * Besides the basic support, we will also generate param providers:
 * - For CommonActivity, param type T is available in the graph.
 * - For ParamPage, we will provide both initial param, and a flow of param that listens to the host activity's onNewIntent.
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
