// MODULE: lib
interface MyScope

// MODULE: main(lib)
@StationEntry(parentScope = MyScope::class)
class MyActivity : CommonActivity<Unit>() {
    @Inject lateinit var myDependency: MyDependency
}

@Inject
class MyDependency(
    val text: String,
    val int: Int
)

@DependencyGraph(MyScope::class)
interface AppGraph {

    @Provides
    fun provideInt(): Int = 123
}

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myActivity = MyActivity()
    myActivity.setAppGraphAndInject(appGraph)
    assertEquals("CommonActivity", myActivity.myDependency.text)
    assertEquals(123, myActivity.myDependency.int)
    return "OK"
}
