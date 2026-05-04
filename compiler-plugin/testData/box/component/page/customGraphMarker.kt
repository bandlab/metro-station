import com.bandlab.common.android.di.ContributesComponent
import com.bandlab.common.android.pager.screen.di.EmptyExtraDependencies
import com.bandlab.uikit.api.page.Page

interface MyScope

@ContributesComponent(
    appDependencies = MyPage.ServiceProvider::class,
    graphMarker = MyScope::class
)
class MyPage : Page<MyViewModel> {

    interface ServiceProvider
}

@Inject
class MyViewModel(val int: Int)

@ContributesTo(MyScope::class)
interface IntProvider {
    @Provides
    fun provideInt(): Int = 42
}

@DependencyGraph(AppScope::class)
interface AppGraph

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val pageGraph = createGraphFactory<MyPage.FeatureGraph.Factory>().create(
        feature = MyPage(),
        serviceProvider = appGraph,
        extraDependencies = EmptyExtraDependencies
    )
    assertEquals(42, pageGraph.getPageViewModel().int)
    return "OK"
}