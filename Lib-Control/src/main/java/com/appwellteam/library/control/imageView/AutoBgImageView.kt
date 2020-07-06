package com.appwellteam.library.control.imageView

import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LightingColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.appwellteam.library.control.AWTView
import com.appwellteam.library.control.chartView.ChartView
import com.appwellteam.library.extension.getThemeDrawable

/**
 * Created by Sambow on 16/1/19.
 */
@Suppress("unused")
class AutoBgImageView : AppCompatImageView, AWTView {
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
        val chartView = ChartView(context)
        chartView.setBorderWidth(1f)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        when (drawable) {
            is StateListDrawable -> super.setImageDrawable(drawable)
            null -> super.setImageDrawable(drawable)
            else -> {
                val layer = AutoBgButtonBackgroundDrawable(drawable)
                super.setImageDrawable(layer)
            }
        }
    }

    override fun setImageResource(pResource: Int) {
        this.setImageDrawable(context.getThemeDrawable(pResource))
    }

    private inner class AutoBgButtonBackgroundDrawable(d: Drawable) : LayerDrawable(arrayOf(d)) {
        // The color filter to apply when the button is pressed
        private var pressedFilter: ColorFilter = LightingColorFilter(Color.LTGRAY, 1)
        // Alpha value when the button is disabled
        private var disabledAlpha = 100

        override fun onStateChange(states: IntArray): Boolean {
            var enabled = false
            var pressed = false

            for (state in states) {
                if (state == android.R.attr.state_enabled)
                    enabled = true
                else if (state == android.R.attr.state_pressed)
                    pressed = true
            }

            mutate()
            if (enabled && pressed) {
                colorFilter = pressedFilter
            } else if (!enabled) {
                colorFilter = null
                alpha = disabledAlpha
            } else {
                colorFilter = null
            }

            invalidateSelf()

            return super.onStateChange(states)
        }

        override fun isStateful(): Boolean {
            return true
        }
    }
}
