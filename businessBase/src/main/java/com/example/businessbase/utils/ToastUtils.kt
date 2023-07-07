package com.example.businessbase.utils

import android.content.Context
import android.view.Gravity
import android.widget.Toast

object ToastUtils {
    private var mToast: Toast? = null

    fun showToast(
        context: Context,
        text: CharSequence?,
        duration: Int = Toast.LENGTH_SHORT,
        gravity: Int = Gravity.CENTER
    ) {
        if (mToast != null) {
            mToast?.cancel()
            mToast = null
        }
        mToast = Toast.makeText(context, text, duration)
        mToast?.setGravity(gravity, 0, 0)
        mToast?.show()
    }
}
