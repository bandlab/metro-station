import com.bandlab.common.android.di.ContributesComponent
import com.bandlab.android.common.activity.CommonActivity

@ContributesComponent(appDependencies = MyActivity.ServiceProvider::class)
class MyActivity : CommonActivity<Unit>() {
    @Inject lateinit var myDependency: MyDependency

    interface ServiceProvider
}

@DependencyGraph(AppScope::class)
interface AppGraph

@Inject
class MyDependency(val type: String)

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val myActivity = MyActivity()
    val graph = createGraphFactory<MyActivity.FeatureGraph.Factory>().create(
        feature = myActivity,
        serviceProvider = appGraph
    )
    graph.injector.injectMembers(myActivity)
    assertEquals("CommonActivity", myActivity.myDependency.type)
    return "OK"
}
