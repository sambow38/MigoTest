package com.appwellteam.library.control.chartView.layer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import com.appwellteam.library.control.chartView.data.enumeration.LayerType
import com.appwellteam.library.control.chartView.util.ChartViewDefault

/**
 * Android-Library
 * Created by Sambow on 16/5/31.
 */
@Suppress("unused")
class LineLayer : BasicLayer() {
    override val layerType: LayerType
        get() = LayerType.LINE

    override fun onDrawLayer(canvas: Canvas, paint: Paint, point: Point) {
        val tCoordinate = callback?.coordinate ?: return
        val tDataList = convertDate(callback?.candleDataList ?: return)

        if (tDataList.isNotEmpty()) {
            val tListSize = tDataList.size
            val tStartIndex = tCoordinate.startIndex
            val tXFirstPosition = tCoordinate.xFirstPosition
            val tXInterval = tCoordinate.xInterval
            val tYFirstPosition = tCoordinate.layerRect.bottom
            val tYMaxValue = tCoordinate.maxValue
            val tYMinValue = tCoordinate.minValue
            val tYValueDist = tYMaxValue - tYMinValue
            val tYValueLength = tCoordinate.layerRect.bottom - tCoordinate.layerRect.top

            var tCurrentYPosition = 0f
            var tCurrentXPosition = 0f
            var tTempYPosition: Float
            var tTempXPosition: Float

            val tLineWidth = callback?.lineWidth ?: ChartViewDefault.lineWidth

            var tStartDraw = false

            var tDotColor: Int
            val tCulOffset = 1

            paint.reset()
            paint.isAntiAlias = true
            paint.color = callback?.color ?: ChartViewDefault.color
            paint.strokeWidth = tLineWidth
            paint.style = Paint.Style.FILL

            for (i in tStartIndex until tListSize) {
                if (tDataList[i].close == dbNone) {
                    continue
                }

                tTempXPosition = tCurrentXPosition
                tCurrentXPosition = tXFirstPosition + tXInterval * (i - tStartIndex)
                tTempYPosition = tCurrentYPosition
                tCurrentYPosition = tYFirstPosition - (tDataList[i].close - tYMinValue) * tYValueLength / tYValueDist

                tDotColor = when {
                    tCurrentYPosition > tTempYPosition -> callback?.colorDown
                            ?: ChartViewDefault.colorDown
                    tCurrentYPosition < tTempXPosition -> callback?.colorUp
                            ?: ChartViewDefault.colorUp
                    else -> callback?.colorEven ?: ChartViewDefault.colorEven
                }

                if (tStartDraw) {
                    val tRectF = tCoordinate.layerRect

                    val tTempUpper = tRectF.top - tCulOffset > tTempYPosition
                    val tTempLower = tRectF.bottom + tCulOffset < tTempYPosition
                    val tCurrentUpper = tRectF.top - tCulOffset > tCurrentYPosition
                    val tCurrentLower = tRectF.bottom + tCulOffset < tCurrentYPosition

                    if (!tTempUpper && !tTempLower && !tCurrentUpper && !tCurrentLower) {
                        canvas.drawLine(tTempXPosition, tTempYPosition, tCurrentXPosition, tCurrentYPosition, paint)
                    } else if ((tTempUpper || tTempLower) && !tCurrentUpper && !tCurrentLower) {
                        val middle: Float = if (tTempUpper) {
                            tRectF.top
                        } else {
                            tRectF.bottom
                        }
                        val tTempX = (tCurrentXPosition - tTempXPosition) * getScale(tTempYPosition, tCurrentYPosition, middle) + tTempXPosition
                        canvas.drawLine(tTempX, middle, tCurrentXPosition, tCurrentYPosition, paint)
                    } else if ((tCurrentUpper || tCurrentLower) && !tTempUpper && !tTempLower) {
                        val middle: Float = if (tCurrentUpper) {
                            tRectF.top
                        } else {
                            tRectF.bottom
                        }
                        val tTempX = (tCurrentXPosition - tTempXPosition) * getScale(tTempYPosition, tCurrentYPosition, middle) + tTempXPosition
                        canvas.drawLine(tTempXPosition, tTempYPosition, tTempX, middle, paint)
                    } else {
                    }
                } else {
                    tStartDraw = true
                }

                if (callback?.showDot() ?: ChartViewDefault.showDot) {
                    paint.color = tDotColor
                    canvas.drawCircle(tCurrentXPosition, tCurrentYPosition, tLineWidth * 2, paint)
                }
            }
        }
    }

    private fun getScale(start: Float, end: Float, middle: Float): Float {
        return (start - middle) / (start - end)
    }
}
