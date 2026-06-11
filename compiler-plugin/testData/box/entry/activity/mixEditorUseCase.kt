import com.bandlab.android.common.ActivityScope

interface MixEditorComponentScope
interface MixEditorActivityScope

@Scope
annotation class MixEditorScope

@StationEntry(
    parentScope = MixEditorComponentScope::class,
    graphMarker = MixEditorActivityScope::class
)
class MixEditorActivity : CommonActivity<Unit>() {
    @Inject lateinit var mixEditorDependency: MixEditorDependency
    @Inject lateinit var activityDependency: ActivityDependency
}

@MixEditorScope
@DependencyGraph(MixEditorComponentScope::class)
interface MixEditorGraph {

    @Provides
    @MixEditorScope
    fun provideDouble(): Double = 12.3

    @DependencyGraph.Factory
    interface Factory {
        fun create(
            @Includes serviceProvider: MixEditorServiceProvider
        ): MixEditorGraph
    }
}

@ContributesTo(AppScope::class)
interface MixEditorServiceProvider {
    val int: Int
}

@DependencyGraph(AppScope::class)
interface AppGraph {
    @Provides
    fun provideInt(): Int = 123
}

@ContributesTo(MixEditorActivityScope::class)
interface MixEditorActivityProviders {
    @Provides
    fun provideBoolean(): Boolean = true
}

@ActivityScope
@Inject
class ActivityDependency(
    val text: String,
    val int: Int,
    val boolean: Boolean,
    val double: Double
)

@MixEditorScope
@Inject
class MixEditorDependency(val int: Int)

fun box(): String {
    val appGraph = createGraph<AppGraph>()
    val meGraph = createGraphFactory<MixEditorGraph.Factory>().create(appGraph)
    val meActivity = MixEditorActivity()
    meActivity.setAppGraphAndInject(meGraph)
    assertEquals(123, meActivity.mixEditorDependency.int)
    assertEquals("CommonActivity", meActivity.activityDependency.text)
    assertEquals(123, meActivity.activityDependency.int)
    assertEquals(true, meActivity.activityDependency.boolean)
    assertEquals(12.3, meActivity.activityDependency.double)
    return "OK"
}
