package com.example.businessbase.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class ImagePreviewRecyclerView(context: Context, attrs: AttributeSet?) :
    RecyclerView(context, attrs) {

    private var mIsInterceptTouchEvent = false

    // 是否处于手势状态
    private var mIsGestureState = false
    private var mCurrentView: ScalableImageView? = null

    fun setCurrentView(currentView: ScalableImageView) {
        mCurrentView = currentView
    }

    /**
     * 处理事件分发
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            mIsInterceptTouchEvent = false
            mIsGestureState = mCurrentView?.isGestureState ?: false
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 处理拦截逻辑
     */
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (mIsGestureState) {
            return true
        }
        // 多指情况
        if (event.pointerCount > 1) {
            mIsGestureState = true
            return true
        }
        return super.onInterceptTouchEvent(event)
    }

    /**
     * 处理 Touch 事件
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (mIsGestureState) {
            if (mIsInterceptTouchEvent) {
                return super.onTouchEvent(ev)
            }
            if (ev.action == MotionEvent.ACTION_DOWN) {
                super.onTouchEvent(ev)
            }
            // 如果 touch 事件没有被子 view 消费，则自己处理 touch 事件
            return if (mCurrentView != null && mCurrentView!!.onTouchEvent(ev)) {
                true
            } else {
                mIsInterceptTouchEvent = true
                super.onTouchEvent(ev)
            }
        }
        return super.onTouchEvent(ev)
    }

}