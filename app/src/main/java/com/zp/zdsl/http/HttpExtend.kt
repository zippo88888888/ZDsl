package com.zp.zdsl.http

import com.zp.zdsl.content.*
import kotlinx.coroutines.CoroutineScope

fun <T> BaseViewModel.http(block: HttpExtend<T>.() -> Unit) {
    val httpExtend = HttpExtend<T>(this)
    block.invoke(httpExtend)
}

fun BaseViewModel.http2(block: HttpExtend<Nothing>.() -> Unit) {
    http(block)
}

/** 一次发送一个 */
fun <T> HttpExtend<T>.request(startBlock: suspend CoroutineScope.() -> CommonBean<T>?) {
    start(startBlock)
}

/** 一次发送多个 */
/** 不能直接更新UI，先切换到UI线程再操作UI  --->>> UI { } */
fun HttpExtend<Nothing>.request2(startBlock: suspend CoroutineScope.() -> Unit) {
    start2(startBlock)
}

fun <T> HttpExtend<T>.loading(loaderBlock: () -> HttpLoader) {
    dialog(loaderBlock)
}

fun <T> HttpExtend<T>.success(resultBlock: T?.() -> Unit) {
    callback(resultBlock)
}

fun <T> HttpExtend<T>.failed(errorBlock: Exception?.() -> Unit) {
    error(errorBlock)
}

fun <T> HttpExtend<T>.finally(finaly: () -> Unit) {
    end(finaly)
}

class HttpExtend<T>(var viewModel: BaseViewModel) {

    private var httpLoader = HttpLoader()
    // 请求成功回调
    private var httpCallBack: (T?.() -> Unit)? = null
    private var httpError: (Exception?.() -> Unit)? = null
    private var httpFinally: (() -> Unit)? = null

    infix fun dialog(httpLoader: () -> HttpLoader) {
        this.httpLoader = httpLoader()
    }

    private fun showDialog() {
        if (httpLoader.showDialog) viewModel.showDialog.postValue(
            BaseViewModel.DialogRespBean(
                httpLoader.dialogTitle,
                httpLoader.dialogCancel
            )
        )
    }

    // 一次请求一个
    infix fun start(startBlock: suspend CoroutineScope.() -> CommonBean<T>?) {
        showDialog()
        viewModel.lunchByIO {
            try {
                val request = startBlock()
                UI {
                    httpCallBack?.invoke(request?.data)
                }
            } catch (e: Exception) {
                callError(e)
            } finally {
                callFinally()
            }
        }
    }

    // 一次请求多个
    infix fun start2(startBlock: suspend CoroutineScope.() -> Unit) {
        showDialog()
        viewModel.lunchByIO {
            try {
                startBlock()
            } catch (e: Exception) {
                callError(e)
            } finally {
                callFinally()
            }
        }
    }

    // 请求 回调
    infix fun callback(resultBlock: T?.() -> Unit) {
        httpCallBack = resultBlock
    }

    // 请求 失败 处理
    infix fun error(errorBlock: Exception?.() -> Unit) {
        httpError = errorBlock
    }

    // 不管请求是否成功或失败，都会调用
    infix fun end(end: () -> Unit) {
        httpFinally = end
    }

    // 处理异常  当然你也可以使用密封类
    private suspend fun callError(e: Exception) {
        e.printStackTrace()
        UI {
            // waitT 扩展函数抛出的异常关联
            val apiException = ApiException.formatException(e)
            // 具体根据业务逻辑而定
            when (apiException.errorCode) {
                401 -> {
                    L.e("http", "callError ---> Token失效 或 登录时间已过期")
                    viewModel.tokenError.postValue(apiException.msg)
                }
                -1001 -> {
                    L.e("http", "callError ---> 登录失败，请重新登录！")
                    viewModel.loginOut.postValue(apiException.msg)
                }
                888 -> {
                    // 当前账号在其他设备登录
                    L.e("http", "callError ---> 当前账号在其他设备登录")
                    viewModel.loginOut.postValue(apiException.msg)
                }
                else -> { // 一般的服务器请求失败处理
                    L.e("http", "callError ---> 请求失败")
                    viewModel.networkError.postValue(
                        BaseViewModel.NetErrorRespBean(
                            httpLoader.state,
                            apiException.msg
                        )
                    )
                    httpError?.invoke(apiException)
                }
            }
            viewModel.dismissDialog.clear() // 出现崩溃，不管如何都将dialog销毁
        }
    }

    // 最终执行
    private suspend fun callFinally() {
        UI {
            if (httpLoader.autoDismissDialog && httpLoader.showDialog) {
                viewModel.dismissDialog.clear()
            }
            httpFinally?.invoke()
        }
    }

}

