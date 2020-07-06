package com.appwellteam.library.control.chartView.axis

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import com.appwellteam.library.control.chartView.callback.AxisCallback

import com.appwellteam.library.control.chartView.data.enumeration.PointLocation

/**
 * Created by Sambow on 16/5/31.
 */
class XAxis(callback: AxisCallback) : Axis(callback) {
    override fun drawAxis(pCanvas: Canvas, pPaint: Paint) {
        val tChartRect = callback.chartRectF
        val tPointLocation = callback.originPointLocation
        val tArrPointList = pointList
        var tTempXPosition = -1000f
        for (tPoint in tArrPointList) {
            pPaint.reset()
            pPaint.color = Color.LTGRAY
            pPaint.style = Paint.Style.STROKE
            pPaint.strokeWidth = 2f
            pPaint.isAntiAlias = true
            val effects = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 5f)
            pPaint.pathEffect = effects

            val tPath = Path()
            tPath.moveTo(tPoint.startX, tPoint.startY)
            tPath.lineTo(tPoint.endX, tPoint.endY)
            pCanvas.drawPath(tPath, pPaint)

            pPaint.reset()

            pPaint.isAntiAlias = true
            pPaint.color = Color.BLACK
            pPaint.textSize = textSize
            val tTextWidth = pPaint.measureText(tPoint.text)
            var tXPosition = tPoint.startX - tTextWidth / 2
            val tLeftPosition = callback.chartRectF.left
            tXPosition = if (tXPosition >= tLeftPosition) tXPosition else tLeftPosition

            if (tXPosition + tTextWidth > callback.chartRectF.right) {
                tXPosition = callback.chartRectF.right - tTextWidth
            }

            if (tXPosition > tTempXPosition) {
                tTempXPosition = tXPosition + tTextWidth

                if (tPointLocation == PointLocation.BOTTOM_LEFT || tPointLocation == PointLocation.BOTTOM_RIGHT) {
                    pCanvas.drawText(tPoint.text, tXPosition, tChartRect.bottom + (callback.xTextSpace + textSize) / 2, pPaint)
                } else {
                    pCanvas.drawText(tPoint.text, tXPosition, tChartRect.top - (callback.xTextSpace - textSize) / 2, pPaint)
                }
            }
        }
    }
}
