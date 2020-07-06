package com.appwellteam.library.control.tabControl

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import com.appwellteam.library.common.AWTViewTool
import com.appwellteam.library.control.AWTView
import com.appwellteam.library.control.R
import com.appwellteam.library.extension.convertDpToPixel
import com.appwellteam.library.extension.getThemeColor
import java.util.*

@Suppress("unused")
class AWTRoundTab : LinearLayout, AWTView {

    interface ITabListener {
        fun onSelected(pIndex: Int, pTitle: String?)
    }

    private var listItem: ArrayList<TabItem> = ArrayList()

    var focusColor: Int = context.getThemeColor(R.color.tab_focus)
        set(value) {
            field = value
            for (tItem in listItem) {
                tItem.reloadBackground()
            }
        }

    var titleColor: Int = Color.WHITE
        set(value) {
            field = value
            for (tItem in listItem) {
                tItem.reloadTitleColor()
            }
        }

    var typeface: Typeface = Typeface.DEFAULT
        set(value) {
            field = value
            for (tItem in listItem) {
                tItem.reloadTypeface()
            }
        }

    private var titleSize: Float = context.convertDpToPixel(14f)

    private var margin: Int = context.convertDpToPixel(10f).toInt()

    private lateinit var callback: ITabListener
    private var listener: ITabListener? = null
    private var currentItem: TabItem? = null

    var focusIndex: Int
        get() {
            val item = currentItem ?: return -1
            return listItem.indexOf(item)
        }
        set(pIndex) {
            if (listItem.size > pIndex) {
                changeFocus(pIndex)
            }
        }

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initialize(context)
    }

    override fun initialize(context: Context) {
        this.orientation = HORIZONTAL
        callback = object : ITabListener {
            override fun onSelected(pIndex: Int, pTitle: String?) {
                changeFocus(pIndex)
            }
        }
    }

    private fun reloadTab() {
        this.removeAllViews()
        currentItem = null
        for (ii in listItem.indices) {
            if (ii != 0) {
                val tLP = LayoutParams(2, LayoutParams.MATCH_PARENT)
                tLP.setMargins(0, margin, 0, margin)
                val tView = View(context)
                tView.setBackgroundColor(context.getThemeColor(R.color.tab_divider))
                this.addView(tView, tLP)
            }

            this.addView(listItem[ii].view)
        }
    }

    private fun changeFocus(pIndex: Int) {
        if (pIndex == listItem.indexOf(currentItem)) {
            return
        }

        currentItem?.setSelected(false)
        currentItem = listItem[pIndex]
        currentItem?.setSelected(true)

        listener?.onSelected(pIndex, currentItem?.title)
    }

    private fun setBackground(pView: View, pDrawable: Drawable?) {
        AWTViewTool.setBackgroundDrawable(pView, pDrawable)
    }

    fun clearTabFocus() {
        currentItem = null
        for (ii in listItem) {
            ii.setSelected(false)
        }
    }

    fun setTitleSizeWithDp(pDp: Float) {
        titleSize = context.convertDpToPixel(pDp)
        for (tItem in listItem) {
            tItem.reloadTitleSize()
        }
    }

    fun setItemArray(pTitle: IntArray) {
        listItem.clear()
        for (ii in pTitle.indices) {
            listItem.add(TabItem(pTitle[ii], ii, callback))
        }
        reloadTab()
    }

    fun setItemArray(pTitle: Array<String>) {
        listItem.clear()
        for (ii in pTitle.indices) {
            listItem.add(TabItem(pTitle[ii], ii, callback))
        }
        reloadTab()
    }

    internal inner class TabItem {
        var title: String? = null
            private set
        private lateinit var panel: RelativeLayout
        private lateinit var textView: AppCompatTextView
        private var index: Int = 0
        private var tabListener: ITabListener? = null
        private var drawable: StateListDrawable? = null
        private var focusDrawable: LayerDrawable? = null
        private var selected = false
        private lateinit var colorStateList: ColorStateList

        val view: View?
            get() = this.panel

        constructor(title: String, pIndex: Int, pListener: ITabListener?) {
            this.title = title
            this.index = pIndex
            this.tabListener = pListener
            createView()
        }

        constructor(@StringRes title: Int, pIndex: Int, pListener: ITabListener?): this(context.getString(title), pIndex, pListener)


        private fun createView() {
            this.panel = RelativeLayout(context)
            setBackground(this.panel, this.drawable)
            //			this.panel.setBackground(this.drawable);

            this.textView = AppCompatTextView(context)
            this.textView.text = title
            this.textView.typeface = typeface
            this.textView.gravity = Gravity.CENTER
            reloadTitleSize()

            val tLP = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
            this.panel.addView(this.textView, tLP)

            val tLLP = LayoutParams(1, LayoutParams.MATCH_PARENT, 1f)
            this.panel.layoutParams = tLLP

            this.panel.setOnClickListener { tabListener?.onSelected(this@TabItem.index, this@TabItem.title) }
            this.colorStateList = ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf()),
                    @Suppress("DEPRECATION")
                    intArrayOf(titleColor, context.getThemeColor(android.R.color.tertiary_text_light)))
            reloadBackground()
        }

        private fun createFocusDrawable(pColor: Int): LayerDrawable {
            val tRoundRadius = margin / 2f
            val tPadding = margin * 8 / 10
            val tRRS = RoundRectShape(floatArrayOf(tRoundRadius, tRoundRadius, tRoundRadius, tRoundRadius, tRoundRadius, tRoundRadius, tRoundRadius, tRoundRadius), null, null)

            val tSD = ShapeDrawable(tRRS)
            tSD.paint.color = pColor
            val tLD = LayerDrawable(arrayOf<Drawable>(tSD))
            tLD.setLayerInset(0, tPadding, tPadding, tPadding, tPadding)
            return tLD
        }

        private fun createStateListDrawable(pPressed: Drawable, pNormal: Drawable): StateListDrawable {
            val tSLD = StateListDrawable()

            tSLD.addState(intArrayOf(android.R.attr.state_pressed), pPressed)
            tSLD.addState(intArrayOf(), pNormal)
            return tSLD
        }

        fun reloadTypeface() {
            this.textView.typeface = typeface
        }

        fun reloadTitleSize() {
            this.textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize)
        }

        fun reloadBackground() {
            this.focusDrawable = createFocusDrawable(focusColor)
            this.drawable = createStateListDrawable(createFocusDrawable(focusColor), ColorDrawable(Color.TRANSPARENT))
            setSelected(this.selected)
        }

        fun reloadTitleColor() {
            this.colorStateList = ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf()),
                    @Suppress("DEPRECATION")
                    intArrayOf(titleColor, context.getThemeColor(android.R.color.tertiary_text_light)))
            setSelected(this.selected)
        }

        fun setSelected(pSelected: Boolean) {
            this.selected = pSelected
            if (this.selected) {
                setBackground(this.panel, this.focusDrawable)
                textView.setTextColor(titleColor)
            } else {
                setBackground(this.panel, this.drawable)
                textView.setTextColor(this.colorStateList)
            }
        }
    }
}
