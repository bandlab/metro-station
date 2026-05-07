interface ChildScope
interface ParentScope

@ContributesInjector(
    scope = ParentScope::class,
    graphMarker = ChildScope::class
)
class MyActivity : CommonActivity<Unit>() {
    @Inject lateinit var myDependency: MyDependency
}

@DependencyGraph(ParentScope::class)
interface AppGraph {
    @Provides
    fun provideInt(): Int = 123
}

@ContributesTo(ChildScope::class)
interface ChildProviders {
    @Provides
    fun provideBoolean(): Boolean = true
}

@Inject
class MyDependency(
    val text: String,
    val int: Int,
    val boolean: Boolean
)

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myActivity = MyActivity()
    val graph = appGraph.asContribution<MyActivity.FeatureExtension.Factory>().create(myActivity)
    graph.injector.injectMembers(myActivity)
    assertEquals("CommonActivity", myActivity.myDependency.text)
    assertEquals(123, myActivity.myDependency.int)
    assertEquals(true, myActivity.myDependency.boolean)
    return "OK"
}
