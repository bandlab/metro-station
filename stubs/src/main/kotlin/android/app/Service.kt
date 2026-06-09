package android.app

import android.content.ContextWrapper

abstract class Service : ContextWrapper() {
    open fun onCreate() = Unit
}