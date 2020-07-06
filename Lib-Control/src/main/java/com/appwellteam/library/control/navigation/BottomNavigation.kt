package com.appwellteam.library.control.navigation

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView.ScaleType
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.appwellteam.library.control.AWTView
import com.appwellteam.library.control.R
import com.appwellteam.library.extension.convertDimensionToPixel
import com.appwellteam.library.extension.convertDpToPixel
import com.appwellteam.library.extension.getThemeColor

/**
 * Created by Sambow on 16/8/19.
 */
@Suppress("unused")
class BottomNavigation : LinearLayout, AWTView {

    private lateinit var panel: LinearLayout
    private lateinit var separator: View
    private var listener: IBottomNavigation? = null

    interface IBottomNavigation {
        fun onClickedItem(item: NavigationItem)
    }

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)// api 21
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initialize(context)
    }

    override fun initialize(context: Context) {
        panel = LinearLayout(context)
        panel.orientation = HORIZONTAL

        orientation = VERTICAL
        setBg(context.getThemeColor(R.color.white))

        separator = View(context)
        separator.setBackgroundColor(context.getThemeColor(R.color.dark_gray))

        var separatorHeight = context.convertDpToPixel(0.5f).toInt()
        separatorHeight = if (separatorHeight == 0) 1 else separatorHeight
        addView(separator, LayoutParams(LayoutParams.MATCH_PARENT, separatorHeight))
        addView(panel, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    fun setListener(listener: IBottomNavigation) {
        this.listener = listener
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setBg(@ColorInt colorBG: Int) {
        setBackgroundColor(colorBG)
    }

    fun setSeparatorBg(@ColorInt colorBG: Int) {
        separator.setBackgroundColor(colorBG)
    }

    fun setItems(vararg items: NavigationItem) {
        panel.removeAllViews()
        val viewHeight = context.resources.getDimensionPixelSize(R.dimen.dp_60)
        for (j in items.indices) {
            val item = items[j]
            val view = ItemView(context, item)
            view.isSelected = j == 0
            view.setOnClickListener { v ->
                listener?.onClickedItem(item)
                val count = panel.childCount
                for (i in 0 until count) {
                    val child = panel.getChildAt(i)
                    child.isSelected = child === v
                }
            }
            val lp = LayoutParams(1, viewHeight, 1f)
            panel.addView(view, lp)
        }
    }

    fun setCurrent(index: Int) {
        val count = panel.childCount
        if (index in 0 until count) {
            for (i in 0 until count) {
                val child = panel.getChildAt(i)
                child.isSelected = i == index
            }
        } else {
            for (i in 0 until count) {
                val child = panel.getChildAt(i)
                child.isSelected = false
            }
        }
    }

    fun setDot(index: Int, showDot: Boolean, click: Boolean) {
        val count = panel.childCount
        if (index in 0 until count) {
            val child = panel.getChildAt(index) as ItemView
            child.dot.visibility = if (showDot) View.VISIBLE else View.INVISIBLE
            if (click) {
                child.performClick()
            }
        }
    }

    private inner class ItemView internal constructor(context: Context, item: NavigationItem) : RelativeLayout(context) {
        private lateinit var itemPanel: LinearLayout
        private lateinit var image: AppCompatImageView
        private lateinit var title: AppCompatTextView
        internal lateinit var dot: DotView

        init {
            initPanel(context)
            initImageView(context)
            initTextView(context)
            initDotView(context)
            setData(item)
        }

        private fun initPanel(context: Context) {
            itemPanel = LinearLayout(context)
            itemPanel.orientation = VERTICAL
            itemPanel.gravity = Gravity.CENTER
            val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            addView(itemPanel, lp)
        }

        private fun initImageView(context: Context) {
            image = AppCompatImageView(context)
            image.scaleType = ScaleType.FIT_CENTER
            image.adjustViewBounds = true

            val dp4 = context.convertDimensionToPixel(R.dimen.dp_4)
            val lp = LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1, 2f)
            lp.setMargins(dp4, dp4, dp4, 0)
            itemPanel.addView(image, lp)
        }

        private fun initTextView(context: Context) {
            title = AppCompatTextView(context)
            title.gravity = Gravity.CENTER
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimensionPixelSize(R.dimen.dp_12).toFloat())

            val dp1 = context.convertDpToPixel(1f).toInt()
            val dp2 = context.convertDpToPixel(2f).toInt()
            val dp4 = context.convertDimensionToPixel(R.dimen.dp_4)
            val lp = LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1, 1f)
            lp.setMargins(dp4, dp1, dp4, dp2)
            itemPanel.addView(title, lp)
        }

        private fun initDotView(context: Context) {
            dot = DotView(context)
            dot.visibility = View.INVISIBLE
            val size = context.convertDimensionToPixel(R.dimen.dp_8)
            val margin = context.convertDimensionToPixel(R.dimen.dp_10)
            val lp = LayoutParams(size, size)
            lp.addRule(ALIGN_PARENT_RIGHT)
            lp.setMargins(0, margin, margin, 0)
            addView(dot, lp)
        }

        internal fun setData(item: NavigationItem) {
            title.setTextColor(item.colorListTitle)

            if (item.title.isEmpty()) {
                title.text = ""
                title.visibility = View.GONE
            } else {
                title.text = item.title
            }

            if (item.image == null) {
                image.setImageBitmap(null)
                image.visibility = View.GONE
            } else {
                image.setImageDrawable(item.image)
            }
        }

        override fun setSelected(selected: Boolean) {
            super.setSelected(selected)
            title.isSelected = selected
            image.isSelected = selected
        }
    }

    private inner class DotView(context: Context) : View(context) {
        private val paint: Paint = Paint()

        init {
            paint.isAntiAlias = true
            paint.color = context.getThemeColor(R.color.navigation_dot)
            paint.style = Paint.Style.FILL_AND_STROKE
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val radiusX = (right - left) * 1f / 2
            val radiusY = (bottom - top) * 1f / 2
            val radiusFinal = if (radiusX > radiusY) {
                radiusY
            } else {
                radiusX
            }
            canvas.drawCircle(width / 2f, height / 2f, radiusFinal, paint)
        }
    }
}
