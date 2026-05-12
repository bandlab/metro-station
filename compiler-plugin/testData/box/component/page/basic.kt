@ContributesComponent(appDependencies = MyPage.ServiceProvider::class)
class MyPage(context: Context) : Page<MyViewModel> {

    interface ServiceProvider {
        val number: Long
    }
}

@Inject
class MyViewModel(val number: Long)

@DependencyGraph(AppScope::class)
interface AppGraph {

    @Provides
    fun provideLong(): Long = 123L
}

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val pageGraph = createGraphFactory<MyPage.FeatureGraph.Factory>().create(
        feature = MyPage(Context.FAKE),
        serviceProvider = appGraph,
        extraDependencies = EmptyExtraDependencies
    )
    assertEquals(123L, pageGraph.getPageViewModel().number)
    return "OK"
}