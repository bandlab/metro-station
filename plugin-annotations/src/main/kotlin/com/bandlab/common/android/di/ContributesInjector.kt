package com.bandlab.common.android.di

import dev.zacsweers.metro.AppScope
import kotlin.reflect.KClass

/**
 *  ### ContributesInjector is in maintence mode, please use [ContributesComponent] instead.
 *
 *  Entry point marker to inject a graph extension, annotate it and contribute
 *  the local dependencies to the graph extension class directly, Metro will
 *  wire up the module during the code generation.
 *
 *  ```kotlin
 *  @ContributesInjector
 *  class MyActivity : CommonActivity() {
 *      @Inject private lateinit var param: String
 *
 *      fun onCreate() {
 *          AndroidInjection.inject(this)
 *      }
 *  }
 *
 *  @ContributesTo(MyActivity::class)
 *  interface MyActivityModule {
 *      @Provides
 *      fun provideParam(activity: MyActivity): String
 *  }
 *  ```
 *  Supported type: Activity, Page, Fragment
 *
 *  @param scope We use [AppScope] by default, you can override other graphs if needed.
 *  @param graphMarker We use the graph extension as the graph marker by default to merge the modules,
 *      but you can provide a custom marker if your feature involves a lot of dependencies.
 *
 *  @see [dev.zacsweers.metro.GraphExtension]
 */
@Target(AnnotationTarget.CLASS)
public annotation class ContributesInjector(
    val scope: KClass<*> = AppScope::class,
    val graphMarker: KClass<*> = Nothing::class,
)