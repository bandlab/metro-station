@MetroStation(
    appDependencies = MyPage.ServiceProvider::class,
    excludes = [RealStringProvider::class]
)
class MyPage : Page<MyViewModel> {
    interface ServiceProvider
}

@Inject
class MyViewModel(
    val string: String,
)

@ContributesTo(MyPage::class)
interface RealStringProvider {
    @Provides
    fun provideString(): String = "real string"
}

@ContributesTo(MyPage::class)
interface FakeStringProvider {
    @Provides
    fun provideString(): String = "fake string"
}

@DependencyGraph(AppScope::class)
interface AppGraph

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myPage = MyPage()
    val viewModel = myPage.injectViewModel(AndroidPageGraphDependencies.fromAppGraph(appGraph))
    assertEquals("fake string", viewModel.string)
    return "OK"
}