package com.example.wallpaper.app

import android.app.Application
import com.example.businessbase.utils.CommonUtil
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        CommonUtil.init(this)
    }
}