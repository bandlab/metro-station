@MetroStation(
    appDependencies = MyPage.ServiceProvider::class,
    excludes = [RealNumberProvider::class]
)
class MyPage : Page<MyViewModel> {
    interface ServiceProvider
}

@Inject
class MyViewModel(
    val number: Int,
)

@ContributesTo(MyPage::class)
interface RealNumberProvider {
    @Provides
    fun provideInt(): Int = 123
}

@ContributesTo(MyPage::class)
interface FakeNumberProvider {
    @Provides
    fun provideInt(): Int = 0
}

@DependencyGraph(AppScope::class)
interface AppGraph

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myPage = MyPage()
    val viewModel = myPage.injectViewModel(AndroidPageGraphDependencies.fromAppGraph(appGraph))
    assertEquals(0, viewModel.number)
    return "OK"
}
