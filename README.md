
#### 优雅使用
```Kotlin

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

    /** 一次一个请求 */
    fun one() {
        http<MutableList<T1>> {
            request { model.t1().waitT() }
            success { oneSuccess.postValue(this) }
        }
    }

    /** 一次多个请求 */
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
                // do sth
            }
            finally {
                // do sth
            }
        }
    }

}

```

#### 感谢[玩Android](https://wanandroid.com/)提供api