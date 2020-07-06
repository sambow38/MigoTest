package com.appwellteam.library.control.chartView.callback


import androidx.annotation.ColorInt

/**
 * Created by Sambow on 16/5/31.
 */
interface LayerCallback {
    val viewCandleCount: Int
    val xTagCount: Int
    @get:ColorInt
    val colorUp: Int
    @get:ColorInt
    val colorDown: Int
    @get:ColorInt
    val colorEven: Int
    @get:ColorInt
    val color: Int
    val yAxisTextOuter: Boolean
    val draggable: Boolean
    fun needRedraw()
    fun showDot(): Boolean
}
