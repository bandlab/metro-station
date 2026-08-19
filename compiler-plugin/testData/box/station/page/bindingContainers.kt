@MetroStation(
    appDependencies = MyPage.ServiceProvider::class,
    bindingContainers = [LongProvider::class]
)
class MyPage : Page<MyViewModel> {
    interface ServiceProvider
}

@Inject
class MyViewModel(
    val number: Long,
)

@BindingContainer
object LongProvider {
    @Provides
    fun provideLong(): Long = 123L
}

@DependencyGraph(AppScope::class)
interface AppGraph

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myPage = MyPage()
    val viewModel = myPage.injectViewModel(AndroidPageGraphDependencies.fromAppGraph(appGraph))
    assertEquals(123L, viewModel.number)
    return "OK"
}