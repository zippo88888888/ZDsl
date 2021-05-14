package com.zp.zdsl.http

import androidx.collection.ArrayMap
import com.zp.zdsl.content.L
import com.zp.zdsl.content.URL
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val defaultApi: HttpApi
    get() {
        return HttpClient.getInstance().createDefaultApi()
    }

class HttpClient {

    private object Client{
        val builder = HttpClient()
    }

    companion object {
        fun getInstance() = Client.builder
    }

    /**
     * 缓存retrofit针对同一个域名下相同的ApiService不会重复创建retrofit对象
     */
    private val apiMap by lazy {
        ArrayMap<String, Any>()
    }

    private val API_KEY = "apiKey"

    private var interceptors = arrayListOf<Interceptor>()
    private var converterFactorys = arrayListOf<Converter.Factory>()

    /**
     * 拦截器
     */
    fun setInterceptors(list: MutableList<Interceptor>?) : HttpClient {
        interceptors.clear()
        if (!list.isNullOrEmpty()) {
            interceptors.addAll(list)
        }
        return this
    }

    /**
     * 解析器
     */
    fun setConverterFactorys(list: MutableList<Converter.Factory>?) : HttpClient {
        converterFactorys.clear()
        // 保证有一个默认的解析器
        converterFactorys.add(GsonConverterFactory.create())
        if (!list.isNullOrEmpty()) {
            converterFactorys.addAll(list)
        }
        return this
    }

    fun createDefaultApi() = createRetrofitApi(URL.ROOT_URL, HttpApi::class.java)

    /**
     * 根据 apiClass 与 baseUrl 创建 不同的Api
     * @param baseUrl String            根目录
     * @param clazz Class<T>            具体的api
     * @param needAddHeader Boolean     是否需要添加公共的头
     * @param showLog Boolean           是否需要显示log
     */
    fun <T> createRetrofitApi(
        baseUrl: String,
        clazz: Class<T>,
        needAddHeader: Boolean = true,
        showLog: Boolean = true
    ): T {
        val key = getApiKey(baseUrl, clazz)
        val api = apiMap[key] as T
        if (api == null) {
            L.e(API_KEY, "RetrofitApi --->>> \"$key\"不存在，需要创建新的")
            val builder = OkHttpClient.Builder()
            builder
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)

            if (needAddHeader) {
//                builder.addInterceptor(MyHttpInterceptor()) // 头部拦截器
            }
            if (interceptors.isNotEmpty()) {
                interceptors.forEach {
                    builder.addInterceptor(it)
                }
            }
            if (showLog) {
                builder.addInterceptor(HttpLoggingInterceptor {
                    L.i(API_KEY, it)
                }.apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
            val rBuilder = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(builder.build())
            if (converterFactorys.isEmpty()) { // 保证有一个默认的解析器
                converterFactorys.add(GsonConverterFactory.create())
            }
            converterFactorys.forEach {
                rBuilder.addConverterFactory(it)
            }
            val newAapi = rBuilder.build().create(clazz)
            apiMap[key] = newAapi
            return newAapi
        }
        return api
    }

    fun <K> getApiKey(baseUrl: String, apiClass: Class<K>) =
        "apiKey_${baseUrl}_${apiClass.name}"

    /**
     * 清空所有拦截器
     */
    fun clearInterceptor() : HttpClient {
        interceptors.clear()
        return this
    }

    /**
     * 清空所有解析器
     */
    fun clearConverterFactory() : HttpClient {
        converterFactorys.clear()
        return this
    }

    /**
     * 清空所有api缓存
     */
    fun clearAllApi() : HttpClient {
        L.e(API_KEY, "清空所有api缓存")
        apiMap.clear()
        return this
    }


}