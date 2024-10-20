package com.test.flagschallenge.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import coil.load


@BindingAdapter("setImage")
fun setImage(imageView: ImageView, image: Int) {
    imageView.load(image)
}

@BindingAdapter("setVisibility")
fun setVisibility(textView: TextView, value: Boolean) {
    textView.visibility = if (value) View.VISIBLE else View.INVISIBLE
}

@BindingAdapter("setVisibility")
fun setVisibility(view: View, value: Boolean) {
    view.visibility = if (value) View.VISIBLE else View.GONE
}

@BindingAdapter("setOnclick")
fun setOnClick(view: View, listener: View.OnClickListener?) {
    view.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0
        private val DOUBLE_CLICK_TIME_DELTA: Long = 300

        override fun onClick(v: View?) {
            val curTime = System.currentTimeMillis()
            if (curTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                listener?.onClick(v)
            } else {
                listener?.onClick(v)
            }
            lastClickTime = curTime
        }
    })
}

@BindingAdapter("setFadeAnimVisibility")
fun setFadeAnimVisibility(view: View, visibility: Boolean) {
    if (visibility) {
        view.makeVisible()
        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        fadeIn.duration = 500
        fadeIn.start()
        return
    }
    val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
    fadeOut.duration = 200
    fadeOut.start()
    fadeOut.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            view.makeGone()
        }
    })
}



