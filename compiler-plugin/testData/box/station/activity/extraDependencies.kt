@MetroStation(
    appDependencies = MyActivity.ServiceProvider::class,
    extraDependencies = MyActivity.ExtraDependencies::class
)
class MyActivity : CommonActivity<Unit>() {
    @Inject lateinit var myDependency: MyDependency

    interface ServiceProvider {
        val boolean: Boolean
    }

    @ContributesTo(AppScope::class)
    interface ExtraDependencies {
        val long: Long
    }
}

@DependencyGraph(AppScope::class)
interface AppGraph {

    @Provides
    fun provideBoolean(): Boolean = true

    @Provides
    fun provideLong(): Long = 42L
}

@Inject
class MyDependency(
    boolean: Boolean,
    long: Long,
) {
    val value = "Hello! $boolean $long"
}

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myActivity = MyActivity()
    val graph = createGraphFactory<MyActivity.FeatureGraph.Factory>().create(
        feature = myActivity,
        serviceProvider = appGraph,
        extraDependencies = appGraph
    )
    graph.injector.injectMembers(myActivity)
    assertEquals("Hello! true 42", myActivity.myDependency.value)
    return "OK"
}
