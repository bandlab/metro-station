@ContributesInjector
class MyPage : Page<MyViewModel>

@Inject
class MyViewModel(val text: String)

@DependencyGraph(AppScope::class)
interface AppGraph

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myPage = MyPage()
    val pageGraph = appGraph.asContribution<MyPage.FeatureExtension.Factory>().create(
        feature = myPage,
        pageGraphDependencies = PageGraphDependencies(),
        navPageDependencies = NavPageDependencies()
    )
    assertEquals("Page", pageGraph.getPageViewModel().text)
    return "OK"
}