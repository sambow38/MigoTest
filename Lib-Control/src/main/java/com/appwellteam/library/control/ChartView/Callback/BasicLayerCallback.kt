package com.appwellteam.library.control.chartView.callback


import androidx.annotation.ColorInt

import com.appwellteam.library.control.chartView.axis.coordinate.Coordinate
import com.appwellteam.library.control.chartView.data.CandleData

/**
 * Created by Sambow on 16/5/31.
 */
interface BasicLayerCallback {
    val coordinate: Coordinate
    val candleDataList: List<CandleData>
    @get:ColorInt
    val colorUp: Int
    @get:ColorInt
    val colorDown: Int
    @get:ColorInt
    val colorEven: Int
    @get:ColorInt
    val color: Int

    fun showDot(): Boolean

    val lineWidth: Float
    val dotRadius: Int
}
