@ContributesConfigSelector
object FooConfigSelector : DebuggableConfigSelector

@DependencyGraph(AppScope::class)
interface AppGraph {
    val configs: Set<DebuggableConfigSelector>
}

fun box(): String {
    val graph = createGraph<AppGraph>()
    assertTrue(FooConfigSelector in graph.configs)
    return "OK"
}
