package com.appwellteam.library.control.chartView.data

/**
 * Created by Sambow on 16/5/31.
 */
@Suppress("MemberVisibilityCanBePrivate")
class CandleData {
    var open = 0f
    var high = 0f
    var low = 0f
    var close = 0f
    var value = 0f
    var volume: Long = 0

    var change = 0f
    var changePercent = 0f
    var date: Long = 0

    var showTitle = false
    var title = ""

    var background = true
//    var otherData: Any
//    var specialDraw: ISpecialDraw

    fun update(candle: CandleData) {
        this.open = candle.open
        this.high = candle.high
        this.low = candle.low
        this.close = candle.close
        this.value = candle.value
        this.volume = candle.volume
        this.change = candle.change
        this.date = candle.date
        this.changePercent = candle.changePercent
        this.showTitle = candle.showTitle
        this.title = candle.title
        this.background = candle.background
//        this.m_otherData = candle.m_otherData
//        this.specialDraw = candle.specialDraw
    }

    fun clone(): CandleData {
        val tData = CandleData()
        tData.update(this)
        return tData
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("[ Open: ").append(open).append(", ")
        sb.append("High: ").append(high).append(", ")
        sb.append("Low: ").append(low).append(", ")
        sb.append("Close: ").append(close).append(", ")
        sb.append("Value: ").append(value).append(", ")
        sb.append("Volume: ").append(volume).append(", ")
        sb.append("Change: ").append(change).append(", ")
        sb.append("ChangePercent: ").append(changePercent).append(", ")
        sb.append("DateInMillis: ").append(date).append(", ")
        sb.append("ShowTitle: ").append(showTitle).append(", ")
        sb.append("Title: ").append(title).append(", ")
        sb.append("DrawBackground: ").append(background).append(" ]")
        return sb.toString()
    }
}
