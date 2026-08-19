// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.common.android.di

import dev.zacsweers.metro.MembersInjector

interface MembersInjectorProvider<T : Any> {
    val injector: MembersInjector<T>
}
