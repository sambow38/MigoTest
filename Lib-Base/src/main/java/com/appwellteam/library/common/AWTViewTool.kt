package com.appwellteam.library.common

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt

/**
 * Created by Sambow on 16/8/16.
 */
@Suppress("unused")
object AWTViewTool {
    fun calculatePixelBy360Dp(dp: Float): Int {
        return calculatePixelByDp(dp, 360f)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun calculatePixelByDp(dp: Float, total: Float): Int {
        val screenWidth = AWTAppInfo.screenWidth
        return (dp * screenWidth / total).toInt()
    }

    @JvmOverloads
    fun calculateColumnWidth(scale: IntArray, viewMargin: Int = 0, columnMargin: Int = 0): IntArray {
        val columnCount = scale.size
        if (columnCount == 0) {
            return IntArray(0)
        } else {
            val screenWidth = AWTAppInfo.screenWidth
            var totalScale = 0
            for (temp in scale) {
                totalScale += Math.abs(temp)
            }

            val screenScale = totalScale + viewMargin * 2 + columnMargin * (columnCount - 1)

            val tempViewMargin = (viewMargin.toFloat() * screenWidth.toFloat() * 1f / screenScale).toInt()
            val tempColumnMargin = (columnMargin.toFloat() * screenWidth.toFloat() * 1f / screenScale).toInt()

            var totalColumnWidth = screenWidth - tempViewMargin * 2
            totalColumnWidth -= tempColumnMargin * (columnCount - 1)

            if (totalScale == 0) return IntArray(0)

            val result = IntArray(columnCount)
            for (i in 0 until columnCount) {
                result[i] = (totalColumnWidth.toFloat() * scale[i].toFloat() * 1f / totalScale).toInt()
            }
            return result
        }
    }

    fun setBackgroundDrawable(view: View, drawable: Drawable?) {
        when (Build.VERSION.SDK_INT) {
            in Build.VERSION_CODES.JELLY_BEAN..Int.MAX_VALUE -> view.background = drawable
            else -> @Suppress("DEPRECATION") view.setBackgroundDrawable(drawable)
        }
    }

    fun getRoundDrawable(pxRadius: Int, pxBorder: Int, @ColorInt colorBg: Int, @ColorInt colorBorder: Int): Drawable {
        val drawable = GradientDrawable()
        drawable.setColor(colorBg)
        drawable.cornerRadius = pxRadius.toFloat()
        if (pxBorder >= 0) {
            drawable.setStroke(pxBorder, colorBorder)
        }
        return drawable
    }
}
