@ContributesInjector
class MyPage : Page<MyViewModel>

@Inject
class MyViewModel(val number: Long)

@DependencyGraph(AppScope::class)
interface AppGraph {

    @Provides
    fun provideLong(): Long = 123L
}