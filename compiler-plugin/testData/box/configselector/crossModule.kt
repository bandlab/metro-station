import com.bandlab.config.api.ContributesConfigSelector
import com.bandlab.config.api.DebuggableConfigSelector

// MODULE: lib
@ContributesConfigSelector
object FooConfigSelector : DebuggableConfigSelector

// MODULE: main(lib)
import com.bandlab.config.api.ContributesConfigSelector
import com.bandlab.config.api.DebuggableConfigSelector

@ContributesConfigSelector
object BarConfigSelector : DebuggableConfigSelector

@DependencyGraph(AppScope::class)
interface AppGraph {
    val configs: Set<DebuggableConfigSelector>
}

fun box(): String {
    val graph = createGraph<AppGraph>()
    assertTrue(FooConfigSelector in graph.configs)
    assertTrue(BarConfigSelector in graph.configs)
    return "OK"
}
