interface MyScope

@StationEntry(parentScope = MyScope::class)
class MyPage : Page<MyViewModel>

@Inject
class MyViewModel(val number: Long)

@DependencyGraph(MyScope::class)
interface AppGraph {

    @Provides
    fun provideLong(): Long = 123L
}

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myPage = MyPage()
    val pageGraph = appGraph.asContribution<MyPage.FeatureExtension.Factory>().create(
        feature = myPage,
        pageGraphDependencies = PageGraphDependencies(),
        navPageDependencies = NavPageDependencies()
    )
    assertEquals(123L, pageGraph.getPageViewModel().number)
    return "OK"
}