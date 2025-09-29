// RUN_PIPELINE_TILL: FRONTEND

@StationEntry(parentScope = Unit::class)
interface <!STATION_ENTRY_NOT_ON_CLASS!>MyInterface<!>

@StationEntry(parentScope = Unit::class)
abstract class <!STATION_ENTRY_NOT_ON_CLASS!>MyAbstractClass<!>

@StationEntry(parentScope = Unit::class)
object <!STATION_ENTRY_NOT_ON_CLASS!>MyObject<!>

@StationEntry(parentScope = Unit::class)
enum class <!STATION_ENTRY_NOT_ON_CLASS!>MyEnum<!>

@StationEntry(parentScope = Unit::class)
class MyClass

/* GENERATED_FIR_TAGS: classDeclaration, classReference, enumDeclaration, interfaceDeclaration, objectDeclaration */
