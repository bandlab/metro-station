// MODULE: lib
@MetroStation(appDependencies = MyActivity.ServiceProvider::class)
class MyActivity : CommonActivity<Unit>() {
    @Inject lateinit var myDependency: MyDependency

    interface ServiceProvider {
        val myDependency: MyDependency
    }
}

@Inject
class MyDependency(val text: String)

// MODULE: main(lib)
@DependencyGraph(AppScope::class)
interface AppGraph {

    @Provides
    fun provideText(): String = "Hello!"
}

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myActivity = MyActivity()
    myActivity.setAppGraphAndInject(appGraph)
    assertEquals("Hello!", myActivity.myDependency.text)
    return "OK"
}
