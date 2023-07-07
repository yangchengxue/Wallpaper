package com.example.wallpaper.adapters.diff

import androidx.recyclerview.widget.DiffUtil
import com.example.wallpaper.model.bean.ImagePreviewBean

class ImagePreviewDiffItemCallBack : DiffUtil.ItemCallback<ImagePreviewBean>() {

    override fun areItemsTheSame(
        oldItem: ImagePreviewBean,
        newItem: ImagePreviewBean
    ): Boolean {
        return oldItem.url == newItem.url
    }

    override fun areContentsTheSame(
        oldItem: ImagePreviewBean,
        newItem: ImagePreviewBean
    ): Boolean {
        return oldItem.url == newItem.url
    }
}