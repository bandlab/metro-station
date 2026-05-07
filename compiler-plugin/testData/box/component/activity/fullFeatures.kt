interface MyScope

@ContributesComponent(
    graphMarker = MyScope::class,
    appDependencies = MyActivity.AppServiceProvider::class,
    extraDependencies = MyActivity.ExtraDependencies::class
)
class MyActivity : CommonActivity<Unit>() {
    @Inject lateinit var myDependency: MyDependency

    interface AppServiceProvider {
        val long: Long
    }

    @ContributesTo(Unit::class)
    interface ExtraDependencies {
        val boolean: Boolean
    }
}

@ContributesTo(MyScope::class)
interface IntProvider {
    @Provides
    fun provideInt(): Int = 42
}

@DependencyGraph(AppScope::class)
interface AppGraph {
    @Provides
    fun provideLong(): Long = 123L
}

@DependencyGraph(Unit::class)
interface ExtraGraph {
    @Provides
    fun provideBoolean(): Boolean = true
}

@Inject
class MyDependency(
    val int: Int,
    val long: Long,
    val boolean: Boolean
)

fun box(): String {
    val myActivity = MyActivity()
    val graph = createGraphFactory<MyActivity.FeatureGraph.Factory>().create(
        feature = myActivity,
        serviceProvider = createGraph<AppGraph>(),
        extraDependencies = createGraph<ExtraGraph>()
    )
    graph.injector.injectMembers(myActivity)
    assertEquals(42, myActivity.myDependency.int)
    assertEquals(123L, myActivity.myDependency.long)
    assertEquals(true, myActivity.myDependency.boolean)
    return "OK"
}
