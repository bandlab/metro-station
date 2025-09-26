@StationEntry
class MyActivity

fun box(): String {
    val factory = object : MyActivityGraphExtension {}
    return "OK"
}