import android.content.BroadcastReceiver

@MetroStation(appDependencies = MyBroadcastReceiver.ServiceProvider::class)
class MyBroadcastReceiver : BroadcastReceiver() {
    @Inject lateinit var myDependency: MyDependency

    override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
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