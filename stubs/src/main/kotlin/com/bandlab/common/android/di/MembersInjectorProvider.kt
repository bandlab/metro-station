// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.common.android.di

import dev.zacsweers.metro.MembersInjector

/**
 * The base interface for `@StationEntry` graph extensions. `@MetroStation` graphs declare a
 * generated `inject(target)` function instead.
 */
interface MembersInjectorProvider<T : Any> {
    val injector: MembersInjector<T>
}
