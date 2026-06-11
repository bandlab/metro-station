@StationEntry
class MyActivity : CommonActivity<MyActivity.Param>() {
    @Inject lateinit var myViewModel: MyViewModel

    data class Param(val id: String)
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

    myActivity.setAppGraphAndInject(appGraph)
    assertEquals("test", myActivity.myViewModel.param.id)
    return "OK"
}
