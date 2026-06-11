// MODULE: lib
@MetroStation(appDependencies = MyPage.ServiceProvider::class)
class MyPage(context: Context) : Page<MyViewModel> {

    interface ServiceProvider {
        val number: Long
    }
}

@Inject
class MyViewModel(val number: Long)

// MODULE: main(lib)
@DependencyGraph(AppScope::class)
interface AppGraph {

    @Provides
    fun provideLong(): Long = 123L
}

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myPage = MyPage(Context.FAKE)
    val viewModel = myPage.injectViewModel(AndroidPageGraphDependencies.fromAppGraph(appGraph))
    assertEquals(123L, viewModel.number)
    return "OK"
}