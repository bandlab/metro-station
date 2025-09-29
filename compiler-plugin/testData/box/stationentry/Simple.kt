import kotlin.reflect.KClass

@StationEntry(parentScope = Unit::class)
class MyActivity

@StationEntry(
    parentScope = Unit::class,
    scope = Int::class
)
class MyFragment

fun box(): String {
    val factory = object : MyActivityGraphExtension {
        override fun inject(target: MyActivity) = Unit
    }
    return "OK"
}