@MetroStation(
    appDependencies = MyPage.ServiceProvider::class,
    additionalScopes = [
        LongScope::class,
        StringScope::class
    ]
)
class MyPage : Page<MyViewModel> {
    interface ServiceProvider
}

@Inject
class MyViewModel(
    val number: Long,
    val string: String
)

interface LongScope
interface StringScope

@ContributesTo(LongScope::class)
interface LongProvider {
    @Provides
    fun provideLong(): Long = 123L
}

@ContributesTo(StringScope::class)
interface StringProvider {
    @Provides
    fun provideString(): String = "str"
}

@DependencyGraph(AppScope::class)
interface AppGraph

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myPage = MyPage()
    val viewModel = myPage.injectViewModel(AndroidPageGraphDependencies.fromAppGraph(appGraph))
    assertEquals(123L, viewModel.number)
    assertEquals("str", viewModel.string)
    return "OK"
}