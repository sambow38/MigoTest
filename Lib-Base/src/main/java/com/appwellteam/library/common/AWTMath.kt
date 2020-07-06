package com.appwellteam.library.common

import android.graphics.Point
import android.graphics.PointF

import java.util.Locale

@Suppress("unused")
object AWTMath {
    fun distance(point1: Point, point2: Point): Double {
        return distance(point1.x.toFloat(), point1.y.toFloat(), point2.x.toFloat(), point2.y.toFloat())
    }

    fun distance(point1: PointF, point2: PointF): Double {
        return distance(point1.x, point1.y, point2.x, point2.y)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        return Math.sqrt(Math.pow((x1 - x2).toDouble(), 2.0) + Math.pow((y1 - y2).toDouble(), 2.0))
    }

    fun distanceF(point1: Point, point2: Point): Float {
        return distanceF(point1.x.toFloat(), point1.y.toFloat(), point2.x.toFloat(), point2.y.toFloat())
    }

    fun distanceF(point1: PointF, point2: PointF): Float {
        return distanceF(point1.x, point1.y, point2.x, point2.y)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun distanceF(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return distance(x1, y1, x2, y2).toFloat()
    }


    /**
     * 取得指定小數位數的小數位數
     *
     * @param value
     * @return 小數位數
     */
    fun getDigit(value: Float): Int {
        if (value.toString().contains("E")) {
            val afterDot = value.toString().split("[.]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].length
            val afterE = value.toString().split("E".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].length
            var decimal = afterDot - afterE - 1
            val multiple = Integer.parseInt(value.toString().split("E".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
            decimal -= multiple
            return if (decimal < 0) 0 else decimal
        } else if (value.toString().contains(".")) {
            return value.toString().split("[.]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].length
        }
        return 0
    }

    /**
     * 取得指定小數位數的價格字串(超過四捨五入)
     *
     * @param value
     * @param digit 小數位數, 0代表整數
     * @return 字串
     */
    fun getFormatString(value: Double, digit: Int): String {
        var result = digit
        if (digit < 0)
            result = 2
        return String.format(Locale.getDefault(), "%1$." + result + "f", value)
    }

    fun getFormatString(value: Float, digit: Int): String {
        var result = digit
        if (digit < 0)
            result = 2
        return String.format(Locale.getDefault(), "%1$." + result + "f", value)
    }

    fun randomInt(): Int {
        return (Math.random() * Integer.MAX_VALUE).toInt()
    }
}
