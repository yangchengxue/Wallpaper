package com.example.businessbase.utils

import android.content.Context
import android.util.Log

object CommonUtil {

    private lateinit var applicationContext: Context
    var screenWidth = 0
    var screenHeight = 0
    // 屏幕比例（高/宽）
    var screenRatio = 0F

    fun init(context: Context) {
        applicationContext = context
        val displayMetrics = context.resources.displayMetrics
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
        screenRatio = screenHeight.toFloat() / screenWidth.toFloat()
        Log.i("WallpaperTAG", "CommonUtil init: screenWidth=$screenWidth screenHeight=$screenHeight screenRatio=${screenRatio}")
    }

    fun getAppContext() = applicationContext
}