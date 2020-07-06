package com.appwellteam.library.control.chartView.axis

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import com.appwellteam.library.control.chartView.callback.AxisCallback

import com.appwellteam.library.control.chartView.data.enumeration.PointLocation
import com.appwellteam.library.control.chartView.util.Tools

/**
 * Created by Sambow on 16/5/31.
 */
class YAxis(callback: AxisCallback) : Axis(callback) {
    override fun drawAxis(pCanvas: Canvas, pPaint: Paint) {
        val tChartRect = callback.chartRectF

        val tPointLocation = callback.originPointLocation

        val tArrPointList = pointList
        val tCulOffset = 1
        for (tPoint in tArrPointList) {
            pPaint.reset()
            pPaint.color = Color.LTGRAY
            pPaint.style = Paint.Style.STROKE
            pPaint.strokeWidth = 2f
            pPaint.isAntiAlias = true
            val effects = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 5f)
            pPaint.pathEffect = effects

            if (tPoint.startY - tChartRect.top > tCulOffset && tChartRect.bottom - tPoint.startY > tCulOffset) {
                val tPath = Path()
                tPath.moveTo(tPoint.startX, tPoint.startY)
                tPath.lineTo(tPoint.endX, tPoint.endY)
                pCanvas.drawPath(tPath, pPaint)
            }

            pPaint.reset()

            pPaint.isAntiAlias = true
            pPaint.color = Color.BLACK
            pPaint.textSize = textSize

            var tYBaseLine = tPoint.startY + textSize / 2f
            val tYTop = tYBaseLine + textSize
            if (tYTop < tChartRect.top) {
                tYBaseLine = tChartRect.top + textSize
            } else if (tYBaseLine > tChartRect.bottom) {
                tYBaseLine = tChartRect.bottom
            }

            if (callback.yAxisTextOuter) {
                when (tPointLocation) {
                    PointLocation.TOP_RIGHT -> pCanvas.drawText(tPoint.text, tChartRect.right + (callback.yTextSpace - pPaint.measureText(tPoint.text)) / 2, tYBaseLine, pPaint)
                    PointLocation.BOTTOM_RIGHT -> pCanvas.drawText(tPoint.text, tChartRect.right + (callback.yTextSpace - pPaint.measureText(tPoint.text)) / 2, tYBaseLine, pPaint)
                    PointLocation.TOP_LEFT -> pCanvas.drawText(tPoint.text, tChartRect.left - (callback.yTextSpace + pPaint.measureText(tPoint.text)) / 2, tYBaseLine, pPaint)
                    PointLocation.BOTTOM_LEFT -> pCanvas.drawText(tPoint.text, tChartRect.left - (callback.yTextSpace + pPaint.measureText(tPoint.text)) / 2, tYBaseLine, pPaint)
                }
            } else {
                when (tPointLocation) {
                    PointLocation.TOP_RIGHT -> pCanvas.drawText(tPoint.text, tChartRect.right - callback.yTextSpace / 6f - pPaint.measureText(tPoint.text), tYBaseLine, pPaint)
                    PointLocation.BOTTOM_RIGHT -> pCanvas.drawText(tPoint.text, tChartRect.right - callback.yTextSpace / 6f - pPaint.measureText(tPoint.text), tYBaseLine, pPaint)
                    PointLocation.TOP_LEFT -> pCanvas.drawText(tPoint.text, tChartRect.left + callback.yTextSpace / 6f, tYBaseLine, pPaint)
                    PointLocation.BOTTOM_LEFT -> pCanvas.drawText(tPoint.text, tChartRect.left + callback.yTextSpace / 6f, tYBaseLine, pPaint)
                }
            }
        }

        if (showTitle) {
            val tTextSize = Tools.determineMaxTextSize(title, callback.yTextSpace * 9 / 10)
            pPaint.textSize = tTextSize

            if (tPointLocation == PointLocation.TOP_RIGHT || tPointLocation == PointLocation.BOTTOM_RIGHT) {
                pCanvas.drawText(title, tChartRect.right + (callback.yTextSpace - pPaint.measureText(title)) / 2, tChartRect.top + tTextSize, pPaint)
            } else {
                pCanvas.drawText(title, tChartRect.left - (callback.yTextSpace + pPaint.measureText(title)) / 2, tChartRect.top + tTextSize, pPaint)
            }
        }
    }
}
