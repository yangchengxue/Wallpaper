package com.example.businessbase.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet

class ScalableImageView : ScalableBaseView {

    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyle: Int) : super(ctx, attrs, defStyle)

    private var mScale = 0f

    override fun onDraw(canvas: Canvas) {
        drawBigBitmap(canvas, mBitmap)
    }

    private fun updateScale() {
        val displayRect = imageDisplayRectF
        mScale =
            displayRect.width() / if ((mDrawableRotation + 180) % 180 == 0) mImageWidth else mImageHeight
        ImageGestureHelper.MathUtils.rectFGiven(displayRect)
    }

    private fun updateFinalScale() {
        val displayRect = imageFinalDisplayRectF
        mScale =
            displayRect.width() / if ((mDrawableRotation + 180) % 180 == 0) mImageWidth else mImageHeight
        ImageGestureHelper.MathUtils.rectFGiven(displayRect)
    }

    private fun drawBigBitmap(canvas: Canvas, bitmap: Bitmap?) {
        canvas.save()
        if (bitmap != null && !bitmap.isRecycled) {
            val mBitmapMatrix = drawCanvasMatrix
            val mPaintFlagsDrawFilter =
                PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawFilter = mPaintFlagsDrawFilter
            canvas.drawBitmap(bitmap, mBitmapMatrix, null)
            mBitmapMatrix.reset()
        }
        canvas.restore()
    }

    override fun onScroll(x: Float, y: Float) {
        updateScale()
    }

    override fun onDecodeFinalRegion() {
        updateFinalScale()
    }
}