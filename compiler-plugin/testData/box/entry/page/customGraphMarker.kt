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
    val viewModel = myPage.injectViewModel(PageGraphDependencies.fromAppGraph(appGraph))
    assertEquals(42, viewModel.int)
    return "OK"
}