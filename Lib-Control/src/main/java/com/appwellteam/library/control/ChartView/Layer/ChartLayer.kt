package com.appwellteam.library.control.chartView.layer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import androidx.annotation.ColorInt
import com.appwellteam.library.control.chartView.callback.BasicLayerCallback
import com.appwellteam.library.control.chartView.callback.CoordinateCallback
import com.appwellteam.library.control.chartView.callback.LayerCallback
import com.appwellteam.library.control.chartView.data.CandleData
import com.appwellteam.library.control.chartView.data.ChartData
import com.appwellteam.library.control.chartView.data.enumeration.LayerType
import com.appwellteam.library.control.chartView.data.enumeration.PointLocation
import com.appwellteam.library.control.chartView.util.ChartViewDefault
import com.appwellteam.library.control.chartView.axis.coordinate.Coordinate
import java.lang.reflect.InvocationTargetException

/**
 * Android-Library
 * Created by Sambow on 16/5/31.
 */

/**
 * Create a new Layer by the LayerType and RectF.
 *
 * RectF(left-offset, top-offset, width, height): All params just be 0 to 1,
 * that meaning the weight of each params.
 */
@Suppress("unused")
class ChartLayer(type: LayerType, rect: RectF, val id: String) {
    private var coordinate: Coordinate
    private var basicLayer: BasicLayer
    private var rectF: RectF

    private var showLayer = true
    private var showCoordinate = true

    private var callback: LayerCallback? = null
    private var chartData: ChartData? = null

    private val lineWidth = ChartViewDefault.lineWidth
    private val dotRadius = ChartViewDefault.dotRadius

    val originPointLocation: PointLocation
        get() = coordinate.originPointLocation

    init {
        rectF = checkRectF(rect)
        basicLayer = setType(type, false)
        coordinate = basicLayer.createCoordinate(object : CoordinateCallback {
            override val viewCandleCount: Int
                get() = callback?.viewCandleCount ?: ChartViewDefault.viewCandleCount

            override val layerType: LayerType
                get() = basicLayer.layerType

            override val xTagCount: Int
                get() = callback?.xTagCount ?: ChartViewDefault.xTagCount

            override val yAxisTextOuter: Boolean
                get() = callback?.yAxisTextOuter ?: ChartViewDefault.yAxisTextOuter

            override val draggable: Boolean
                get() = callback?.draggable ?: ChartViewDefault.draggable

            override fun getChartData(): ChartData? {
                return this@ChartLayer.chartData
            }
        })
    }

    private fun checkRectF(pRect: RectF): RectF {
        val tLeftOffset = checkRectLimit(pRect.left)
        val tTopOffset = checkRectLimit(pRect.top)
        var tWidth = checkRectLimit(pRect.right)
        var tHeight = checkRectLimit(pRect.bottom)
        if (tLeftOffset + tWidth > 1f) {
            tWidth = 1 - tLeftOffset
        }
        if (tTopOffset + tHeight > 1f) {
            tHeight = 1 - tTopOffset
        }

        return RectF(tLeftOffset, tTopOffset, tWidth, tHeight)
    }

    private fun checkRectLimit(pValue: Float): Float {
        return when {
            pValue > 1.0f -> 1.0f
            pValue < 0f -> 0f
            else -> pValue
        }
    }

    //region getter / setter
    fun setType(pType: LayerType) {
        basicLayer = setType(pType, true)
    }

    private fun setType(type: LayerType, needDraw: Boolean): BasicLayer {
        var basicLayer: BasicLayer? = null
        val tClass = type.objectClass
        try {
            basicLayer = tClass.getConstructor().newInstance()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }

        val result = basicLayer ?: LineLayer()

        result.setBasicLayerCallback(object : BasicLayerCallback {
            override val coordinate: Coordinate
                get() = this@ChartLayer.coordinate

            override val candleDataList: List<CandleData>
                get() = chartData?.candleData ?: ArrayList()

            override val colorUp: Int
                get() = callback?.colorUp ?: ChartViewDefault.colorUp

            override val colorDown: Int
                get() = callback?.colorDown ?: ChartViewDefault.colorDown

            override val colorEven: Int
                get() = callback?.colorEven ?: ChartViewDefault.colorEven

            override val color: Int
                get() = callback?.color ?: ChartViewDefault.color

            override fun showDot(): Boolean {
                return callback?.showDot() ?: ChartViewDefault.showDot
            }

            override val lineWidth: Float
                get() = this@ChartLayer.lineWidth

            override val dotRadius: Int
                get() = this@ChartLayer.dotRadius
        })

        if (needDraw) {
            setData(chartData?: return result)
            callback?.needRedraw()
        }
        return result
    }

    fun setCallback(pCallback: LayerCallback) {
        callback = pCallback
    }

    fun setData(chart: ChartData) {
        chartData = chart
        coordinate.setData(chartData)
        callback?.needRedraw()
    }

    fun setShowLayer(showLayer: Boolean)
    {
        this.showLayer = showLayer
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setXAxisTextColor(@ColorInt color: Int) {
        coordinate.xAxis.textColor = color
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setXAxisTextSize(size: Float) {
        coordinate.xAxis.textSize = size
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setXAxisShowLine(show: Boolean) {
        coordinate.xAxis.showLine = show
    }

    fun setXAxisShowPointLabel(show: Boolean) {
        coordinate.xAxis.showPointLabel = show
    }

    fun setXAxisConfig(@ColorInt color: Int, size: Float, showLine: Boolean) {
        setXAxisTextColor(color)
        setXAxisTextSize(size)
        setXAxisShowLine(showLine)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setYAxisTextColor(@ColorInt color: Int) {
        coordinate.yAxis.textColor = color
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setYAxisTextSize(size: Float) {
        coordinate.yAxis.textSize = size
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setYAxisShowLine(show: Boolean) {
        coordinate.yAxis.showLine = show
    }

    fun setYAxisShowPointLabel(show: Boolean) {
        coordinate.yAxis.showPointLabel = show
    }

    fun setYAxisConfig(@ColorInt color: Int, size: Float, showLine: Boolean) {
        setYAxisTextColor(color)
        setYAxisTextSize(size)
        setYAxisShowLine(showLine)
    }

    fun setYAxisIntervalCount(count: Int) {
        coordinate.yIntervalCount = count
    }

    fun setCoordinateStartIndex(shift: Int) {
        var index = coordinate.startIndex + shift
        if (index < 0) {
            index = 0
        }
        val size = chartData?.candleData?.size ?: 0
        val viewedSize = callback?.viewCandleCount ?: ChartViewDefault.viewCandleCount
        if (index + viewedSize > size) {
            index = size - viewedSize
        }
        coordinate.startIndex = index
    }

    //endregion

    //region draw
    fun initializeRect(pRect: RectF, pSpace: RectF, textSpaceX: Float, textSpaceY: Float) {
        if (showLayer) {
            coordinate.initialize(rectF, pRect, pSpace, textSpaceX, textSpaceY)
        }
    }

    fun drawLayer(pCanvas: Canvas, pPaint: Paint, pPoint: Point) {
        if (showLayer) {
            if (showCoordinate) {
                coordinate.drawCoordinate(pCanvas, pPaint)
            }
            basicLayer.drawLayer(pCanvas, pPaint, pPoint)
        }
    }

//    fun drawCrossText(canvas: Canvas, paint: Paint) {
//        //		basicLayer.drawCrossText(pCanvas, pPaint);
//    }

    companion object {
        const val CHART_LAYER_ID = "Chart_Layer_ID"
        const val BASIC_LAYER = "Basic_Layer"
    }
    //endregion
}
