package android.content

abstract class ContextWrapper : Context() {
    fun getApplicationContext(): Context = this
}