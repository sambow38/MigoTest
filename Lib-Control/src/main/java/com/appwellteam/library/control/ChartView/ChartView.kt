package com.appwellteam.library.control.chartView

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import androidx.annotation.ColorInt
import com.appwellteam.library.control.AWTView
import com.appwellteam.library.control.chartView.callback.ChartViewCallback
import com.appwellteam.library.control.chartView.callback.LayerCallback
import com.appwellteam.library.control.chartView.data.ChartData
import com.appwellteam.library.control.chartView.data.enumeration.PointLocation
import com.appwellteam.library.control.chartView.util.ChartViewDefault
import com.appwellteam.library.control.chartView.layer.ChartLayer
import java.util.*

/**
 * Android-Library
 * Created by Sambow on 16/5/31.
 */
@Suppress("unused")
class ChartView : View, AWTView {
    private lateinit var layerList: MutableList<ChartLayer>
    private lateinit var paint: Paint
    private lateinit var point: Point

    private var measureWidth = -1
    private var measureHeight = -1

    private var isDrag = false
    private var clickListener: OnClickListener? = null
    private var chartViewCallback: ChartViewCallback? = null

    private var chartData: ChartData? = null
    private var viewCandleCount = ChartViewDefault.viewCandleCount
    private var xTagCount = ChartViewDefault.xTagCount

    private var borderColor = ChartViewDefault.borderColor
    private var borderWidth = ChartViewDefault.borderWidth

    private var yAxisTextOuter = ChartViewDefault.yAxisTextOuter

    @ColorInt
    private var colorUp = ChartViewDefault.colorUp
    @ColorInt
    private var colorDown = ChartViewDefault.colorDown
    @ColorInt
    private var colorEven = ChartViewDefault.colorEven
    @ColorInt
    private var color = ChartViewDefault.color

    private var showDot = ChartViewDefault.showDot
    private var draggable = ChartViewDefault.draggable

