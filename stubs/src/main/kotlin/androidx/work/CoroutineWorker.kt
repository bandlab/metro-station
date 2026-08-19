// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package androidx.work

import android.content.Context

abstract class CoroutineWorker {
    fun getApplicationContext(): Context = Context.FAKE

    abstract suspend fun doWork(): Result
}
