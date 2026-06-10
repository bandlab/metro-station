package com.bandlab.android.common.activity

abstract class CommonActivity<Params : Any> {

    lateinit var params: Params

    val type: String = "CommonActivity"

    open fun inject() {
        throw UnsupportedOperationException("This method will be implemented by the compiler plugin.")
    }

    interface ServiceProvider
}