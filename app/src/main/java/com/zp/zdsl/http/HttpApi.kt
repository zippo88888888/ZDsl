package com.zp.zdsl.http

import com.zp.zdsl.content.*
import retrofit2.Call
import retrofit2.http.GET

interface HttpApi {

    @GET(URL.T1)
    fun get1() : Call<CommonBean<MutableList<T1>>>

    @GET(URL.T2)
    fun get2() : Call<CommonBean<MutableList<T2>>>

    @GET(URL.T3)
    fun get3() : Call<CommonBean<MutableList<T3>>>

}