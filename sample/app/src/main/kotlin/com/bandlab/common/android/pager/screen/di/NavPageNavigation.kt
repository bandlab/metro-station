package com.bandlab.common.android.pager.screen.di

interface NavPageNavigation {

    companion object {
        val NOOP: NavPageNavigation = object : NavPageNavigation {}
    }
}
