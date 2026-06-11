@MetroStation(appDependencies = MyActivity.ServiceProvider::class)
class MyActivity : CommonActivity<Unit>() {
    @Inject lateinit var myDependency: MyDependency

    interface ServiceProvider {
        val boolean: Boolean
        val long: Long
    }
}

@DependencyGraph(AppScope::class)
interface AppGraph {

    @Provides
    fun provideBoolean(): Boolean = true

    @Provides
    fun provideLong(): Long = 42L
}

@Inject
class MyDependency(
    boolean: Boolean,
    long: Long,
) {
    val value = "Hello! $boolean $long"
}

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myActivity = MyActivity()
    myActivity.setAppGraphAndInject(appGraph)
    assertEquals("Hello! true 42", myActivity.myDependency.value)
    return "OK"
}
