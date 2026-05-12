package android.content

abstract class Context {

    companion object {
        val FAKE = object : Context() {}
    }
}