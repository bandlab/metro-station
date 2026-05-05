@ContributesComponent(
    appDependencies = MyPage.ServiceProvider::class,
    extraDependencies = MyPage.ExtraDependencies::class
)
class MyPage : Page<MyViewModel> {

    interface ServiceProvider {
        val number: Long
    }

    @ContributesTo(AppScope::class)
    interface ExtraDependencies {
        val boolean: Boolean
    }
}

@Inject
class MyViewModel(
    number: Long,
    boolean: Boolean
) {
    val value = "Hello! $boolean $number"
}

@DependencyGraph(AppScope::class)
interface AppGraph {

    @Provides
    fun provideLong(): Long = 123L

    @Provides
    fun provideBoolean(): Boolean = true
}

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val pageGraph = createGraphFactory<MyPage.FeatureGraph.Factory>().create(
        feature = MyPage(),
        serviceProvider = appGraph,
        extraDependencies = appGraph
    )
    assertEquals("Hello! true 123", pageGraph.getPageViewModel().value)
    return "OK"
}