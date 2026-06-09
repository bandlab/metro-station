package androidx.work

import android.content.Context

abstract class CoroutineWorker {
    fun getApplicationContext(): Context = Context.FAKE
    abstract suspend fun doWork(): Result
}