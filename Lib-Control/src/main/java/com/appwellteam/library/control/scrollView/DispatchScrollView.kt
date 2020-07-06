package com.appwellteam.library.control.scrollView

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

import com.appwellteam.library.control.AWTView

/**
 * Created by Sambow on 16/1/11.
 */
@Suppress("unused")
class DispatchScrollView : ScrollView, AWTView {
    private var scrollable = true

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initialize(context)
    }

    override fun initialize(context: Context) {

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (scrollable) {
            super.onTouchEvent(event)
        } else {
            false
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (scrollable) {
            super.onInterceptTouchEvent(event)
        } else {
            false
        }
    }
}
