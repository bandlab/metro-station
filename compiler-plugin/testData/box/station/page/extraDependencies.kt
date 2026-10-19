// The plain constructor parameter intentionally has no val: the plugin must retain it for
// injectViewModel and combine its Boolean binding with the app graph's Long binding.
@MetroStation(
    appDependencies = MyPage.ServiceProvider::class,
    extraDependencies = MyPage.ExtraDependencies::class
)
class MyPage(extraDependencies: ExtraDependencies) : Page<MyViewModel> {

    interface ServiceProvider {
        val number: Long
    }

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
}

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val extraDependencies = object : MyPage.ExtraDependencies {
        override val boolean: Boolean = true
    }
    val myPage = MyPage(extraDependencies)
    val viewModel = myPage.injectViewModel(AndroidPageGraphDependencies.fromAppGraph(appGraph))
    assertEquals("Hello! true 123", viewModel.value)
    return "OK"
}
