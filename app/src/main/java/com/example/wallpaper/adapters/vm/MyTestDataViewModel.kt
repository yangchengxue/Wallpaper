package com.example.wallpaper.adapters.vm

import com.example.wallpaper.model.bean.WallpapersData

class MyTestDataViewModel(data: WallpapersData) {
    val type = data.type
    val str = data.paperUrl
    val name = data.paperName
}
