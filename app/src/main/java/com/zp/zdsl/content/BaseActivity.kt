package com.zp.zdsl.content

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.zp.zdsl.http.LoadDialog

abstract class BaseActivity<VM : BaseViewModel> : AppCompatActivity() {

    protected var vm: VM? = null
    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentId())
        vm = ViewModelProvider(this).get(getViewModelClass(this) as Class<VM>)
        registerDefault()
        initAll()
    }

    abstract fun getContentId():Int

    abstract fun initAll()

    private fun createDialog(title: String?, isCancelable: Boolean) =
        LoadDialog(this, title).apply {
            setCancelable(isCancelable)
            setCanceledOnTouchOutside(false)
        }

    /**
     * 显示 Dialog
     */
    open fun showViewDialog(title: String?, isCancelable: Boolean) {
        if (dialog != null) {
            dialog?.dismiss()
            dialog = null
        }
        dialog = createDialog(title, isCancelable)
        dialog?.show()
    }

    /**
     * 销毁 Dialog
     */
    open fun dissmissDialog() {
        if (dialog != null && dialog?.isShowing == true) {
            dialog?.dismiss()
            dialog = null
        }
    }

    open fun stopAll() = Unit

    open fun loginOut(msg: String?) {
        L.e(msg ?: "当前账号在其他设备登录")
    }

    open fun tokenError(msg: String?) {
        L.e(msg ?: "Token 失效 或 登录时间已过期")
    }


    open fun reqFailed(msg: String?) {
        L.e(msg ?: "reqFailed")
    }

    open fun reqLoadMoreFailed(msg: String?) {
        L.e(msg ?: "reqLoadMoreFailed")
    }


    private fun registerDefault() {
        val that = this
        vm?.apply {
            showDialog.changeUI(that) {
                if (this == null) return@changeUI
                showViewDialog(title, isCancelable)
            }
            dismissDialog.changeUI(that) {
                dissmissDialog()
            }
            loginOut.changeUI(that) {
                loginOut(this)
            }
            tokenError.changeUI(that) {
                tokenError(this)
            }
            stopAll.changeUI(that) {
                stopAll()
            }
            networkError.changeUI(that) {
                if (this == null) return@changeUI
                when (state) {
                    0 -> reqFailed(message)
                    1 -> reqLoadMoreFailed(message)
                }
            }
        }
    }

}