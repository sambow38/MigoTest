package com.appwellteam.library.control.chartView.axis

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.annotation.ColorInt
import com.appwellteam.library.AWTApplication
import com.appwellteam.library.control.chartView.callback.AxisCallback
import com.appwellteam.library.extension.convertDpToPixel
import java.util.*

/**
 * Created by Sambow on 16/5/31.
 */
abstract class Axis(var callback: AxisCallback) {

    internal var textSize = AWTApplication.app?.convertDpToPixel(13f) ?: 13f
    @ColorInt
    internal var textColor = Color.BLACK

    internal var pointList: List<Point> = ArrayList()

    internal var title = ""
    internal var showTitle = false
    internal var showLine = true
    internal var showPointLabel = true

    abstract fun drawAxis(pCanvas: Canvas, pPaint: Paint)

    //region getter / setter

    fun getSuffix(): String {
        return ""
    }

    fun getPrefix(): String {
        return ""
    }

    //endregion
}
