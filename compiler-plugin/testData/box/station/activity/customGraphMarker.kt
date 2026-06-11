interface MyScope

@MetroStation(
    appDependencies = MyActivity.ServiceProvider::class,
    graphMarker = MyScope::class
)
class MyActivity : CommonActivity<Unit>() {
    @Inject lateinit var myDependency: MyDependency

    interface ServiceProvider
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
    myActivity.setAppGraphAndInject(appGraph)
    assertEquals(42, myActivity.myDependency.int)
    return "OK"
}
