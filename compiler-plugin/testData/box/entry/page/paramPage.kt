import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@StationEntry
class MyPage : ParamPage<MyViewModel, MyPage.Params> {
    data class Params(val number: Long)
}

@Inject
class MyViewModel(
    private val param: MyPage.Params,
    private val paramFlow: Flow<MyPage.Params>,
) {
    val number = param.number
    val numberFromFlow = runBlocking { paramFlow.first().number }
}

@DependencyGraph(AppScope::class)
interface AppGraph

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myPage = MyPage()
    val pageGraph = appGraph.asContribution<MyPage.FeatureExtension.Factory>().create(
        feature = MyPage(),
        pageGraphDependencies = PageGraphDependencies(initialParam = MyPage.Params(number = 123L)),
        navPageDependencies = NavPageDependencies()
    )
    assertEquals(123L, pageGraph.getPageViewModel().number)
    assertEquals(123L, pageGraph.getPageViewModel().numberFromFlow)
    return "OK"
}