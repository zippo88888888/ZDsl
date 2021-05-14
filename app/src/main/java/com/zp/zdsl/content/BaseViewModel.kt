package com.zp.zdsl.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

open class BaseViewModel : ViewModel() {

    /**
     * Dialog 网络请求
     */
    data class DialogRespBean(
        var title: String? = "加载中",
        var isCancelable: Boolean = true
    )
    /**
     * 网络请求失败，响应到UI上的 Bean
     * @property state Int              区分刷新（加载）或加载更多
     * @property message String         错误描述
     * @constructor
     */
    data class NetErrorRespBean(
        var state: Int = 0,
        var message: String? = "网络不给力啊，再试一次吧"
    )

    /**
     * 显示dialog
     */
    val showDialog by lazy {
        SingLiveData<DialogRespBean>()
    }

    /**
     * 销毁dialog
     */
    val dismissDialog by lazy {
        SingLiveData<Void>()
    }

    /**
     * 网络请求错误
     */
    val networkError by lazy {
        SingLiveData<NetErrorRespBean>()
    }

    /**
     * 停止所有操作
     */
    val stopAll by lazy {
        SingLiveData<Void>()
    }

    /**
     * 当前账号在其他设备登录
     */
    val loginOut by lazy {
        SingLiveData<String?>()
    }

    /**
     * Token 失效 或 登录时间已过期
     */
    val tokenError by lazy {
        SingLiveData<String?>()
    }

    open fun lunchByIO(
        context: CoroutineContext = IO,
        block: suspend CoroutineScope.() -> Unit
    ) = viewModelScope.launch(context) { block() }


}