package com.example.wallpaper.adapters.vm

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.databinding.ObservableField
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.businessbase.views.ScalableImageView
import com.example.wallpaper.consts.AppConst.TAG

class ImagePreviewViewModel {

    var isLoadSuccess: ObservableField<Boolean> = ObservableField()

    fun showImage(photoView: ScalableImageView, url: String) {
        isLoadSuccess.set(false)
        Glide.with(photoView.context)
            .asBitmap()
            .load(url)
            // 不要缓存
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            // 要缓存
            /*.skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)*/
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(bmp: Bitmap, transition: Transition<in Bitmap>?) {
                    Log.i(TAG, "load url success, url=${url}")
                    // todo-ycx
                    photoView.init(bmp, 1150, 1210)
                    isLoadSuccess.set(true)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Log.i(TAG, "load url failed, url=${url}")
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }
}
