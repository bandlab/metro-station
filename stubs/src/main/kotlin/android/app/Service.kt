// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package android.app

import android.content.ContextWrapper

abstract class Service : ContextWrapper() {
    open fun onCreate() = Unit
}
