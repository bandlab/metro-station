@MetroStation(
    appDependencies = MyPage.ServiceProvider::class,
    additionalScopes = [
        LongScope::class,
        BooleanScope::class
    ]
)
class MyPage : Page<MyViewModel> {
    interface ServiceProvider
}

@Inject
class MyViewModel(
    val number: Long,
    val boolean: Boolean,
)

interface LongScope
interface BooleanScope

@ContributesTo(LongScope::class)
interface LongProvider {
    @Provides
    fun provideLong(): Long = 123L
}

@ContributesTo(BooleanScope::class)
interface BooleanProvider {
    @Provides
    fun provideBoolean(): Boolean = true
}

@DependencyGraph(AppScope::class)
interface AppGraph

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myPage = MyPage()
    val viewModel = myPage.injectViewModel(AndroidPageGraphDependencies.fromAppGraph(appGraph))
    assertEquals(123L, viewModel.number)
    assertEquals(true, viewModel.boolean)
    return "OK"
}
