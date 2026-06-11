import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@MetroStation(appDependencies = MyPage.ServiceProvider::class)
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
    val myPage = MyPage(Context.FAKE)
    val viewModel = myPage.injectViewModel(
        PageGraphDependencies.fromAppGraph(appGraph),
        MyPage.Params(number = 123L)
    )
    assertEquals(123L, viewModel.number)
    assertEquals(123L, viewModel.numberFromFlow)
    return "OK"
}