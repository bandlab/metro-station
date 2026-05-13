@MetroStation(appDependencies = MyActivity.ServiceProvider::class)
class MyActivity : CommonActivity<Unit>() {
    @Inject lateinit var myDependency: MyDependency

    interface ServiceProvider {
        val myDependency: MyDependency
    }
}

@DependencyGraph(AppScope::class)
interface AppGraph {

    @Provides
    fun provideText(): String = "Hello!"
}

@Inject
class MyDependency(val text: String)

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myActivity = MyActivity()
    val graph = createGraphFactory<MyActivity.FeatureGraph.Factory>().create(
        feature = myActivity,
        serviceProvider = appGraph,
        extraDependencies = EmptyExtraDependencies
    )
    graph.injector.injectMembers(myActivity)
    assertEquals("Hello!", myActivity.myDependency.text)
    return "OK"
}
