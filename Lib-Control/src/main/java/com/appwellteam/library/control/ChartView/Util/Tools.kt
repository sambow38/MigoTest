package com.appwellteam.library.control.chartView.util

import android.graphics.Paint
import android.graphics.RectF

/**
 * Android-Library
 * Created by Sambow on 16/5/31.
 */
object Tools {
    /**
     * 計算字體大小
     *
     * @param str
     * 顯示文字
     * @param maxWidth
     * 長度範圍
     * @return 字體大小
     */
    fun determineMaxTextSize(str: String, maxWidth: Float): Float {
        var size = 0
        val paint = Paint()

        do {
            paint.textSize = (++size).toFloat()
        } while (paint.measureText(str) < maxWidth)

        // 精確到小數第一位
        var tSize = size * 1f
        do {
            tSize += 0.1f
            paint.textSize = tSize
        } while (paint.measureText(str) < maxWidth)
        return tSize
    }

    fun convertToRect(pRect: RectF, pPanelRect: RectF, pSpace: RectF): RectF {
        val tPanelWidth = pPanelRect.width()
        val tPanelHeight = pPanelRect.height()
        val tLeft = tPanelWidth * pRect.left + pSpace.left
        val tTop = tPanelHeight * pRect.top + pSpace.top
        val tRight = tPanelWidth * pRect.right + tLeft
        val tBottom = tPanelHeight * pRect.bottom + tTop
        return RectF(tLeft, tTop, tRight, tBottom)
    }
}
