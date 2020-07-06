package com.appwellteam.library.control.wheelView

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import com.appwellteam.library.control.AWTView
import com.appwellteam.library.control.R
import com.appwellteam.library.control.wheelView.adapter.BaseWheelAdapter
import com.appwellteam.library.model.AWTItem
import kankan.wheel.widget.OnWheelScrollListener
import kankan.wheel.widget.WheelView
import java.util.*

/**
 * Created by sambow on 2018/4/2.
 */

@Suppress("unused")
class DateWheel : LinearLayout, AWTView {
    private lateinit var wheelYear: WheelView
    private lateinit var wheelMonth: WheelView
    private lateinit var wheelDay: WheelView

    private val years = ArrayList<AWTItem>()
    private var isScrolling = false

    var currentDate: Calendar
        get() {
            val year = Integer.parseInt(years[wheelYear.currentItem].value)
            val month = wheelMonth.currentItem
            val day = wheelDay.currentItem + 1
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            calendar.timeInMillis
            return calendar
        }
        set(calendar) {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val yearStr = year.toString()

            val list = createWheelData(calendar.getActualMaximum(Calendar.DAY_OF_MONTH), "")
            wheelDay.adapter = BaseWheelAdapter(list)

            wheelYear.currentItem = years.indexOf(AWTItem(yearStr, yearStr))
            wheelMonth.currentItem = month
            wheelDay.currentItem = day - 1
        }

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initialize(context)
    }

    override fun initialize(context: Context) {
        orientation = HORIZONTAL
        wheelDay = createWheelView(context, 2, true)
        wheelMonth = createWheelView(context, 3, true)
        wheelYear = createWheelView(context, 2, false)

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)

        for (i in year - 100..year) {
            val yearStr = i.toString()
            years.add(AWTItem(yearStr, yearStr))
        }
        val list = createWheelData(calendar.getActualMaximum(Calendar.DAY_OF_MONTH), "")

        initWheel(wheelYear, years)
        initWheel(wheelMonth, createMonthData())
        initWheel(wheelDay, list)
    }

    private fun createWheelView(context: Context, weight: Int, cyclic: Boolean): WheelView {
        val wheelView = WheelView(context)
        wheelView.isCyclic = cyclic
        val layoutParams = LayoutParams(1, ViewGroup.LayoutParams.WRAP_CONTENT, weight.toFloat())
        addView(wheelView, layoutParams)
        return wheelView
    }


    private fun initWheel(wheel: WheelView, items: List<AWTItem>) {
        wheel.visibleItems = 5
        wheel.addScrollingListener(object : OnWheelScrollListener {
            override fun onScrollingStarted(wheel: WheelView) {
                isScrolling = true
            }

            override fun onScrollingFinished(wheel: WheelView) {
                isScrolling = false
                if (wheel === wheelYear || wheel === wheelMonth) {
                    checkDateCount(wheelDay.currentItem)
                }
            }
        })
        wheel.adapter = BaseWheelAdapter(items)
    }


    private fun checkDateCount(index: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.YEAR, Integer.parseInt(years[wheelYear.currentItem].value))
        calendar.set(Calendar.MONTH, wheelMonth.currentItem)
        calendar.timeInMillis
        val list = createWheelData(calendar.getActualMaximum(Calendar.DAY_OF_MONTH), "")
        wheelDay.adapter = BaseWheelAdapter(list)
        when {
            index == -1 -> wheelDay.currentItem = calendar.get(Calendar.DAY_OF_MONTH) - 1
            index < list.size -> wheelDay.currentItem = index
            else -> wheelDay.currentItem = list.size - 1
        }
    }

    private fun createMonthData(): List<AWTItem> {
        val monthArray = context.resources.getStringArray(R.array.month_eng)
        val monthShortArray = context.resources.getStringArray(R.array.month_eng_short)
        val items = ArrayList<AWTItem>()
        for (i in monthArray.indices) {
            items.add(AWTItem(monthArray[i], monthShortArray[i]))
        }
        return items
    }

    private fun createWheelData(count: Int, str: String): List<AWTItem> {
        val items = ArrayList<AWTItem>()
        for (i in 1..count) {
            items.add(AWTItem(i.toString() + str, i.toString()))
        }
        return items
    }
}
