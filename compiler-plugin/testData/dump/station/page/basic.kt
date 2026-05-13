@MetroStation(appDependencies = MyPage.ServiceProvider::class)
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
