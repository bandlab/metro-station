@StationEntry
class MyActivity : CommonActivity<Unit>() {
    @Inject lateinit var myDependency: MyDependency
}

@DependencyGraph(AppScope::class)
interface AppGraph

@Inject
class MyDependency(val type: String)

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myActivity = MyActivity()
    val graph = appGraph.asContribution<MyActivity.FeatureExtension.Factory>().create(myActivity)
    graph.injector.injectMembers(myActivity)
    assertEquals("CommonActivity", myActivity.myDependency.type)
    return "OK"
}
