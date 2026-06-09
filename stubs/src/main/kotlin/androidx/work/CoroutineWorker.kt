package androidx.work

abstract class CoroutineWorker {
    abstract suspend fun doWork(): Result
}