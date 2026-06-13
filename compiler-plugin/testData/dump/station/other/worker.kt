import androidx.work.CoroutineWorker
import androidx.work.Result

@MetroStation(appDependencies = MyWorker.ServiceProvider::class)
class MyWorker : CoroutineWorker() {
    @Inject lateinit var myDependency: MyDependency

    override suspend fun doWork(): Result {
        return object : Result() {}
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