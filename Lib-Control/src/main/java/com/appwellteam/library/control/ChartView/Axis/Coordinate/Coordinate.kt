package com.appwellteam.library.control.chartView.axis.coordinate

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.appwellteam.library.AWTApplication
import com.appwellteam.library.common.AWTMath
import com.appwellteam.library.control.chartView.axis.Axis
import com.appwellteam.library.control.chartView.axis.Point
import com.appwellteam.library.control.chartView.axis.XAxis
import com.appwellteam.library.control.chartView.axis.YAxis
import com.appwellteam.library.control.chartView.callback.AxisCallback
import com.appwellteam.library.control.chartView.callback.CoordinateCallback
import com.appwellteam.library.control.chartView.data.ChartData
import com.appwellteam.library.control.chartView.data.enumeration.LayerType
import com.appwellteam.library.control.chartView.data.enumeration.PointLocation
import com.appwellteam.library.control.chartView.util.Tools
import com.appwellteam.library.extension.convertDpToPixel
import com.appwellteam.library.extension.digit
import java.util.*

/**
 * Created by Sambow on 16/5/31.
 */
@Suppress("unused")
class Coordinate(val callback: CoordinateCallback) {
    internal var xAxis: Axis
        private set
    internal var yAxis: Axis
        private set
    internal val originPointLocation = PointLocation.BOTTOM_RIGHT
//    private var callback: CoordinateCallback? = null

    internal var startIndex = 0
    private var queryCount = 0

    internal var xFirstPosition = 0f
    internal var xInterval = 0f
    private var yFirstPosition = 0f
    private var yInterval = 0f
    internal var yIntervalCount = -1
    private var yIntervalStandard = 0f
    internal var maxValue = 0f
    internal var minValue = 0f

    internal lateinit var layerRect: RectF
    private var chartRect: RectF? = null

    private var xTextSpace = 0f
    private var yTextSpace = 0f

    private var dynamicDigit = false
    private var digit = 2
    private val padding = AWTApplication.app?.convertDpToPixel(8f) ?: 8f

    init {
        val callback = object : AxisCallback {
            override val chartRectF: RectF
                get() = this@Coordinate.layerRect

            override val originPointLocation: PointLocation
                get() = this@Coordinate.originPointLocation

            override val xTextSpace: Float
                get() = this@Coordinate.getXTextSpace()

            override val yTextSpace: Float
                get() = this@Coordinate.getYTextSpace()

            override val yAxisTextOuter: Boolean
                get() = callback.yAxisTextOuter
        }
        xAxis = XAxis(callback)
        yAxis = YAxis(callback)
        layerRect = RectF(0f, 0f, 0f, 0f)
    }

    //region getter / setter
    fun getXTextSpace(): Float {
        return xTextSpace
    }

    fun getYTextSpace(): Float {
        return yTextSpace
    }

    fun setYAxis(axis: Axis) {
        axis.callback = yAxis.callback
        yAxis = axis
    }

    fun setData(chartData: ChartData?) {
        val tXList = ArrayList<Point>()
        val tYList = ArrayList<Point>()

        if (chartData != null) {
            val tLayerType = callback.layerType

            val tDataList = chartData.candleData
            val tDataCount = tDataList.size

            queryCount = callback.viewCandleCount
            queryCount = if (queryCount == 0 || tDataCount < queryCount) tDataCount else queryCount

            val tStartIndex = tDataCount - queryCount
            startIndex = tStartIndex
            var nXTagCount = callback.xTagCount - 1 // 間隔要將想要顯示的數量-1,
            if (nXTagCount == 0) {
                nXTagCount = 1
            }

            val tXTagCount = if (queryCount < nXTagCount) 1 else queryCount / nXTagCount // 每間隔多少畫一根

            var tMaxValue = 0f
            var tMinValue = 0f

            var tempStartIndex = 0
            if (!callback.draggable) {
                // 如果不可拖拉, y軸只拿可視資料來找最大最小值,
                // 如果可以拖拉, y軸就拿全部的資料找最大最小值
                tempStartIndex = tStartIndex
            }

            var value: Float
            for (i in tempStartIndex until tDataCount) {
                val tData = tDataList[i]

                value = if (tLayerType === LayerType.LINE) {
                    tData.close
                } else {
                    tData.value
                }

                if (i == tempStartIndex) {
                    tMaxValue = value
                    tMinValue = value
                } else {
                    if (value > tMaxValue) {
                        tMaxValue = value
                    }
                    if (value < tMinValue) {
                        tMinValue = value
                    }
                }


                if (dynamicDigit) {
                    digit = Math.max(digit, value.digit)
                }

                if ((i - tempStartIndex) % tXTagCount == 0 || i - tempStartIndex == tDataCount - tempStartIndex - 1) { //間隔到了或是資料最後一根
                    tXList.add(Point(0f, 0f, 0f, 0f, true, tData.title, i + 1 - tempStartIndex))
                }
            }


            if (yIntervalCount == -1) { // not set yet
                // use default
                yIntervalCount = 5
            }
            yIntervalStandard = (tMaxValue - tMinValue) / yIntervalCount.toFloat()
            val tTempStandard = yIntervalStandard // 每個間隔的大小

            for (i in 0..yIntervalCount) {
                var tCurrentValue = AWTMath.getFormatString(tMaxValue - tTempStandard * (yIntervalCount - i), digit)
                tCurrentValue = yAxis.getPrefix() + tCurrentValue + yAxis.getSuffix()
                tYList.add(Point(0f, 0f, 0f, 0f, true, tCurrentValue, i))
            }

            maxValue = tMaxValue
            minValue = tMinValue
        }

        xAxis.pointList = tXList
        yAxis.pointList = tYList
    }
    //endregion

