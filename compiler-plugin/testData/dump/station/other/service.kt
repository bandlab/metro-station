import android.app.Service

@MetroStation(appDependencies = MyService.ServiceProvider::class)
class MyService : Service() {
    @Inject lateinit var myDependency: MyDependency

    override fun onCreate() {
        println(myDependency.text)
    }

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