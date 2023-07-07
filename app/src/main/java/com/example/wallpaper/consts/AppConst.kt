package com.example.wallpaper.consts

import android.os.Environment

object AppConst {
    const val TAG = "WallpaperTAG"
    const val BASE_PATH = "壁纸"
    const val WALLPAPER_SPAN_COUNT = 3
    const val WALLPAPER_INTERVAL_SPAN_COUNT = 8
    const val WALLPAPER_INTERVAL_PROPORTION = 0.01

    const val ARG_IMAGE_URL = "ARG_IMAGE_URL"

    object DataLoadState {
        const val LOADING = "LOADING"
        const val SUCCESS = "SUCCESS"
        const val NO_CONTENT = "NO_CONTENT"
        const val FAIL = "FAIL"
    }
}