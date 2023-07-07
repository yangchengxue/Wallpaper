package com.example.wallpaper

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.businessbase.views.ImagePreviewRecyclerView
import com.example.businessbase.views.ScalableImageView
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class ImagePreviewLayoutManager @Inject constructor(@ActivityContext context: Context) :
    LinearLayoutManager(context) {

    private val mPagerSnapHelper: PagerSnapHelper by lazy {
        PagerSnapHelper()
    }
    private var mOnViewPagerListener: ImagePreviewListener? = null
    private var mRecyclerView: RecyclerView? = null
    private var mLastPosition = 0
    private var mLastPhotoView: ScalableImageView? = null

    init {
        orientation = HORIZONTAL
    }

    fun setOnViewPagerListener(listener: ImagePreviewListener) {
        mOnViewPagerListener = listener
    }

    override fun onAttachedToWindow(view: RecyclerView) {
        super.onAttachedToWindow(view)
        mRecyclerView = view
        mPagerSnapHelper.attachToRecyclerView(view)
        mRecyclerView?.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                if (childCount == 1) {
                    setCurrentView(mLastPosition)
                }
            }

            override fun onChildViewDetachedFromWindow(view: View) {}
        })
    }

    override fun onScrollStateChanged(state: Int) {
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            mPagerSnapHelper.findSnapView(this)?.let {
                val curPosition = getPosition(it)
                if (mLastPosition != curPosition) {
                    resetLastPhotoViewState()
                    setCurrentView(curPosition)
                    mLastPosition = curPosition
                }
            }
        }
    }

    override fun scrollToPosition(position: Int) {
        super.scrollToPosition(position)
        mLastPosition = position
    }

    private fun setCurrentView(position: Int) {
        mPagerSnapHelper.findSnapView(this)?.findViewById<ScalableImageView>(R.id.photoView)?.let {
            mLastPhotoView = it
            (mRecyclerView as ImagePreviewRecyclerView).setCurrentView(it)
            mOnViewPagerListener?.onPageSelected(position)
        }
    }

    private fun resetLastPhotoViewState() {
        mLastPhotoView?.resetDisplayRect(true)
    }

}