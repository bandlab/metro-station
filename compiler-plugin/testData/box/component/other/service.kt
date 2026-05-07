import android.app.Service

@ContributesComponent(appDependencies = MyService.ServiceProvider::class)
class MyService : Service() {
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
    val myService = MyService()
    val graph = createGraphFactory<MyService.FeatureGraph.Factory>().create(
        feature = myService,
        serviceProvider = appGraph,
        extraDependencies = EmptyExtraDependencies
    )
    graph.injector.injectMembers(myService)
    assertEquals("Hello!", myService.myDependency.text)
    return "OK"
}
