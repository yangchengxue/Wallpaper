package com.example.businessbase.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import com.example.businessbase.R

class LoadingView : AppCompatImageView {
    private var rotateAnimation: RotateAnimation? = null

    constructor(ctx: Context) : super(ctx) {
        init()
    }

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {
        init()
    }

    constructor(ctx: Context, attrs: AttributeSet, defStyle: Int) : super(ctx, attrs, defStyle) {
        init()
    }

    private fun init() {
        setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.base_loading))
        showLoading(true)
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        showLoading(visibility == View.VISIBLE)
    }

    private fun showLoading(isShow: Boolean) {
        rotateAnimation = RotateAnimation(
            0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 1100
            repeatCount = -1
        }
        if (isShow) {
            this.animation = rotateAnimation
        } else {
            rotateAnimation?.cancel()
            this.clearAnimation()
        }
    }
}