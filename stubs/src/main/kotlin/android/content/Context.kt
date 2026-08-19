// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package android.content

abstract class Context {

    companion object {
        val FAKE = object : Context() {}
    }
}
