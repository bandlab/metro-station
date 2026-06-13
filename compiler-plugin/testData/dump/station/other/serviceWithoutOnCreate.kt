import android.app.Service

@MetroStation(appDependencies = MyService.ServiceProvider::class)
class MyService : Service() {
    @Inject lateinit var myDependency: MyDependency

    interface ServiceProvider {
        val myDependency: MyDependency
    }
}

@DependencyGraph(AppScope::class)
interface AppGraph {

    @Provides
    fun provideText(): String = "Hello!"
}

@Inject
class MyDependency(val text: String)