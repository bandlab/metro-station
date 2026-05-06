import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@ContributesInjector
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