    //region draw
    fun initialize(pWeight: RectF, pChartPanel: RectF, pSpace: RectF, textSpaceX: Float, textSpaceY: Float) {
        initialize(pWeight, pChartPanel, pSpace, textSpaceX, textSpaceY, false)
    }

    fun initialize(pWeight: RectF, pChartPanel: RectF, pSpace: RectF, textSpaceX: Float, textSpaceY: Float, autoResizeText: Boolean) {
        layerRect = Tools.convertToRect(pWeight, pChartPanel, pSpace)
        chartRect = RectF(pSpace.left, pSpace.top, pSpace.left + layerRect.width(), pSpace.top + layerRect.height())

        xTextSpace = textSpaceX
        yTextSpace = textSpaceY


        var tXInterval = 0f
        if (queryCount - 1 > 0) {
            tXInterval = (layerRect.width() - 2 * padding) / (queryCount - 1)
        }
        xInterval = tXInterval

        val tYList = yAxis.pointList
        val tYListSize = tYList.size

        yInterval = if (maxValue - minValue == 0f) {
            0f
        } else {
            layerRect.height() * yIntervalStandard / (maxValue - minValue)
        }
        yFirstPosition = if (tYListSize > 0) layerRect.bottom - (layerRect.height() - yInterval * yIntervalCount) else layerRect.bottom

        xFirstPosition = layerRect.left + padding

        if (autoResizeText) {
            val tXTextPadding = xTextSpace / 8
            val tYTextPadding = yTextSpace / 6

            var tTextSize = 12f
            val tYWidth = yTextSpace - tYTextPadding * 2
            val tXWidth = xTextSpace - tXTextPadding * 2
            for (ii in 0 until tYListSize) {
                val tPoint = tYList[ii]

                if (tPoint.text != "") {
                    val tTempTextSize = Tools.determineMaxTextSize(tPoint.text, tYWidth)
                    tTextSize = if (tTempTextSize >= tXWidth) tXWidth else tTempTextSize
                }
            }
            xAxis.textSize = tTextSize
            yAxis.textSize = tTextSize
        }
    }

    fun drawCoordinate(pCanvas: Canvas, pPaint: Paint) {
        // 可拖拉的情況下 x軸要看資料來顯示, 若不可拖拉則拿原本的 x軸 point 即可
        //        if (callback.isDraggable()) {
        val tXList = ArrayList<Point>()
        val tChartData = callback.getChartData() ?: return
        val tDataList = tChartData.candleData
        val endIndex = startIndex + callback.viewCandleCount
        for (i in startIndex until endIndex) {
            if (i < tDataList.size) {
                val candle = tDataList[i]
                if (candle.showTitle) {
                    val interval = i - startIndex
                    val tXPoint = interval * xInterval + layerRect.left + padding
                    val p = Point(tXPoint, layerRect.bottom, tXPoint, layerRect.top, true, candle.title, interval)
                    tXList.add(p)
                }
            }
        }
        xAxis.pointList = tXList
        //        } else {
        //            List<Point> tXList = xAxis.getPointList();
        //            int tXListSize = tXList.size();
        //            for (int ii = 0; ii < tXListSize; ii++) {
        //                Point tPoint = tXList.get(ii);
        //                float tXPoint = tPoint.getIntervalCount() * xInterval + layerRect.left;
        //                tPoint.setStartY(layerRect.bottom);
        //                tPoint.setEndY(layerRect.top);
        //                tPoint.setStartX(tXPoint);
        //                tPoint.setEndX(tXPoint);
        //            }
        //        }

        val tYList = yAxis.pointList
        val tYListSize = tYList.size
        for (ii in 0 until tYListSize) {
            val tPoint = tYList[ii]
            val tYPoint = yFirstPosition - ii * yInterval
            tPoint.startY = tYPoint
            tPoint.endY = tYPoint
            tPoint.startX = layerRect.right
            tPoint.endX = layerRect.left
        }

        xAxis.drawAxis(pCanvas, pPaint)
        yAxis.drawAxis(pCanvas, pPaint)
    }
    //endregion
}
