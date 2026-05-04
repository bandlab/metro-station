import com.bandlab.common.android.di.ContributesComponent
import com.bandlab.common.android.pager.screen.di.EmptyExtraDependencies
import com.bandlab.uikit.api.page.Page

@ContributesComponent(appDependencies = MyPage.ServiceProvider::class)
class MyPage : Page<MyViewModel> {

    interface ServiceProvider
}

@Inject
class MyViewModel(val text: String)

@DependencyGraph(AppScope::class)
interface AppGraph

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val pageGraph = createGraphFactory<MyPage.FeatureGraph.Factory>().create(
        feature = MyPage(),
        serviceProvider = appGraph,
        extraDependencies = EmptyExtraDependencies
    )
    assertEquals("Page", pageGraph.getPageViewModel().text)
    return "OK"
}