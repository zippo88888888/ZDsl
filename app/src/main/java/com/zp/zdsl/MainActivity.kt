package com.zp.zdsl

import com.zp.zdsl.content.*
import com.zp.zdsl.http.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity<MainViewModel>() {

    override fun getContentId() = R.layout.activity_main

    override fun initAll() {
        main_oneBtn.setOnClickListener {
            vm?.one()
        }

        main_oneByOneBtn.setOnClickListener {
            vm?.oneByOne()
        }

        vm?.apply {
            oneSuccess.changeUI(this@MainActivity) {
                main_oneBtn.text = String.format("list.size ---> %s", this?.size)
            }
            oneByOneSuccess.changeUI(this@MainActivity) {
                main_oneByOneBtn.text = "one-By-One-Success"
            }
        }
    }

    override fun stopAll() {
        main_oneByOneBtn.text = "stopAll"
    }
}

class MainViewModel : BaseViewModel() {

    val oneSuccess by lazy {
        SingLiveData<MutableList<T1>>()
    }

    val oneByOneSuccess by lazy {
        SingLiveData<Void>()
    }

    private val model by lazy {
        MainModel()
    }

    fun one() {
        http<MutableList<T1>> {
            request { model.t1().waitT() }
            success { oneSuccess.postValue(this) }
        }
    }

    fun oneByOne() {
        http2 {
            loading { HttpLoader(showDialog = false, autoDismissDialog = false) }
            request2 {
                val t1 = model.t1().waitT()
                val t2 = model.t2().waitT()
                val t3 = model.t3().waitT()
                UI {
                    // 模拟 请求成功
                    if (!t1.data.isNullOrEmpty()) {
                        L.i("t2 --- ${t2.data?.size}")
                        L.i("t3 --- ${t3.data?.size}")
                        oneByOneSuccess.clear()
                    } else {
                        stopAll.clear()
                    }
                }
            }
            failed {
                // 可以省略，base已经实现
//                networkError.postValue(NetErrorRespBean())

                // do sth
            }
            finally {
                // do sth
            }
        }
    }

}

class MainModel() {

    fun t1() = defaultApi.get1()
    fun t2() = defaultApi.get2()
    fun t3() = defaultApi.get3()

}