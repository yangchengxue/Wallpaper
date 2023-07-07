package com.example.businessbase.views

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

class AsyncPhotoDrawable(
    private var mMeasuredWidth: Int = 0,
    private var mMeasuredHeight: Int = 0
) : Drawable(), Drawable.Callback {

    override fun getIntrinsicWidth(): Int {
        return mMeasuredWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mMeasuredHeight
    }

    override fun invalidateDrawable(p0: Drawable) {}

    override fun scheduleDrawable(p0: Drawable, p1: Runnable, p2: Long) {}

    override fun unscheduleDrawable(p0: Drawable, p1: Runnable) {}

    override fun draw(p0: Canvas) {}

    override fun setAlpha(p0: Int) {}

    override fun setColorFilter(p0: ColorFilter?) {}

    override fun getOpacity() = PixelFormat.UNKNOWN
}