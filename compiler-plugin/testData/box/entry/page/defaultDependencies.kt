@StationEntry
class MyPage : Page<MyViewModel>

@Inject
class MyViewModel(val text: String)

@DependencyGraph(AppScope::class)
interface AppGraph

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myPage = MyPage()
    val viewModel = myPage.injectViewModel(PageGraphDependencies.fromAppGraph(appGraph))
    assertEquals("Page", viewModel.text)
    return "OK"
}