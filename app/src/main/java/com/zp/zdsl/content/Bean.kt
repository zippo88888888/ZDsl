package com.zp.zdsl.content

import android.os.Parcelable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.zp.zdsl.http.ApiException
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.lang.reflect.ParameterizedType
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Parcelize
data class CommonBean<T>(
    var data: @RawValue T? = null,
    var errorCode: Int = 0,
    var errorMessage: String = "网络不给力，再试一次吧！"
) :Serializable, Parcelable


@Parcelize
data class T1(
    var courseId: String = "",
    var id: String = "",
    var name: String = "",
    var order: String = "",
    var parentChapterId: String = "",
    var userControlSetTop: String = "",
    var visible: String = "",
):Serializable, Parcelable

@Parcelize
data class T2(
    var desc: String = "",
    var id: String = "",
    var imagePath: String = "",
    var order: String = "",
    var title: String = "",
    var type: String = "",
    var url: String = "",
    var visible: String = "",
):Serializable, Parcelable

@Parcelize
data class T3(
    var link: String = "",
    var id: String = "",
    var name: String = "",
    var order: String = "",
    var visible: String = "",
):Serializable, Parcelable

suspend fun <T> Call<CommonBean<T>>.waitT(): CommonBean<T> {
    return suspendCoroutine {

        enqueue(object : Callback<CommonBean<T>> {

            override fun onResponse(
                call: Call<CommonBean<T>>,
                response: Response<CommonBean<T>>
            ) {
                val body = response.body()
                if (body is ResponseBody) { // ResponseBody情况
                    if (response.isSuccessful) {
                        it.resume(body)
                    } else {
                        it.resumeWithException(ApiException("网络不给力啊，再试一次吧"))
                    }
                } else { // 其他实体类的情况
                    if (response.isSuccessful) { // 请求成功
                        val isSuccess = body?.errorCode == 0 // 业务逻辑OK
                        if (body != null && isSuccess) { // 业务逻辑OK
                            it.resume(body)
                        } else if (body?.errorCode == -1001) { // 请求成功 但是code -1001 单独处理
                            it.resumeWithException(ApiException("登录失效，需要重新登录", -1001))
                        } else { // 请求成功 但是业务逻辑是不对的
                            it.resumeWithException(ApiException("网络不给力啊，再试一次吧"))
                        }
                    } else { // 服务器抛出异常的情况，具体根据业务逻辑进行判定，如我这里必须需要单独处理401的异常
                        if (response.code() == 401) {
                            // 服务器直接抛出 401异常，手动处理
                            it.resumeWithException(ApiException("当前账号在其他设备登录", 401))
                        } else {
                            it.resumeWithException(ApiException("网络不给力啊，再试一次吧"))
                        }

                    }
                }
            }

            override fun onFailure(call: Call<CommonBean<T>>, t: Throwable) {
                t.printStackTrace()
                L.e("http", "onFailure 接口异常 ---> ${call.request().url()}")
                it.resumeWithException(ApiException.formatException(t))
            }
        })
    }
}


val IO: CoroutineContext
    get() {
        return Dispatchers.IO
    }

val Main: CoroutineContext
    get() {
        return Dispatchers.Main
    }

/**
 * 切换到 IO 线程
 */
suspend fun IO(block: suspend CoroutineScope.() -> Unit) {
    withContext(IO) {
        block()
    }
}

/**
 * 切换到 UI 线程
 */
suspend fun UI(block: suspend CoroutineScope.() -> Unit) {
    withContext(Main) {
        block()
    }
}

@Parcelize
data class HttpLoader(
    var state: Int = 0, // 用来区分是刷新还是加载更多
    var showDialog: Boolean = true, // 请求是否显示 dialog
    var autoDismissDialog: Boolean = true, // 请求成功后是否自动销毁dialog
    var dialogTitle: String = "加载中", // dialog title
    var dialogCancel: Boolean = true // dialog 是否可以取消
) : Parcelable

fun <VM> getViewModelClass(obj: Any): VM =
    (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as VM

fun <T> MutableLiveData<T>.changeUI(owner: LifecycleOwner, block: T?.() -> Unit) {
    observe(owner, {
        block.invoke(it)
    })
}
