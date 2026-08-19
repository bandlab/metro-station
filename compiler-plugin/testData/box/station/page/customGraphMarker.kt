interface MyScope

@MetroStation(
    appDependencies = MyPage.ServiceProvider::class,
    graphMarker = MyScope::class
)
class MyPage : Page<MyViewModel> {

    interface ServiceProvider
}

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
    val viewModel = myPage.injectViewModel(AndroidPageGraphDependencies.fromAppGraph(appGraph))
    assertEquals(42, viewModel.int)
    return "OK"
}