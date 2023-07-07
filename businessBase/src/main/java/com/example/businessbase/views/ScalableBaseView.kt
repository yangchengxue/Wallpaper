package com.example.businessbase.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView

abstract class ScalableBaseView : AppCompatImageView, IPhotoView,
    ImageGestureHelper.OuterGestureListener {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyle: Int) : super(ctx, attrs, defStyle)

    protected var mDrawableRotation = 0
    protected var mImageWidth = SIZE_UNKNOWN
    protected var mImageHeight = SIZE_UNKNOWN

    /**
     * 是否拦截 Touch 事件
     */
    var isInterceptTouchEvent = false

    private val mListener: ImageGestureHelper by lazy {
        ImageGestureHelper(
            context,
            this
        )
    }

    var mBitmap: Bitmap? = null

    open fun init(bitmap: Bitmap, width: Int, height: Int) {
        mBitmap = bitmap
        initDrawableRotation()
        setAllowScale(true)
        setAllowRotate(false)
        isEnabled = true
        isActivated = true
        scaleType = ScaleType.FIT_CENTER
        setDrawable(width, height)
        mListener.setOuterGestureListener(this)
    }

    private fun setDrawable(width: Int, height: Int) {
        val drawable = AsyncPhotoDrawable(width, height)
        setImageDrawable(drawable)
    }

    fun initDrawableRotation() {
        mDrawableRotation = 0
    }

    /**
     * 允许图片手势缩放
     */
    fun setAllowScale(allowScale: Boolean) {
        mListener.setSupportScale(allowScale)
    }

    /**
     * 允许图片手势旋转
     */
    fun setAllowRotate(allowRotate: Boolean) {
        mListener.setSupportRotation(allowRotate)
    }

    val drawCanvasMatrix: Matrix
        get() {
            val matrix = ImageGestureHelper.MathUtils.matrixTake()
            mListener.getCurrentImageMatrix(matrix)
            return matrix
        }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (isInterceptTouchEvent) {
            false
        } else mListener.onTouchEvent(event)
    }

    /**
     * 重置显示画面
     */
    fun resetDisplayRect(animate: Boolean) {
        mListener.resetDisplayRect(animate)
    }

    /**
     * 是否处于手势状态
     */
    val isGestureState: Boolean
        get() = mListener.isGestureState

    /**
     * 设置点击监听器
     */
    var tapListener: TabListener? = null

    override fun isReady(): Boolean {
        return drawable != null && drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0 && width > 0 && height > 0
    }

    override fun vibrate(effectId: Int) {}

    override fun getImageRectF(): RectF {
        val rectF = ImageGestureHelper.MathUtils.rectFTake()
        if (mBitmap != null && !mBitmap!!.isRecycled) {
            rectF[0f, 0f, mBitmap!!.width.toFloat()] = mBitmap!!.height.toFloat()
        } else if (drawable != null) {
            rectF[0f, 0f, drawable.intrinsicWidth.toFloat()] = drawable.intrinsicHeight.toFloat()
        } else {
            rectF[0f, 0f, width.toFloat()] = height.toFloat()
        }
        return rectF
    }

    /**
     * @return 经过转换后的图片显示区域
     */
    override fun getImageDisplayRectF(): RectF {
        val rectF = ImageGestureHelper.MathUtils.rectFTake()
        val matrix = ImageGestureHelper.MathUtils.matrixTake()
        mListener.getCurrentImageMatrix(matrix)
        val src = imageRectF
        matrix.mapRect(rectF, src)
        val offsetY = (parent as ViewGroup).top
        rectF.offset(0f, offsetY.toFloat())
        ImageGestureHelper.MathUtils.matrixGiven(matrix)
        ImageGestureHelper.MathUtils.rectFGiven(src)
        return rectF
    }

    val imageFinalDisplayRectF: RectF
        get() {
            val rectF = ImageGestureHelper.MathUtils.rectFTake()
            val matrix = ImageGestureHelper.MathUtils.matrixTake()
            mListener.getFinalImageMatrix(matrix)
            val src = imageRectF
            matrix.mapRect(rectF, src)
            ImageGestureHelper.MathUtils.matrixGiven(matrix)
            ImageGestureHelper.MathUtils.rectFGiven(src)
            return rectF
        }

    /**
     * 获取内部变换矩阵.
     *
     * 内部变换矩阵是原图到fit center状态的变换,当原图尺寸变化或者控件大小变化都会发生改变
     * 当尚未布局或者原图不存在时,其值无意义.所以在调用前需要确保前置条件有效,否则将影响计算结果.
     *
     * @param matrix 用于填充结果的对象
     * @return 如果传了matrix参数则将matrix填充后返回, 否则new一个填充返回
     */
    override fun getInnerMatrix(matrix: Matrix): Matrix {
        var matrix: Matrix? = matrix
        if (matrix == null) {
            matrix = Matrix()
        } else {
            matrix.reset()
        }
        if (isReady) {
            //原图大小
            val tempSrc: RectF
            var drawBig = false
            if (mBitmap != null && !mBitmap!!.isRecycled) {
                drawBig = true
                tempSrc = ImageGestureHelper.MathUtils.rectFTake(
                    0f,
                    0f,
                    mBitmap!!.width.toFloat(),
                    mBitmap!!.height.toFloat()
                )
            } else {
                tempSrc = ImageGestureHelper.MathUtils.rectFTake(
                    0f,
                    0f,
                    drawable.intrinsicWidth.toFloat(),
                    drawable.intrinsicHeight.toFloat()
                )
            }

            //控件大小
            val tempDst =
                ImageGestureHelper.MathUtils.rectFTake(0f, 0f, width.toFloat(), height.toFloat())
            //计算fit center矩阵
            matrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.CENTER)
            if (drawBig) {
                val tempMatrix = ImageGestureHelper.MathUtils.matrixTake()

                // 旋转角度
                matrix.postRotate(
                    mDrawableRotation.toFloat(),
                    (width / 2).toFloat(),
                    (height / 2).toFloat()
                )

                // 获得旋转后的显示区域
                matrix.mapRect(tempSrc)

                // 再做一次fit center的矩阵变换
                tempMatrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.CENTER)

                // 将两个矩阵拼接起来
                matrix.postConcat(tempMatrix)
                ImageGestureHelper.MathUtils.matrixGiven(tempMatrix)
            }

            //释放临时对象
            ImageGestureHelper.MathUtils.rectFGiven(tempDst)
            ImageGestureHelper.MathUtils.rectFGiven(tempSrc)
        }
        return matrix
    }

    override fun onSingleTapConfirmed(e: MotionEvent?) {
        tapListener?.onSingleTapUp()
    }

    override fun onDoubleTap() {
        tapListener?.onDoubleTap()
    }

    override fun onScaleLarge() {
        tapListener?.onScaleLarge()
    }

    override fun onScaleBegin() {}

    override fun onScroll(x: Float, y: Float) {}

    override fun onScaleEnd() {}

    override fun onDecodeFinalRegion() {}

    override fun onRotationEnd(rotation: Int) {
        mDrawableRotation = (rotation + mDrawableRotation) % 360
    }

    override fun getViewWidth(): Int {
        return width
    }

    override fun getViewHeight(): Int {
        return height
    }

    override fun onOuterMatrixChanged() {
        invalidate()
    }

    companion object {
        const val SIZE_UNKNOWN = -1
    }
}