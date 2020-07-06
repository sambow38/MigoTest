package com.appwellteam.library.control.chartView.callback

import android.graphics.RectF

import com.appwellteam.library.control.chartView.data.enumeration.PointLocation

/**
 * Created by Sambow on 16/5/31.
 */
interface AxisCallback {
    val chartRectF: RectF
    val originPointLocation: PointLocation
    val xTextSpace: Float
    val yTextSpace: Float
    val yAxisTextOuter: Boolean
}
