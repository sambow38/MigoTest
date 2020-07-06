package com.appwellteam.library.control.wheelView.adapter

import com.appwellteam.library.model.AWTItem

import kankan.wheel.widget.WheelAdapter

/**
 * Created by Sambow on 16/8/5.
 */
class BaseWheelAdapter(private val items: List<AWTItem>) : WheelAdapter {
    override var maximumLength: Int = 0
        private set

    override val itemsCount: Int
        get() = items.size

    init {

        for (item in this.items) {
            val length = item.title.length
            if (length > maximumLength) {
                maximumLength = length
            }
        }
    }

    override fun getItem(index: Int): String {
        return items[index].title
    }
}
