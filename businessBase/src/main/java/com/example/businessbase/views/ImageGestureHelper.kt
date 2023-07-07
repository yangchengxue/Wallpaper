package com.example.businessbase.views

import android.view.MotionEvent
import android.graphics.PointF
import android.os.Looper
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.ScaleGestureDetector
import android.view.ViewConfiguration
import android.graphics.RectF
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.animation.TimeInterpolator
import android.R.attr
import android.animation.Animator
import android.content.Context
import android.graphics.Matrix
import android.os.Handler
import android.util.Log
import android.view.GestureDetector
import java.util.*

class ImageGestureHelper(context: Context, attachView: IPhotoView) {
    private val mFinalMatrix = Matrix()

    /**
     * 外部矩阵变化事件通知监听器
     */
    interface OuterGestureListener {
        fun onOuterMatrixChanged()
        fun onScaleBegin()
        fun onScaleEnd()
        fun onScroll(x: Float, y: Float)
        fun onDecodeFinalRegion()
        fun onRotationEnd(rotation: Int)
        fun onSingleTapConfirmed(e: MotionEvent?)
        fun onDoubleTap()
        fun onScaleLarge()
    }

    /**
     * 外层变换矩阵，如果是单位矩阵，那么图片是fit center状态
     */
    val outerMatrix = Matrix()

    /**
     * 当前手势状态
     *
     * @see .PINCH_MODE_FREE
     *
     * @see .PINCH_MODE_SCROLL
     *
     * @see .PINCH_MODE_SCALE
     */
    private var mPinchMode = PINCH_MODE_FREE
    private val mLastVector = PointF()
    private val mCurrentVector = PointF()
    private var mOverScrollType = OVER_SCROLL_NONE
    private var isInOverScroll = false
    private val mLastMovePoint = PointF()
    private val mDownPoint = PointF()
    private val mLastPoint = PointF()
    private var isSupportScale = true
    private var isSupportRotation = true
    private val mTouchSlop: Int
    private var isScaling = false
    private var mScale = 1f
    private var mLastScale = 1f
    private var mBeginScale = 1f
    private var mDegree = 0f
    private var mTempDegree = 0f

    // 两个手指中如果有一个手指抬上来，执行actionUp操作
    private var forceActionUp = false

    /**
     * 图片缩放动画
     *
     *
     * 缩放模式把图片的位置大小超出限制之后触发.
     * 双击图片放大或缩小时触发.
     * 手动调用outerMatrixTo触发.
     */
    private var mScaleAnimator: ScaleAnimator? = null

    /**
     * 滑动产生的惯性动画
     */
    private var mFlingAnimator: FlingAnimator? = null
    private var isFling = false
    private var mOuterGestureListener: OuterGestureListener? = null
    private var mExtOuterGestureListener: OuterGestureListener? = null
    private val mHandler = Handler(Looper.getMainLooper())
    private var mSingleTapUpRunnable: SingleTapUpRunnable? = null

    private inner class SingleTapUpRunnable(private val event: MotionEvent) : Runnable {
        override fun run() {
            if (mOuterGestureListener != null) {
                mOuterGestureListener!!.onSingleTapConfirmed(event)
            }
        }
    }

