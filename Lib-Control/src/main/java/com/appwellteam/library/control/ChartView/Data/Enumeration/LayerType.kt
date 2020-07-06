package com.appwellteam.library.control.chartView.data.enumeration

import com.appwellteam.library.control.chartView.layer.BasicLayer
import com.appwellteam.library.control.chartView.layer.LineLayer

/**
 * Created by Sambow on 16/6/1.
 */
@Suppress("unused")
enum class LayerType(private val ID: Int, val objectClass: Class<out BasicLayer>) {
    NONE(0, LineLayer::class.java),
    LINE(1, LineLayer::class.java);
    //	K_BAR(2, KBarLayer.class),
    //	MA_CD(3, MACDLayer.class),
    //	AREA(4, AreaLayer.class),
    //	MA(5, MALayer.class),
    //	BAR(6, BarLayer.class),
    //	DAILY(7, DailyLayer.class),
    //	AVG(8, AvgLayer.class);


    companion object {

        internal fun fromID(pID: Int): LayerType {
            for (tType in values()) {
                if (tType.ID == pID) {
                    return tType
                }
            }
            return NONE
        }
    }
}
