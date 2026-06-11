// MODULE: lib
interface MyScope

// MODULE: main(lib)
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
    val viewModel = myPage.injectViewModel(PageGraphDependencies.fromAppGraph(appGraph))
    assertEquals(123L, viewModel.number)
    return "OK"
}