    //region constructor
    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)// api 21
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initialize(context)
    }

    override fun initialize(context: Context) {
        paint = Paint()
        point = Point(Integer.MIN_VALUE, Integer.MIN_VALUE)
        layerList = ArrayList()


        super.setOnClickListener(OnClickListener { v ->
            if (isDrag) {
                isDrag = false
                return@OnClickListener
            }
            clickListener?.onClick(v)
        })
        setOnLongClickListener { true }
    }
    //endregion

    //region override
    override fun setOnClickListener(l: OnClickListener?) {
        clickListener = l
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val tWidth = measureSize(widthMeasureSpec)
        val tHeight = measureSize(heightMeasureSpec)
        if (measureHeight != tHeight || measureWidth != tWidth) {
            measureHeight = tHeight
            measureWidth = tWidth
            setMeasuredDimension(tWidth, tHeight)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
    //endregion

    //region private function
    private fun measureSize(measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            val resources = context.resources
            val metrics = resources.displayMetrics
            result = (300 * (metrics.densityDpi / 160f)).toInt()
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
        }

        return result
    }

    private fun createRectF(left: Float = 0f, top: Float = 0f, right: Float = 0f, bottom: Float = 0f): RectF {
        return RectF(left, top, right, bottom)
    }
    //endregion

    //region layer
    fun addLayer(chartLayer: ChartLayer) {
        for (tLayer in layerList) {
            if (tLayer.id == chartLayer.id) {
                throw RuntimeException("ID is already exist!")
            }
        }

        chartLayer.setCallback(object : LayerCallback {
            override val viewCandleCount: Int
                get() = this@ChartView.viewCandleCount

            override val xTagCount: Int
                get() = this@ChartView.xTagCount

            override val colorUp: Int
                get() = this@ChartView.colorUp

            override val colorDown: Int
                get() = this@ChartView.colorDown

            override val colorEven: Int
                get() = this@ChartView.colorEven

            override val color: Int
                get() = this@ChartView.color

            override val yAxisTextOuter: Boolean
                get() = this@ChartView.yAxisTextOuter

            override val draggable: Boolean
                get() = this@ChartView.draggable

            override fun needRedraw() {
                reload()
            }

            override fun showDot(): Boolean {
                return showDot
            }
        })
        layerList.add(chartLayer)
        reload()
    }

    fun getLayer(pIndex: Int): ChartLayer? {
        return if (pIndex < layerList.size) {
            layerList[pIndex]
        } else {
            null
        }
    }

    fun getLayer(pID: String): ChartLayer? {
        return layerList.firstOrNull {
            it.id == pID
        }
    }

    fun removeLayer(pIndex: Int): Boolean {
        if (pIndex >= 0 && pIndex < layerList.size) {
            layerList.removeAt(pIndex)
            reload()
            return true
        }
        return false
    }

    fun removeLayer(layer: ChartLayer): Boolean {
        val findLayer = layerList.firstOrNull {
            it === layer
        } ?: return false

        return run {
            layerList.remove(findLayer)
            reload()
            true
        }
    }
    //endregion

    //region getter / setter
    fun setColorUp(@ColorInt color: Int) {
        colorUp = color
    }

    fun setColorDown(@ColorInt color: Int) {
        colorDown = color
    }

    fun setColorEven(@ColorInt color: Int) {
        colorEven = color
    }

    fun setColor(@ColorInt color: Int) {
        this.color = color
    }

    fun setShowDot(show: Boolean) {
        showDot = show
    }

    fun setDraggable(draggable: Boolean) {
        this.draggable = draggable
    }

    fun setChartViewCallback(callback: ChartViewCallback) {
        chartViewCallback = callback
    }

    fun setData(data: ChartData) {
        chartData = data
        reload(true)
    }

    fun setBorderColor(@ColorInt color: Int) {
        borderColor = color
    }

    fun setBorderWidth(width: Float) {
        borderWidth = width
    }

    fun setYAxisTextOuter(outer: Boolean) {
        yAxisTextOuter = outer
    }

    fun setViewCandleCount(count: Int) {
        viewCandleCount = count
    }

    fun setXTagCount(count: Int) {
        xTagCount = count
    }

    //endregion

    //region api
    @JvmOverloads
    fun reload(pNeedResetData: Boolean = false) {
        if (pNeedResetData) {
            val tSize = layerList.size
            for (ii in 0 until tSize) {
                layerList[ii].setData(ChartData(chartData?: continue))
            }
        } else {
            invalidate()
        }
        // postInvalidate();
    }
    //endregion

    //region draw
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = borderColor
        paint.strokeWidth = borderWidth
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE

        val tChartRect = createRectF(
                left = left + width * ChartMargin,
                top = top + height * ChartMargin,
                right = left + width * (1 - ChartMargin),
                bottom = top + height * (1 - ChartMargin))

        var textSpaceX = width * SpaceRate
        var textSpaceY = width * SpaceRate

        if (yAxisTextOuter) {
            for (tLayer in layerList) {
                when (tLayer.originPointLocation) {
                    PointLocation.BOTTOM_LEFT -> {
                        tChartRect.left = left + width * SpaceRate
                        tChartRect.bottom = top + height * (1 - SpaceRate)
                        textSpaceY = tChartRect.left - left
                        textSpaceX = bottom - tChartRect.bottom
                    }
                    PointLocation.BOTTOM_RIGHT -> {
                        tChartRect.right = left + width * (1 - SpaceRate)
                        tChartRect.bottom = top + height * (1 - SpaceRate)
                        textSpaceY = right - tChartRect.right
                        textSpaceX = bottom - tChartRect.bottom
                    }
                    PointLocation.TOP_LEFT -> {
                        tChartRect.left = left + width * SpaceRate
                        tChartRect.top = top + height * SpaceRate
                        textSpaceY = tChartRect.left - left
                        textSpaceX = tChartRect.top - top
                    }
                    PointLocation.TOP_RIGHT -> {
                        tChartRect.right = left + width * (1 - SpaceRate)
                        tChartRect.top = top + height * SpaceRate
                        textSpaceY = right - tChartRect.right
                        textSpaceX = tChartRect.top - top
                    }
                }
            }
        } else {
            for (tLayer in layerList) {
                when (tLayer.originPointLocation) {
                    PointLocation.BOTTOM_LEFT -> {
                        tChartRect.bottom = top + height * (1 - SpaceRate)
                        textSpaceY = width * SpaceRate
                        textSpaceX = bottom - tChartRect.bottom
                    }
                    PointLocation.BOTTOM_RIGHT -> {
                        tChartRect.bottom = top + height * (1 - SpaceRate)
                        textSpaceY = width * SpaceRate
                        textSpaceX = bottom - tChartRect.bottom
                    }
                    PointLocation.TOP_LEFT -> {
                        tChartRect.top = top + height * SpaceRate
                        textSpaceY = width * SpaceRate
                        textSpaceX = tChartRect.top - top
                    }
                    PointLocation.TOP_RIGHT -> {
                        tChartRect.top = top + height * SpaceRate
                        textSpaceY = width * SpaceRate
                        textSpaceX = tChartRect.top - top
                    }
                }
            }
        }

        val tSpaceRect = createRectF(
                left = tChartRect.left - left,
                top = tChartRect.top - top,
                right = right - tChartRect.right,
                bottom = bottom - tChartRect.bottom)

        val drawRectF = createRectF(
                left = tSpaceRect.left,
                top = tSpaceRect.top,
                right = tSpaceRect.left + tChartRect.width(),
                bottom = tSpaceRect.top + tChartRect.height())

        canvas.drawRect(drawRectF, paint)

        for (tLayer in layerList) {
            tLayer.initializeRect(tChartRect, tSpaceRect, textSpaceX, textSpaceY)
            tLayer.drawLayer(canvas, paint, point)
        }

//        for (tLayer in layerList) {
//            tLayer.drawCrossText(canvas, paint)
//        }

        chartViewCallback?.didDrawFinished()
    }

    companion object {

        private const val SpaceRate = 3f / 40f
        private const val ChartMargin = 1f / 40f
    }

    //endregion
}
