@MetroStation(appDependencies = MyPage.ServiceProvider::class)
class MyPage : Page<MyViewModel> {

    interface ServiceProvider
}

@Inject
class MyViewModel(val text: String)

@DependencyGraph(AppScope::class)
interface AppGraph

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myPage = MyPage()
    val viewModel = myPage.injectViewModel(AndroidPageGraphDependencies.fromAppGraph(appGraph))
    assertEquals("Page", viewModel.text)
    return "OK"
}