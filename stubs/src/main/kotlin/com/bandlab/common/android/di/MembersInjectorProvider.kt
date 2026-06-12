package com.bandlab.common.android.di

import dev.zacsweers.metro.MembersInjector

interface MembersInjectorProvider<T : Any> {
    val injector: MembersInjector<T>
}