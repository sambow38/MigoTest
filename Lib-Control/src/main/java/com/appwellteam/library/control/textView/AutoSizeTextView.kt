package com.appwellteam.library.control.textView

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import com.appwellteam.library.control.AWTView

/**
 * Created by Sambow on 16/9/10.
 */
class AutoSizeTextView : AppCompatTextView, AWTView {
    private var textWidth = 0

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context)
    }

    override fun initialize(context: Context) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        textWidth = measuredWidthAndState
    }

    override fun onDraw(canvas: Canvas) {
        val size = textSize
        val width = paint.measureText(text.toString())
        if (width > textWidth) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, size - 1)
        } else {
            super.onDraw(canvas)
        }
    }
}
