@ContributesInjector
class MyActivity : CommonActivity<MyActivity.Param>() {
    @Inject lateinit var myViewModel: MyViewModel

    data class Param(val id: String)
}

@DependencyGraph(AppScope::class)
interface AppGraph

@Inject
class MyViewModel(
    val param: MyActivity.Param,
)