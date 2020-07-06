package com.appwellteam.library.control.chartView.layer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point

import com.appwellteam.library.control.chartView.axis.coordinate.Coordinate
import com.appwellteam.library.control.chartView.callback.BasicLayerCallback
import com.appwellteam.library.control.chartView.callback.CoordinateCallback
import com.appwellteam.library.control.chartView.data.CandleData
import com.appwellteam.library.control.chartView.data.enumeration.LayerType

/**
 * Android-Library
 * Created by Sambow on 16/5/31.
 */
abstract class BasicLayer {
    protected var callback: BasicLayerCallback? = null
    protected val dbNone = java.lang.Float.MIN_VALUE

    internal abstract val layerType: LayerType

    internal fun createCoordinate(callback: CoordinateCallback): Coordinate {
        return Coordinate(callback)
    }

    internal fun convertDate(list: List<CandleData>): List<CandleData> {
        return list
    }

    //region getter / setter
    internal fun setBasicLayerCallback(callback: BasicLayerCallback) {
        this.callback = callback
    }
    //endregion

    //region draw
    internal fun drawLayer(canvas: Canvas, paint: Paint, point: Point) {
        onDrawLayer(canvas, paint, point)
    }

    internal abstract fun onDrawLayer(canvas: Canvas, paint: Paint, point: Point)
    //endregion
}