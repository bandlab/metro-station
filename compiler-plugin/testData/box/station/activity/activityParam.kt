@MetroStation(appDependencies = MyActivity.ServiceProvider::class)
class MyActivity : CommonActivity<MyActivity.Param>() {
    @Inject lateinit var myViewModel: MyViewModel

    data class Param(val id: String)

    interface ServiceProvider
}

@DependencyGraph(AppScope::class)
interface AppGraph

@Inject
class MyViewModel(
    val param: MyActivity.Param,
)

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myActivity = MyActivity()
    // Fake the param
    myActivity.params = MyActivity.Param("test")

    val graph = createGraphFactory<MyActivity.FeatureGraph.Factory>().create(
        feature = myActivity,
        serviceProvider = appGraph,
        extraDependencies = EmptyExtraDependencies
    )
    graph.injector.injectMembers(myActivity)
    assertEquals("test", myActivity.myViewModel.param.id)
    return "OK"
}
