package com.zp.zdsl.content

import android.util.Log

object L {

    private const val TAG = "APP_LOG"
    private const val I = 1
    private const val E = 3

    fun i(msg: String) {
        i(TAG, msg)
    }

    fun e(msg: String) {
        e(TAG, msg)
    }

    fun i(tag: String, message: String) {
        log(I, tag, message)
    }

    fun e(tag: String, message: String) {
        log(E, tag, message)
    }


    private fun log(type: Int, TAG: String, value: String = "null") {
        when (type) {
            E -> Log.e(TAG, value)
            I -> Log.i(TAG, value)
        }
    }

}