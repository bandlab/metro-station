interface MyScope

@MetroStation(
    graphMarker = MyScope::class,
    appDependencies = MyActivity.AppServiceProvider::class
)
class MyActivity : CommonActivity<Unit>() {
    @Inject lateinit var myDependency: MyDependency

    interface AppServiceProvider {
        val long: Long
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

@Inject
class MyDependency(
    val int: Int,
    val long: Long,
)

fun box(): String {
    val myActivity = MyActivity()
    val graph = createGraphFactory<MyActivity.FeatureGraph.Factory>().create(
        feature = myActivity,
        serviceProvider = createGraph<AppGraph>(),
        extraDependencies = EmptyExtraDependencies
    )
    graph.injector.injectMembers(myActivity)
    assertEquals(42, myActivity.myDependency.int)
    assertEquals(123L, myActivity.myDependency.long)
    return "OK"
}
