package com.bandlab.android.common

abstract class CommonActivity<Params : Any> {

    lateinit var params: Params

    interface ServiceProvider
}