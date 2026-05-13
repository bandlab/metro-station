interface MyScope

@StationEntry(graphMarker = MyScope::class)
class MyPage : Page<MyViewModel>

@Inject
class MyViewModel(val int: Int)

@ContributesTo(MyScope::class)
interface IntProvider {
    @Provides
    fun provideInt(): Int = 42
}

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
    assertEquals(42, pageGraph.getPageViewModel().int)
    return "OK"
}