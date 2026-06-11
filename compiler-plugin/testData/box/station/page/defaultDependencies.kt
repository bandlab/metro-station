@MetroStation(appDependencies = MyPage.ServiceProvider::class)
class MyPage(context: Context) : Page<MyViewModel> {

    interface ServiceProvider
}

@Inject
class MyViewModel(val text: String)

@DependencyGraph(AppScope::class)
interface AppGraph

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myPage = MyPage(Context.FAKE)
    val viewModel = myPage.injectViewModel(PageGraphDependencies.fromAppGraph(appGraph))
    assertEquals("Page", viewModel.text)
    return "OK"
}