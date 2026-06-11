@StationEntry
class MyPage : Page<MyViewModel>

@Inject
class MyViewModel(val number: Long)

@DependencyGraph(AppScope::class)
interface AppGraph {

    @Provides
    fun provideLong(): Long = 123L
}

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myPage = MyPage()
    val viewModel = myPage.injectViewModel(AndroidPageGraphDependencies.fromAppGraph(appGraph))
    assertEquals(123L, viewModel.number)
    return "OK"
}