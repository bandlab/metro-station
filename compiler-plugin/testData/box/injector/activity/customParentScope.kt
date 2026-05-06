interface MyScope

@ContributesInjector(scope = MyScope::class)
class MyActivity : CommonActivity<Unit>() {
    @Inject lateinit var myDependency: MyDependency
}

@DependencyGraph(MyScope::class)
interface AppGraph {

    @Provides
    fun provideInt(): Int = 123
}

@Inject
class MyDependency(
    val text: String,
    val int: Int
)

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myActivity = MyActivity()
    val graph = appGraph.asContribution<MyActivity.FeatureExtension.Factory>().create(myActivity)
    graph.injector.injectMembers(myActivity)
    assertEquals("CommonActivity", myActivity.myDependency.text)
    assertEquals(123, myActivity.myDependency.int)
    return "OK"
}
