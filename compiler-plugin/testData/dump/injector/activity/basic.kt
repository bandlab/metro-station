@ContributesInjector
class MyActivity : CommonActivity<Unit>() {
    @Inject lateinit var myDependency: MyDependency
}

@DependencyGraph(AppScope::class)
interface AppGraph {

    @Provides
    fun provideInt(): Int = 123
}

@Inject
class MyDependency(
    val text: String,
    val int: Int
)
