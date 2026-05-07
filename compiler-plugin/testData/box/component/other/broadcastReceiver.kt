import android.content.BroadcastReceiver

@ContributesComponent(appDependencies = MyBroadcastReceiver.ServiceProvider::class)
class MyBroadcastReceiver : BroadcastReceiver() {
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
    val myReceiver = MyBroadcastReceiver()
    val graph = createGraphFactory<MyBroadcastReceiver.FeatureGraph.Factory>().create(
        feature = myReceiver,
        serviceProvider = appGraph,
        extraDependencies = EmptyExtraDependencies
    )
    graph.injector.injectMembers(myReceiver)
    assertEquals("Hello!", myReceiver.myDependency.text)
    return "OK"
}
