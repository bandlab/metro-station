@ContributesComponent(appDependencies = MyPage.ServiceProvider::class)
class MyPage(context: Context) : Page<MyViewModel> {

    interface ServiceProvider
}

@Inject
class MyViewModel(val text: String)

@DependencyGraph(AppScope::class)
interface AppGraph

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val pageGraph = createGraphFactory<MyPage.FeatureGraph.Factory>().create(
        feature = MyPage(Context.FAKE),
        serviceProvider = appGraph,
        extraDependencies = EmptyExtraDependencies
    )
    assertEquals("Page", pageGraph.getPageViewModel().text)
    return "OK"
}