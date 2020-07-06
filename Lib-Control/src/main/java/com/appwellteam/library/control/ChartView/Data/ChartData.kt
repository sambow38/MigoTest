package com.appwellteam.library.control.chartView.data

import java.util.ArrayList

/**
 * Created by Sambow on 16/5/31.
 */
@Suppress("unused")
class ChartData() {
    private var list: MutableList<CandleData> = ArrayList()

    internal val candleData: List<CandleData>
        get() = list

    constructor(chartData: ChartData) : this() {
        val tCandleData = ArrayList(chartData.candleData)

        for (candle in tCandleData) {
            list.add(candle.clone())
        }
    }

    fun addCandleData(data: CandleData) {
        list.add(data)
    }

    fun addCandleData(index: Int, data: CandleData) {
        list.add(index, data)
    }

}
