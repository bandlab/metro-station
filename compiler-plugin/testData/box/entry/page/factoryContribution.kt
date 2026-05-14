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
    val factory = appGraph.graphExtensionFactories[MyPage::class] as MyPage.FeatureExtension.Factory
    val pageGraph = factory.create(
        feature = myPage,
        pageGraphDependencies = PageGraphDependencies(),
        navPageDependencies = NavPageDependencies()
    )
    assertEquals(123L, pageGraph.getPageViewModel().number)
    return "OK"
}