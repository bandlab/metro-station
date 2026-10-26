// Exercises the generated graph and members injector directly. Injection at the start of doWork
// is checked by dump/station/other/worker.kt, rather than invoking the callback here.
import androidx.work.CoroutineWorker
import androidx.work.Result

@MetroStation(appDependencies = MyWorker.ServiceProvider::class)
class MyWorker : CoroutineWorker() {
    @Inject lateinit var myDependency: MyDependency

    override suspend fun doWork(): Result {
        println(myDependency.text)
        return object : Result() {}
    }

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
    val myWorker = MyWorker()
    val graph = createGraphFactory<MyWorker.FeatureGraph.Factory>().create(
        feature = myWorker,
        serviceProvider = appGraph,
        extraDependencies = EmptyExtraDependencies
    )
    graph.injector.injectMembers(myWorker)
    assertEquals("Hello!", myWorker.myDependency.text)
    return "OK"
}
