interface MyScope

@StationEntry(graphMarker = MyScope::class)
class MyActivity : CommonActivity<Unit>() {
    @Inject lateinit var myDependency: MyDependency
}

@ContributesTo(MyScope::class)
interface IntProvider {
    @Provides
    fun provideInt(): Int = 42
}

@DependencyGraph(AppScope::class)
interface AppGraph

@Inject
class MyDependency(val int: Int)

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myActivity = MyActivity()
    val graph = appGraph.asContribution<MyActivity.FeatureExtension.Factory>().create(myActivity)
    graph.injector.injectMembers(myActivity)
    assertEquals(42, myActivity.myDependency.int)
    return "OK"
}
