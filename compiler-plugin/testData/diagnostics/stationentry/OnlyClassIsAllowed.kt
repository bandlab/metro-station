// RUN_PIPELINE_TILL: FRONTEND

@StationEntry(parentScope = Unit::class)
interface MyInterface

@StationEntry(parentScope = Unit::class)
abstract class MyAbstractClass

@StationEntry(parentScope = Unit::class)
object MyObject

@StationEntry(parentScope = Unit::class)
enum class MyEnum

@StationEntry(parentScope = Unit::class)
class MyClass