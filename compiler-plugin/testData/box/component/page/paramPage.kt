import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@ContributesComponent(appDependencies = MyPage.ServiceProvider::class)
class MyPage(context: Context) : ParamPage<MyViewModel, MyPage.Params> {

    data class Params(val number: Long)

    interface ServiceProvider
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
    val pageGraph = createGraphFactory<MyPage.FeatureGraph.Factory>().create(
        feature = MyPage(Context.FAKE),
        pageGraphDependencies = PageGraphDependencies(initialParam = MyPage.Params(number = 123L)),
        serviceProvider = appGraph,
        extraDependencies = EmptyExtraDependencies
    )
    assertEquals(123L, pageGraph.getPageViewModel().number)
    assertEquals(123L, pageGraph.getPageViewModel().numberFromFlow)
    return "OK"
}