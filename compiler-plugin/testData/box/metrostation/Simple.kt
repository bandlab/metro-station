@MetroStation
class MyActivity

@MetroStation(scope = Int::class)
class MyFragment

fun box(): String {
    val factory = object : MyActivityDependencyGraph {
        override fun inject(target: MyActivity) = Unit
    }
    return "OK"
}