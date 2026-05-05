import com.bandlab.common.android.pager.screen.ParamPage
import com.bandlab.common.android.pager.screen.di.PageGraphDependencies
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@ContributesComponent(appDependencies = MyPage.ServiceProvider::class)
class MyPage : ParamPage<MyViewModel, MyPage.Params> {

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