package com.bandlab.android.common.activity

abstract class CommonActivity<Params : Any> {

    lateinit var params: Params

    val type: String = "CommonActivity"

    interface ServiceProvider
}