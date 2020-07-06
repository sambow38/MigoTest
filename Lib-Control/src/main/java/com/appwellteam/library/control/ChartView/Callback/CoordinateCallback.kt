package com.appwellteam.library.control.chartView.callback

import com.appwellteam.library.control.chartView.data.ChartData
import com.appwellteam.library.control.chartView.data.enumeration.LayerType

/**
 * Created by Sambow on 16/5/31.
 */
interface CoordinateCallback {
    val viewCandleCount: Int
    val layerType: LayerType
    val xTagCount: Int
    val yAxisTextOuter: Boolean
    val draggable: Boolean
    fun getChartData(): ChartData?
}