    var mGestureListener: GestureDetector.SimpleOnGestureListener =
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                //只有在单指模式结束之后才允许执行fling
                if (mPinchMode == PINCH_MODE_FREE && !(mScaleAnimator != null && mScaleAnimator!!.isRunning)) {
                    Log.d(TAG, "onFling: ")
                    isFling = true
                    val factor = 4f
                    val velocity = Math.max(Math.abs(velocityX), Math.abs(velocityY))
                    val duration = (FLING_COASTING_DURATION_S
                            * Math.pow(
                        velocity.toDouble(),
                        (1f / (factor - 1f)).toDouble()
                    )).toFloat()
                    fling(velocityX, velocityY, (duration * 1000).toLong())
                }
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                Log.d(TAG, "onDoubleTap: ")
                //当手指快速第二次按下触发,此时必须是单指模式才允许执行doubleTap
                if (mPinchMode == PINCH_MODE_SCROLL && !(mScaleAnimator != null && mScaleAnimator!!.isRunning)) {
                    doubleTap(e.x, e.y)
                }
                if (mSingleTapUpRunnable != null) {
                    mHandler.removeCallbacks(mSingleTapUpRunnable!!)
                }
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                Log.i(TAG, "onSingleTapConfirmed: ")
                // 过滤掉长按事件
                return if (e.eventTime - e.downTime > TAP_TIMEOUT) {
                    true
                } else true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                Log.d(TAG, "onSingleTapUp: ")
                mSingleTapUpRunnable = SingleTapUpRunnable(e)
                mHandler.postDelayed(mSingleTapUpRunnable!!, DOUBLE_TAP_TIME_OUT.toLong())
                return true
            }

            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                return super.onScroll(e1, e2, distanceX, distanceY)
            }
        }
    private val mScaleListener: OnScaleGestureListener = object : OnScaleGestureListener {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val startScale = mLastScale
            val endScale = mBeginScale * scaleFactor
            val scale = endScale / startScale
            if (endScale > 1) {
                if (mOuterGestureListener != null) {
                    mOuterGestureListener!!.onScaleLarge()
                }
                if (checkMaxScale() && endScale > MAX_SCALE) {
                    return false
                }
            }
            if (scale != 1f && mLastScale > MIN_SCALE) {
                mLastScale = endScale
                mScale = mLastScale
                outerMatrix.postScale(scale, scale, detector.focusX, detector.focusY)
                dispatchOuterMatrixChanged()
            }
            return false
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isScaling = true
            mLastScale = mScale
            mBeginScale = mLastScale
            Log.d(TAG, "onScaleBegin: mBeginScale = $mBeginScale")
            if (mExtOuterGestureListener != null) {
                mExtOuterGestureListener!!.onScaleBegin()
            }
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            Log.d(TAG, "onScaleEnd: ")
            isScaling = false
        }
    }
    private val mGestureDetector by lazy { GestureDetector(context, mGestureListener) }
    private val mScaleDetector: ScaleGestureDetector
    private val mPhotoView: IPhotoView

    init {
        mScaleDetector = ScaleGestureDetector(context, mScaleListener)
        // 双指缩放到一定程度才会触发onScaleBegin回调，降低该值可提高灵敏度
        ReflectHelper.setField(mScaleDetector, "mMinSpan", 50)
        mPhotoView = attachView
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    protected fun checkMaxScale(): Boolean {
        return false
    }

    private fun setPinchMode(pinchMode: Int) {
        mPinchMode = pinchMode
    }

    fun setOverScrollType(scrollType: Int) {
        mOverScrollType = scrollType
    }

    private val isReady: Boolean
        private get() = mPhotoView.isReady
    private val width: Int
        private get() = mPhotoView.viewWidth
    private val height: Int
        private get() = mPhotoView.viewHeight
    private val imageRectF: RectF
        private get() = mPhotoView.imageRectF

    private fun vibrate(effectId: Int) {
        mPhotoView.vibrate(effectId)
    }

    val isZoomAt: Boolean
        get() = mScale - 1 > 0.001
    val isInitialState: Boolean
        get() = outerMatrix.isIdentity
    val isGestureState: Boolean
        get() {
            val outerScale = MathUtils.getMatrixScale(
                outerMatrix
            )
            return !MathUtils.isAlmostEquals(outerScale, 1f)
        }

    fun setSupportScale(supportScale: Boolean) {
        isSupportScale = supportScale
    }

    fun setSupportRotation(supportRotation: Boolean) {
        isSupportRotation = supportRotation
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action and MotionEvent.ACTION_MASK
        var handled = true
        if (action == MotionEvent.ACTION_POINTER_UP) {
            forceActionUp = true
            onActionUp(event)
        } else if (action == MotionEvent.ACTION_UP) {
            if (forceActionUp) {
                forceActionUp = false
            } else {
                onActionUp(event)
            }
        } else if (action == MotionEvent.ACTION_CANCEL) {
            onActionUp(event)
        } else if (action == MotionEvent.ACTION_DOWN) {
            onActionDown(event)
            //非第一个点按下，关闭滚动模式，开启缩放模式，记录缩放模式的一些初始数据
        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
            onActionPointerDown(event)
        } else if (action == MotionEvent.ACTION_MOVE) {
            handled = onActionMove(event)
        }
        if (isSupportScale && event.pointerCount > 1) {
            mScaleDetector.onTouchEvent(event)
        }
        mGestureDetector.onTouchEvent(event)
        return handled
    }

    private fun onActionDown(event: MotionEvent) {
        forceActionUp = false
        mTempDegree = 0f
        mDegree = mTempDegree
        mDownPoint[event.x] = event.y
        //在矩阵动画过程中不允许启动滚动模式
        if (!(mScaleAnimator != null && mScaleAnimator!!.isRunning)) {
            //停止所有动画
            cancelAllAnimator()
            //切换到滚动模式
            setPinchMode(PINCH_MODE_SCROLL)
            //保存触发点用于move计算差值
            mLastMovePoint[event.x] = event.y
        }
    }

    private fun onActionMove(event: MotionEvent): Boolean {
        if (mPinchMode == PINCH_MODE_FREE && event.pointerCount > 1) {
            setPinchMode(PINCH_MODE_SCALE)
        }
        if (mPinchMode == PINCH_MODE_SCROLL && Math.abs(event.x - mDownPoint.x) < mTouchSlop && Math.abs(
                event.y - mDownPoint.y
            ) < mTouchSlop
        ) {
            return true
        }
        if (!(mScaleAnimator != null && mScaleAnimator!!.isRunning)) {
            //在滚动模式下移动
            if (mPinchMode == PINCH_MODE_SCROLL) {
                //每次移动产生一个差值累积到图片位置上
                val handled =
                    scrollBy(event.x - mLastMovePoint.x, event.y - mLastMovePoint.y, false)
                //记录新的移动点
                mLastMovePoint[event.x] = event.y
                return handled
            } else if (mPinchMode == PINCH_MODE_SCALE && event.pointerCount > 1) {

                // 检测双指移动
                detectScaleMove(event)

                // 检测旋转
                detectRotation(event)
            }
        }
        return true
    }

    /**
     * 检测双指移动，当图片没有超过View边界的时候不让移动
     *
     * @param event
     */
    private fun detectScaleMove(event: MotionEvent) {
        val lineCenter =
            MathUtils.getCenterPoint(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
        val translateX = lineCenter[0] - mLastPoint.x
        val translateY = lineCenter[1] - mLastPoint.y
        if (isScaling || !outerMatrix.isIdentity) {
            scrollBy(translateX, translateY, false)
        }
        mLastPoint[lineCenter[0]] = lineCenter[1]
        mLastMovePoint[lineCenter[0]] = lineCenter[1]
    }

    private fun onActionUp(event: MotionEvent) {
        Log.d(TAG, "onActionUp: event is " + event.action + ", mPinchMode = " + mPinchMode)
        //如果之前是缩放模式,还需要触发一下缩放结束动画
        if (mPinchMode == PINCH_MODE_SCALE || isInOverScroll) {
            val innerMatrix = MathUtils.matrixTake()
            getInnerMatrix(innerMatrix)
            val innerScale = MathUtils.getMatrixScale(innerMatrix)
            if (mScale > getMaxScale(innerScale)) {
                vibrate(21230)
            } else if (mScale <= MIN_SCALE) {
                vibrate(21240)
            }
            MathUtils.matrixGiven(innerMatrix)
            val degree = MathUtils.getCurrentRotateDegree(
                outerMatrix
            )
            if (degree != 0f) {
                onRotationEnd()
            } else {
                scaleEnd()
            }
        }
        setPinchMode(PINCH_MODE_FREE)
    }

    private fun onActionPointerDown(event: MotionEvent) {
        if (event.pointerCount > 2) {
            return
        }
        forceActionUp = false

        //停止所有动画
        cancelAllAnimator()
        //切换到缩放模式
        setPinchMode(PINCH_MODE_SCALE)
        Log.d(TAG, "onActionPointerDown: setPinchMode to scale")
        val lineCenter =
            MathUtils.getCenterPoint(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
        mLastPoint[lineCenter[0]] = lineCenter[1]
        mLastVector[event.getX(1) - event.getX(0)] = event.getY(1) - event.getY(0)
    }

    /**
     * 让图片移动一段距离
     *
     *
     * 不能移动超过可移动范围,超过了就到可移动范围边界为止.
     *
     * @param xDiff 移动距离
     * @param yDiff 移动距离
     * @return
     */
    private fun scrollBy(xDiff: Float, yDiff: Float, isFling: Boolean): Boolean {
        var xDiff = xDiff
        var yDiff = yDiff
        if (!isReady) {
            return false
        }
        if (xDiff == 0f && yDiff == 0f) {
            Log.d(TAG, "scrollBy: xDiff = yDiff = 0.")
            return true
        }
        val diffX = xDiff
        val diffY = yDiff
        val bound = imageDisplayRectF

        //控件大小
        val viewWidth = width.toFloat()
        val displayHeight = height.toFloat()

        //如果当前图片宽度小于控件宽度，则不能移动
        if (bound.right - bound.left <= viewWidth) {
            xDiff = 0f
            //如果图片左边在移动后超出控件左边
        } else if (bound.left + xDiff > 0) {
            //如果在移动之前是没超出的，计算应该移动的距离
            xDiff = if (bound.left < 0) {
                -bound.left
                //否则无法移动
            } else {
                0f
            }
            //如果图片右边在移动后超出控件右边
        } else if (bound.right + xDiff < viewWidth) {
            //如果在移动之前是没超出的，计算应该移动的距离
            xDiff = if (bound.right > viewWidth) {
                viewWidth - bound.right
                //否则无法移动
            } else {
                0f
            }
        }

        //以下同理
        if (bound.bottom - bound.top < displayHeight) {
            yDiff = 0f
        } else if (bound.top + yDiff > 0) {
            yDiff = if (bound.top < 0) {
                -bound.top
            } else {
                0f
            }
        } else if (bound.bottom + yDiff < displayHeight) {
            yDiff = if (bound.bottom > displayHeight) {
                displayHeight - bound.bottom
            } else {
                0f
            }
        }
        if (xDiff == 0f && yDiff == 0f) {
            if (!isFling && Math.abs(diffY) > Math.abs(diffX) && !isInOverScroll) {
                MathUtils.rectFGiven(bound)
                return false
            }
        }
        if (mScale == 1f && !isFling && xDiff == 0f && mOverScrollType != OVER_SCROLL_NONE) {
            xDiff = calculateOverScroll(diffX)
        }
        MathUtils.rectFGiven(bound)
        //应用移动变换
        outerMatrix.postTranslate(xDiff, yDiff)
        if (mOuterGestureListener != null) {
            mOuterGestureListener!!.onScroll(xDiff, yDiff)
        }
        dispatchOuterMatrixChanged()

        //检查是否有变化
        return if (xDiff != 0f || yDiff != 0f) {
            // 交给ViewPager去处理左右滑动
            xDiff != 0f || Math.abs(diffX) <= Math.abs(diffY)
        } else {
            isInOverScroll
        }
    }

    /**
     * @param xDiff > 0为向左滑动，< 0为向右滑动
     * @return
     */
    private fun calculateOverScroll(xDiff: Float): Float {
        var translateX = 0f
        if (mOverScrollType == OVER_SCROLL_NONE) {
            return 0f
        }
        if (mOverScrollType == OVER_SCROLL_ALL) {
            isInOverScroll = true
            translateX = xDiff * 0.2f
        } else if (xDiff > 0 && mOverScrollType == OVER_SCROLL_LEFT) {
            isInOverScroll = true
            translateX = xDiff * 0.2f
        } else if (xDiff < 0 && mOverScrollType == OVER_SCROLL_RIGHT) {
            isInOverScroll = true
            translateX = xDiff * 0.2f
        } else if (isInOverScroll) {
            translateX = xDiff * 0.2f
        }
        return translateX
    }

    private val isAnimating: Boolean
        private get() = if (mScaleAnimator != null && mScaleAnimator!!.isStarted) {
            true
        } else isFling

    /**
     * 当缩放操作结束动画
     *
     *
     * 如果图片超过边界,找到最近的位置动画恢复.
     * 如果图片缩放尺寸超过最大值或者最小值,找到最近的值动画恢复.
     */
    private fun scaleEnd() {
        if (!isReady) {
            return
        }

        // 可能由于前面有cancelAnimator，导致mScale的值与图片的实际缩放值对应不上
        Log.d(TAG, "scaleEnd: scale is " + mScale + ", matrix is " + outerMatrix.toShortString())
        mScale = MathUtils.getMatrixScale(
            outerMatrix
        )

        //是否修正了位置
        var change = false

        //获取图片整体的变换矩阵
        val currentMatrix = MathUtils.matrixTake()
        getCurrentImageMatrix(currentMatrix)

        //整体缩放比例
        val innerMatrix = MathUtils.matrixTake()
        getInnerMatrix(innerMatrix)
        val innerScale = MathUtils.getMatrixScale(innerMatrix)
        MathUtils.matrixGiven(innerMatrix)

        //第二层缩放比例
        val outerScale = mScale

        //控件大小
        val displayWidth = width.toFloat()
        val displayHeight = height.toFloat()
        //最大缩放比例
        val maxScale = getMaxScale(innerScale)
        //比例修正
        var scalePost = 1f
        //位置修正
        var postX = 0f
        var postY = 0f
        var isExceedMaxScale = false

        //如果整体缩放比例大于最大比例，进行缩放修正
        if (outerScale > maxScale) {
            isExceedMaxScale = true
            scalePost = maxScale / outerScale
            Log.i(TAG, "scaleEnd: outerScale > maxScale")
        }
        //如果缩放修正后整体导致第二层缩放小于1（就是图片比fit center状态还小），重新修正缩放
        if (outerScale * scalePost < 1f) {
            scalePost = 1f / outerScale
            Log.d(TAG, "scaleEnd: outerScale * scalePost < 1f")
        }

        //如果缩放修正不为1，说明进行了修正
        if (scalePost != 1f) {
            change = true
        }

        //尝试根据缩放点进行缩放修正
        val testMatrix = MathUtils.matrixTake(currentMatrix)
        testMatrix.postScale(scalePost, scalePost, mLastMovePoint.x, mLastMovePoint.y)
        val testBound = imageRectF
        //获取缩放修正后的图片方框
        testMatrix.mapRect(testBound)

        //检测缩放修正后位置有无超出，如果超出进行位置修正
        if (testBound.right - testBound.left < displayWidth) {
            postX = displayWidth / 2f - (testBound.right + testBound.left) / 2f
        } else if (testBound.left > 0) {
            postX = -testBound.left
        } else if (testBound.right < displayWidth) {
            postX = displayWidth - testBound.right
        }
        if (testBound.bottom - testBound.top < displayHeight) {
            postY = displayHeight / 2f - (testBound.bottom + testBound.top) / 2f
        } else if (testBound.top > 0) {
            postY = -testBound.top
        } else if (testBound.bottom < displayHeight) {
            postY = displayHeight - testBound.bottom
        }
        //如果位置修正不为0，说明进行了修正
        if (postX != 0f || postY != 0f) {
            change = true
        }

        //只有有执行修正才执行动画
        if (change) {
            //计算结束矩阵
            val animEnd = MathUtils.matrixTake(
                outerMatrix
            )
            animEnd.postScale(scalePost, scalePost, mLastMovePoint.x, mLastMovePoint.y)
            animEnd.postTranslate(postX, postY)
            mScale *= scalePost
            if (isExceedMaxScale) {
                mFinalMatrix.set(animEnd)
                if (mOuterGestureListener != null) {
                    mOuterGestureListener!!.onDecodeFinalRegion()
                }
            }
            matrixAnimateTo(animEnd)
            MathUtils.matrixGiven(animEnd)
        } else {
            if (mExtOuterGestureListener != null) {
                mExtOuterGestureListener!!.onScaleEnd()
            }
        }

        //清理临时变量
        MathUtils.rectFGiven(testBound)
        MathUtils.matrixGiven(testMatrix)
        MathUtils.matrixGiven(currentMatrix)
    }

    private fun detectRotation(event: MotionEvent) {
        if (!isSupportRotation) {
            return
        }

        // 计算双指中点，旋转围绕中点进行
        val lineCenter =
            MathUtils.getCenterPoint(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
        mCurrentVector[event.getX(1) - event.getX(0)] = event.getY(1) - event.getY(0)
        val degree = getRotateDegree(mLastVector, mCurrentVector)
        if (mScale >= 1) {
            mTempDegree = degree
        } else {
            outerMatrix.postRotate(degree - mTempDegree, lineCenter[0], lineCenter[1])
            mDegree += degree - mTempDegree
            mTempDegree = degree
            dispatchOuterMatrixChanged()
        }
    }

    private fun onRotationEnd() {
        val degree = mDegree % 360
        var toDegree = 0
        val positiveDegree = Math.abs(degree)
        if (positiveDegree < ROTATION_SLOP) {
            toDegree = 0
        } else if (positiveDegree >= ROTATION_SLOP && positiveDegree < 90 + ROTATION_SLOP) {
            toDegree = if (degree > 0) 90 else -90
        } else if (positiveDegree >= 90 + ROTATION_SLOP && positiveDegree < 180 + ROTATION_SLOP) {
            toDegree = if (degree > 0) 180 else -180
        } else if (positiveDegree >= 180 + ROTATION_SLOP && positiveDegree < 270 + ROTATION_SLOP) {
            toDegree = if (degree > 0) 270 else -270
        }
        Log.d(TAG, "onRotationEnd: degree is $degree, toDegree is $toDegree")
        if (toDegree != 0) {
        } else {
            val animEnd = MathUtils.matrixTake()
            matrixAnimateTo(animEnd, false)
            MathUtils.matrixGiven(animEnd)
            isInOverScroll = false
            mDegree = toDegree.toFloat()
            mScale = 1f
            return
        }
        var imageRectF = imageRectF

        //控件大小
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val animEnd = MathUtils.matrixTake(
            outerMatrix
        )
        animEnd.postRotate(toDegree - mDegree, mLastPoint.x, mLastPoint.y)

        // 获得旋转后图片显示的位置
        val innerMatrix = MathUtils.matrixTake()
        getInnerMatrix(innerMatrix)
        var current = MathUtils.matrixTake(innerMatrix)
        current.postConcat(animEnd)
        current.mapRect(imageRectF)

        // 根据图片显示的大小和View的大小算出缩放比例
        val scalePost =
            Math.min(viewWidth * 1f / imageRectF.width(), viewHeight * 1f / imageRectF.height())
        animEnd.postScale(scalePost, scalePost, mLastPoint.x, mLastPoint.y)

        // 获得缩放后图片显示的位置
        current = innerMatrix
        current.postConcat(animEnd)
        imageRectF = imageRectF
        current.mapRect(imageRectF)

        // 根据图片显示位置和View的位置算出偏移
        val translateX = (viewWidth - imageRectF.width()) / 2
        val translateY = (viewHeight - imageRectF.height()) / 2
        animEnd.postTranslate(translateX - imageRectF.left, translateY - imageRectF.top)
        MathUtils.matrixGiven(current)
        MathUtils.matrixGiven(innerMatrix)
        matrixAnimateTo(animEnd, true)
        MathUtils.matrixGiven(animEnd)
        isInOverScroll = false
        mDegree = toDegree.toFloat()
        mScale = 1f
    }

    fun resetDisplayRect(animate: Boolean) {
        setPinchMode(PINCH_MODE_FREE)
        mScale = 1f
        mDegree = 1f
        if (outerMatrix.isIdentity) {
            return
        }
        if (animate) {
            val animEnd = MathUtils.matrixTake()
            matrixAnimateTo(animEnd)
            MathUtils.matrixGiven(animEnd)
        } else {
            outerMatrix.reset()
        }
    }

    /**
     * 执行当前outerMatrix到指定outerMatrix渐变的动画
     *
     *
     * 调用此方法会停止正在进行中的手势以及手势动画.
     * 当duration为0时,outerMatrix值会被立即设置而不会启动动画.
     *
     * @param endMatrix 动画目标矩阵
     */
    private fun matrixAnimateTo(endMatrix: Matrix?, isRotationAnimation: Boolean = false) {
        if (endMatrix == null) {
            return
        }
        //将手势设置为PINCH_MODE_FREE将停止后续手势的触发
        setPinchMode(PINCH_MODE_FREE)

        //停止所有正在进行的动画
        cancelAllAnimator()
        val from = MathUtils.matrixTake()
        getCurrentImageMatrix(from)
        val to = MathUtils.matrixTake()
        getInnerMatrix(to)
        to.postConcat(endMatrix)
        if (mScaleAnimator == null) {
            mScaleAnimator = ScaleAnimator(outerMatrix, endMatrix)
        } else {
            mScaleAnimator!!.setMatrix(outerMatrix, endMatrix)
        }
        mScaleAnimator!!.setRotationAnimation(isRotationAnimation)
        mScaleAnimator!!.start()
        MathUtils.matrixGiven(from)
        MathUtils.matrixGiven(to)
    }

    /**
     * 获取内部变换矩阵.
     *
     *
     * 内部变换矩阵是原图到fit center状态的变换,当原图尺寸变化或者控件大小变化都会发生改变
     * 当尚未布局或者原图不存在时,其值无意义.所以在调用前需要确保前置条件有效,否则将影响计算结果.
     *
     * @param matrix 用于填充结果的对象
     * @return 如果传了matrix参数则将matrix填充后返回, 否则new一个填充返回
     */
    fun getInnerMatrix(matrix: Matrix?): Matrix {
        return mPhotoView.getInnerMatrix(matrix)
    }

    /**
     * 获取图片总变换矩阵.
     *
     *
     * 总变换矩阵为内部变换矩阵x外部变换矩阵,决定了原图到所见最终状态的变换
     * 当尚未布局或者原图不存在时,其值无意义.所以在调用前需要确保前置条件有效,否则将影响计算结果.
     *
     * @param matrix 用于填充结果的对象
     * @return 如果传了matrix参数则将matrix填充后返回, 否则new一个填充返回
     * @see .getInnerMatrix
     */
    fun getCurrentImageMatrix(matrix: Matrix): Matrix {
        //获取内部变换矩阵
        var matrix = matrix
        matrix = getInnerMatrix(matrix)
        //乘上外部变换矩阵
        matrix.postConcat(outerMatrix)
        return matrix
    }

    fun getFinalImageMatrix(matrix: Matrix): Matrix {
        var matrix = matrix
        matrix = getInnerMatrix(matrix)
        //乘上外部变换矩阵
        matrix.postConcat(mFinalMatrix)
        return matrix
    }

    /**
     * 获取当前变换后的图片位置和尺寸
     *
     *
     * 当尚未布局或者原图不存在时,其值无意义.所以在调用前需要确保前置条件有效,否则将影响计算结果.
     *
     * @return 如果传了rectF参数则将rectF填充后返回, 否则new一个填充返回
     * @see .getCurrentImageMatrix
     */
    val imageDisplayRectF: RectF
        get() = mPhotoView.imageDisplayRectF

    /**
     * 使用Math#atan2(double y, double x)方法求上次触摸事件两指所示向量与x轴的夹角，
     * 再求出本次触摸事件两指所示向量与x轴夹角，最后求出两角之差即为图片需要转过的角度
     *
     * @param lastVector    上次触摸事件两指间连线所表示的向量
     * @param currentVector 本次触摸事件两指间连线所表示的向量
     * @return 两向量夹角，单位“度”，顺时针旋转时为正数，逆时针旋转时返回负数
     */
    private fun getRotateDegree(lastVector: PointF, currentVector: PointF): Float {
        //上次触摸事件向量与x轴夹角
        val lastRad = Math.atan2(lastVector.y.toDouble(), lastVector.x.toDouble())
        //当前触摸事件向量与x轴夹角
        val currentRad = Math.atan2(currentVector.y.toDouble(), currentVector.x.toDouble())
        // 两向量与x轴夹角之差即为需要旋转的角度
        val rad = currentRad - lastRad
        //“弧度”转“度”
        return Math.toDegrees(rad).toFloat()
    }

    /**
     * 双击后放大或者缩小
     *
     *
     * 将图片缩放比例缩放到nextScale指定的值.
     * 但nextScale值不能大于最大缩放值不能小于fit center情况下的缩放值.
     * 将双击的点尽量移动到控件中心.
     *
     * @param x 双击的点
     * @param y 双击的点
     */
    private fun doubleTap(x: Float, y: Float) {
        Log.d(TAG, "doubleTap: ")
        if (!isReady || !isSupportScale) {
            return
        }

        //获取第一层变换矩阵
        val innerMatrix = MathUtils.matrixTake()
        getInnerMatrix(innerMatrix)
        //当前总的缩放比例
        val innerScale = MathUtils.getMatrixScale(innerMatrix)
        val outerScale = MathUtils.getMatrixScale(
            outerMatrix
        )
        val currentScale = innerScale * outerScale
        if (MathUtils.isAlmostEquals(outerScale, 1f)) {
            if (mOuterGestureListener != null) {
                mOuterGestureListener!!.onDoubleTap()
            }
            if (mExtOuterGestureListener != null) {
                mExtOuterGestureListener!!.onDoubleTap()
            }
        }

        //控件大小
        val displayWidth = width.toFloat()
        val displayHeight = height.toFloat()

        //接下来要放大的大小
        //如果接下来放大大于最大值或者小于fit center值，则取边界
        val nextScale = calculateNextScale(innerScale, outerScale, currentScale)
        Log.d(
            TAG,
            "doubleTap: innerScale is $innerScale, outerScale is $outerScale, currentScale is $currentScale, next scale is $nextScale"
        )

        //开始计算缩放动画的结果矩阵
        val animEnd = MathUtils.matrixTake(
            outerMatrix
        )
        //计算还需缩放的倍数
        animEnd.postScale(nextScale, nextScale, x, y)
        mScale *= nextScale

        //将放大点移动到控件中心
        animEnd.postTranslate(displayWidth / 2f - x, displayHeight / 2f - y)
        //得到放大之后的图片方框
        val testMatrix = MathUtils.matrixTake(innerMatrix)
        testMatrix.postConcat(animEnd)
        val testBound = imageRectF
        testMatrix.mapRect(testBound)
        //修正位置
        var postX = 0f
        var postY = 0f
        if (testBound.right - testBound.left < displayWidth) {
            postX = displayWidth / 2f - (testBound.right + testBound.left) / 2f
        } else if (testBound.left > 0) {
            postX = -testBound.left
        } else if (testBound.right < displayWidth) {
            postX = displayWidth - testBound.right
        }
        if (testBound.bottom - testBound.top < displayHeight) {
            postY = displayHeight / 2f - (testBound.bottom + testBound.top) / 2f
        } else if (testBound.top > 0) {
            postY = -testBound.top
        } else if (testBound.bottom < displayHeight) {
            postY = displayHeight - testBound.bottom
        }
        //应用修正位置
        animEnd.postTranslate(postX, postY)
        mFinalMatrix.set(animEnd)
        if (mOuterGestureListener != null && nextScale > 1) {
            mOuterGestureListener!!.onDecodeFinalRegion()
        }
        if (mExtOuterGestureListener != null && nextScale > 1) {
            mExtOuterGestureListener!!.onDecodeFinalRegion()
        }
        Log.e(TAG, "doubleTap: anim End is " + animEnd.toShortString())
        matrixAnimateTo(animEnd)
        MathUtils.rectFGiven(testBound)
        MathUtils.matrixGiven(testMatrix)
        MathUtils.matrixGiven(animEnd)
        MathUtils.matrixGiven(innerMatrix)
    }

    protected fun getMaxScale(innerScale: Float): Float {
        val imageBound = imageRectF
        val imageWidth = imageBound.width().toInt()
        val scale = width * 1f / imageWidth / innerScale * 2f
        Log.d(TAG, "getMaxScale: scale is $scale")
        return Math.max(scale, MAX_SCALE)
    }

    private fun calculateNextScale(
        innerScale: Float,
        outerScale: Float,
        currentScale: Float
    ): Float {
        val rectF = imageDisplayRectF
        val viewWidth = width
        val viewHeight = height
        val scale: Float
        val displayWidth = rectF.width().toInt()
        val displayHeight = rectF.height().toInt()
        scale = if (Math.abs(mScale - 1) < 0.01) {
            if (viewWidth - displayWidth > 2) {
                viewWidth * 1f / displayWidth
            } else if (viewHeight * 1f / displayHeight > 2) {
                viewHeight * 1f / displayHeight
            } else {
                DOUBLE_TAB_SCALE.toFloat()
            }
        } else {
            1 / outerScale
        }
        MathUtils.rectFGiven(rectF)
        return scale
    }

    /**
     * 执行惯性动画
     *
     *
     * 动画在遇到不能移动就停止.
     * 动画速度衰减到很小就停止.
     *
     *
     * 其中参数速度单位为 像素/秒
     *
     * @param vx x方向速度
     * @param vy y方向速度
     */
    private fun fling(vx: Float, vy: Float, duration: Long) {
        if (!isReady) {
            return
        }
        //清理当前可能正在执行的动画
        cancelAllAnimator()
        //创建惯性动画
        //FlingAnimator单位为 像素/帧,一秒60帧
        mFlingAnimator = FlingAnimator(vx / 60f, vy / 60f, duration)
        mFlingAnimator!!.start()
    }

    /**
     * 停止所有手势动画
     */
    private fun cancelAllAnimator() {
        if (mScaleAnimator != null) {
            mScaleAnimator!!.cancel()
            mScaleAnimator = null
        }
        if (mFlingAnimator != null) {
            mFlingAnimator!!.cancel()
            mFlingAnimator = null
        }
    }

    /**
     * 惯性动画
     *
     *
     * 速度逐渐衰减,每帧速度衰减为原来的FLING_DAMPING_FACTOR,当速度衰减到小于1时停止.
     * 当图片不能移动时,动画停止.
     */
    private inner class FlingAnimator(vectorX: Float, vectorY: Float, duration: Long) :
        ValueAnimator(), AnimatorUpdateListener, Animator.AnimatorListener {
        /**
         * 速度向量
         */
        private val mVector: FloatArray

        /**
         * 创建惯性动画
         *
         *
         * 参数单位为 像素/帧
         *
         * @param vectorX  速度向量
         * @param vectorY  速度向量
         * @param duration
         */
        init {
            setFloatValues(0f, 1f)
            Log.d(TAG, "FlingAnimator: duration is $duration")
            setDuration(duration)
            interpolator = TimeInterpolator { input ->
                (1.0f - Math.pow(
                    (1.0f - input).toDouble(),
                    attr.factor.toDouble()
                )).toFloat()
            }
            addUpdateListener(this)
            addListener(this)
            mVector = floatArrayOf(vectorX, vectorY)
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            //移动图像并给出结果
            val result = scrollBy(mVector[0], mVector[1], true)
            //衰减速度
            mVector[0] *= FLING_DAMPING_FACTOR
            mVector[1] *= FLING_DAMPING_FACTOR
            //速度太小或者不能移动了就结束
            if (!result || MathUtils.getDistance(0f, 0f, mVector[0], mVector[1]) < 1f) {
                animation.cancel()
            }
        }

        override fun onAnimationStart(animation: Animator) {}
        override fun onAnimationEnd(animation: Animator) {
            Log.d(TAG, "FlingAnimator, onAnimationEnd: mOuterGestureListener.onStartRegionDecode()")
            isFling = false
        }

        override fun onAnimationCancel(animation: Animator) {
            isFling = false
        }

        override fun onAnimationRepeat(animation: Animator) {}
    }

    /**
     * 缩放动画
     *
     *
     * 在给定时间内从一个矩阵的变化逐渐动画到另一个矩阵的变化
     */
    private inner class ScaleAnimator(start: Matrix, end: Matrix) : ValueAnimator(),
        AnimatorUpdateListener, Animator.AnimatorListener {
        /**
         * 开始矩阵
         */
        private val mStart = FloatArray(9)

        /**
         * 结束矩阵
         */
        private val mEnd = FloatArray(9)

        /**
         * 中间结果矩阵
         */
        private val mResult = FloatArray(9)
        private var isRotationAnimation = false

        /**
         * 构建一个缩放动画
         *
         *
         * 从一个矩阵变换到另外一个矩阵
         *
         * @param start 开始矩阵
         * @param end   结束矩阵
         */
        init {
            setFloatValues(0f, 1f)
            duration = SCALE_ANIMATOR_DURATION.toLong()
            addUpdateListener(this)
            addListener(this)
            start.getValues(mStart)
            end.getValues(mEnd)
        }

        fun setMatrix(start: Matrix, end: Matrix) {
            start.getValues(mStart)
            end.getValues(mEnd)
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            //获取动画进度
            val value = animation.animatedValue as Float
            //根据动画进度计算矩阵中间插值
            for (i in 0..8) {
                mResult[i] = mStart[i] + (mEnd[i] - mStart[i]) * value
            }

            //设置矩阵并重绘
            outerMatrix.setValues(mResult)
            dispatchOuterMatrixChanged()
        }

        override fun onAnimationStart(animation: Animator) {
            Log.d(TAG, "ScaleAnimator, onAnimationStart: ")
            isScaling = true
        }

        override fun onAnimationEnd(animation: Animator) {
            Log.d(TAG, "ScaleAnimator, onAnimationEnd: mOuterGestureListener.onStartRegionDecode()")
            isInOverScroll = false
            isScaling = false
            forceActionUp = false
            if (isRotationAnimation) {
                // 重置Outer矩阵，角度由Inner矩阵来控制
                outerMatrix.reset()
                if (mOuterGestureListener != null) {
                    mOuterGestureListener!!.onRotationEnd(mDegree.toInt())
                }
                isRotationAnimation = false
            }
            if (mExtOuterGestureListener != null) {
                mExtOuterGestureListener!!.onScaleEnd()
            }
        }

        override fun onAnimationCancel(animation: Animator) {
            isInOverScroll = false
            isRotationAnimation = false
        }

        override fun onAnimationRepeat(animation: Animator) {}
        fun setRotationAnimation(rotationAnimation: Boolean) {
            isRotationAnimation = rotationAnimation
        }
    }

    /**
     * 添加外部矩阵变化监听
     *
     * @param listener
     */
    fun setOuterGestureListener(listener: OuterGestureListener?) {
        if (listener == null) {
            return
        }
        mOuterGestureListener = listener
    }

    fun addOuterGestureListener(listener: OuterGestureListener?) {
        mExtOuterGestureListener = listener
    }

    /**
     * 触发外部矩阵修改事件
     *
     *
     * 需要在每次给外部矩阵设置值时都调用此方法.
     *
     * @see .mOuterMatrix
     */
    private fun dispatchOuterMatrixChanged() {
        if (mOuterGestureListener == null) {
            return
        }
        mOuterGestureListener!!.onOuterMatrixChanged()
    }

    /**
     * 对象池
     *
     *
     * 防止频繁new对象产生内存抖动.
     * 由于对象池最大长度限制,如果吞度量超过对象池容量,仍然会发生抖动.
     * 此时需要增大对象池容量,但是会占用更多内存.
     *
     * @param <T> 对象池容纳的对象类型
    </T> */
    private abstract class ObjectsPool<T>(
        /**
         * 对象池的最大容量
         */
        private val mSize: Int
    ) {
        /**
         * 对象池队列
         */
        private val mQueue: Queue<T>

        /**
         * 创建一个对象池
         *
         * @param size 对象池最大容量
         */
        init {
            mQueue = LinkedList()
        }

        /**
         * 获取一个空闲的对象
         *
         *
         * 如果对象池为空,则对象池自己会new一个返回.
         * 如果对象池内有对象,则取一个已存在的返回.
         * take出来的对象用完要记得调用given归还.
         * 如果不归还,让然会发生内存抖动,但不会引起泄漏.
         *
         * @return 可用的对象
         * @see .given
         */
        fun take(): T {
            //如果池内为空就创建一个
            return if (mQueue.size == 0) {
                newInstance()
            } else {
                //对象池里有就从顶端拿出来一个返回
                resetInstance(mQueue.poll())
            }
        }

        /**
         * 归还对象池内申请的对象
         *
         *
         * 如果归还的对象数量超过对象池容量,那么归还的对象就会被丢弃.
         *
         * @param obj 归还的对象
         * @see .take
         */
        fun given(obj: T?) {
            //如果对象池还有空位子就归还对象
            if (obj != null && mQueue.size < mSize) {
                mQueue.offer(obj)
            }
        }

        /**
         * 实例化对象
         *
         * @return 创建的对象
         */
        protected abstract fun newInstance(): T

        /**
         * 重置对象
         *
         *
         * 把对象数据清空到就像刚创建的一样.
         *
         * @param obj 需要被重置的对象
         * @return 被重置之后的对象
         */
        protected abstract fun resetInstance(obj: T): T
    }

    /**
     * 矩阵对象池
     */
    private class MatrixPool(size: Int) : ObjectsPool<Matrix>(size) {
        override fun newInstance(): Matrix {
            return Matrix()
        }

        override fun resetInstance(obj: Matrix): Matrix {
            obj.reset()
            return obj
        }
    }

    /**
     * 矩形对象池
     */
    private class RectFPool(size: Int) : ObjectsPool<RectF>(size) {
        override fun newInstance(): RectF {
            return RectF()
        }

        override fun resetInstance(obj: RectF): RectF {
            obj.setEmpty()
            return obj
        }
    }

    /**
     * 数学计算工具类
     */
    object MathUtils {
        private val mMatrixPool = MatrixPool(16)
        private val mRectFPool = RectFPool(16)
        fun matrixTake(): Matrix {
            return mMatrixPool.take()
        }

        /**
         * 获取某个矩阵的copy
         */
        fun matrixTake(matrix: Matrix?): Matrix {
            val result = mMatrixPool.take()
            if (matrix != null) {
                result.set(matrix)
            }
            return result
        }

        /**
         * 归还矩阵对象
         */
        fun matrixGiven(matrix: Matrix) {
            mMatrixPool.given(matrix)
        }

        /**
         * 获取矩形对象
         */
        fun rectFTake(): RectF {
            return mRectFPool.take()
        }

        /**
         * 按照指定值获取矩形对象
         */
        fun rectFTake(left: Float, top: Float, right: Float, bottom: Float): RectF {
            val result = mRectFPool.take()
            result[left, top, right] = bottom
            return result
        }

        /**
         * 获取某个矩形的副本
         */
        fun rectFTake(rectF: RectF?): RectF {
            val result = mRectFPool.take()
            if (rectF != null) {
                result.set(rectF)
            }
            return result
        }

        /**
         * 归还矩形对象
         */
        fun rectFGiven(rectF: RectF) {
            mRectFPool.given(rectF)
        }

        /**
         * 获取两点之间距离
         *
         * @param x1 点1
         * @param y1 点1
         * @param x2 点2
         * @param y2 点2
         * @return 距离
         */
        fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
            val x = x1 - x2
            val y = y1 - y2
            return Math.sqrt((x * x + y * y).toDouble()).toFloat()
        }

        /**
         * 获取两点的中点
         *
         * @param x1 点1
         * @param y1 点1
         * @param x2 点2
         * @param y2 点2
         * @return float[]{x, y}
         */
        fun getCenterPoint(x1: Float, y1: Float, x2: Float, y2: Float): FloatArray {
            return floatArrayOf((x1 + x2) / 2f, (y1 + y2) / 2f)
        }

        /**
         * 获取矩阵的缩放值
         * 有旋转角度的时候不能用这个方法！！
         *
         * @param matrix 要计算的矩阵
         */
        fun getMatrixScale(matrix: Matrix?): Float {
            return if (matrix != null) {
                val value = FloatArray(9)
                matrix.getValues(value)
                if (value[0] != 0f) Math.abs(value[0]) else Math.abs(value[1])
            } else {
                1f
            }
        }

        fun getCurrentRotateDegree(matrix: Matrix): Float {
            val xAxis = floatArrayOf(1f, 0f)

            // 每次重置初始向量的值为与x轴同向
            xAxis[0] = 1f
            xAxis[1] = 0f
            // 初始向量通过矩阵变换后的向量
            matrix.mapVectors(xAxis)
            // 变换后向量与x轴夹角
            val rad = Math.atan2(xAxis[1].toDouble(), xAxis[0].toDouble())
            return Math.toDegrees(rad).toFloat()
        }

        fun isAlmostEquals(a: Float, b: Float): Boolean {
            return Math.abs(a - b) < 0.01
        }
    }

    companion object {
        private const val TAG = "GestureListener"
        private val TAP_TIMEOUT = ViewConfiguration.getTapTimeout()
        private const val DOUBLE_TAP_TIME_OUT = 150
        private const val FLING_COASTING_DURATION_S = 0.05f

        // 回弹类型
        const val OVER_SCROLL_LEFT = 1
        const val OVER_SCROLL_RIGHT = 2
        const val OVER_SCROLL_ALL = 3
        const val OVER_SCROLL_NONE = 4
        private const val ROTATION_SLOP = 45

        /**
         * 图片缩放动画时间
         */
        const val SCALE_ANIMATOR_DURATION = 250

        /**
         * 惯性动画衰减参数
         */
        const val FLING_DAMPING_FACTOR = 0.93f
        private const val MAX_SCALE = 16f
        private const val MIN_SCALE = 0.5f
        const val DOUBLE_TAB_SCALE = 2

        /**
         * 手势状态：自由状态
         */
        const val PINCH_MODE_FREE = 0

        /**
         * 手势状态：单指滚动状态
         */
        const val PINCH_MODE_SCROLL = 1

        /**
         * 手势状态：双指缩放状态
         */
        const val PINCH_MODE_SCALE = 2
    }
}