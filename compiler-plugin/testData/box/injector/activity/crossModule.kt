// MODULE: lib
@ContributesInjector
class MyActivity : CommonActivity<Unit>() {
    @Inject lateinit var myDependency: MyDependency
}

@Inject
class MyDependency(
    val text: String,
    val int: Int
)

// MODULE: main(lib)
@DependencyGraph(AppScope::class)
interface AppGraph {

    @Provides
    fun provideInt(): Int = 123
}

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myActivity = MyActivity()
    val graph = appGraph.asContribution<MyActivity.FeatureExtension.Factory>().create(myActivity)
    graph.injector.injectMembers(myActivity)
    assertEquals("CommonActivity", myActivity.myDependency.text)
    assertEquals(123, myActivity.myDependency.int)
    return "OK"
}
