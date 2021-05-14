package com.zp.zdsl.http

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class LoadDialog(
    context: Context,
    private var title: String? = "加载中"
) : AlertDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wh = dip2px(136f)
        setContentView(getContentView(wh))
    }

    private fun getContentView(wh: Int) = LinearLayout(context).apply {
        window?.setLayout(wh, wh)
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER

        val barWh = dip2px(45f)
        val bar = ProgressBar(context).run {
            layoutParams = LinearLayout.LayoutParams(barWh, barWh)
            this
        }
        addView(bar)
        val padding = dip2px(14f)
        val titleTxt = TextView(context).run {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            setPadding(0, padding, 0, 0)
            textSize = 13f
            maxLines = 1
            setTextColor(Color.BLACK)
            text = title
            this
        }
        addView(titleTxt)
    }

    override fun dismiss() {
        System.gc()
        super.dismiss()
    }

    private fun dip2px(dpValue: Float) = (dpValue * context.resources.displayMetrics.density + 0.5f).toInt()
}