package com.zp.zdsl.http

import retrofit2.HttpException
import java.net.SocketTimeoutException

// 异常格式化
class ApiException : Exception {

    var msg: String? = "网络不给力啊，再试一次吧！"
    var errorCode = _500_SERVICE

    private constructor(code: Int, throwable: Throwable) : super(throwable) {
        msg = throwable.message
        errorCode = code
    }

    constructor(message: String?, code: Int = _500_SERVICE) : super(message) {
        msg = message
        errorCode = code
    }

    companion object {

        const val _500_SERVICE = 500
        const val UNKNOW_ERROR = -1

        fun formatException(e: Throwable): ApiException {
            val apiException: ApiException
            when (e) {
                is HttpException -> {
                    apiException = ApiException(e.code(), e)
                    apiException.msg = "网络不给力啊，再试一次吧！"
                }
                is SocketTimeoutException -> {
                    apiException = ApiException("网络不给力啊，再试一次吧！", _500_SERVICE)
                }
                //    ... 其他异常处理
                is ApiException -> {
                    apiException = e
                }
                else -> {
                    apiException = ApiException(UNKNOW_ERROR, e)
                    apiException.msg = "未知异常，请联系管理员"
                }
            }
            return apiException
        }

    }
}

