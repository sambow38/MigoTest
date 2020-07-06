/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 *  
 *  Copyright 2010 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package kankan.wheel.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation
import android.os.Build
import android.os.Handler
import android.os.Message
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.animation.Interpolator
import android.widget.Scroller
import com.appwellteam.library.common.AWTCommon
import com.appwellteam.library.control.R
import com.appwellteam.library.extension.convertDimensionToPixel
import com.appwellteam.library.extension.getThemeColor
import com.appwellteam.library.extension.getThemeDrawable
import java.lang.ref.WeakReference
import java.util.*


/**
 * Numeric wheel view.
 *
 * @author Yuri Kanivets
 */
@Suppress("unused")
class WheelView : View {
    //	private static final int ITEMS_TEXT_COLOR = 0xFF000000;

    /** Top and bottom shadows colors  */
    private var shadowsColors = intArrayOf(-0x222223, 0x00EFEFEF, 0x00FFFFFF)

    /** Text size  */
    private var defaultTextSize = 48
    // private static final int defaultTextSize = 24;

    /** Top and bottom items offset (to hide that)  */
    private var itemOffset = defaultTextSize / 5

    // Wheel Values
    /**
     * Gets wheel adapter
     *
     * @return the adapter
     */
    var adapter: WheelAdapter? = null
        /**
         * Sets wheel adapter
         *
         * @param adapter
         * the new wheel adapter
         */
        set(adapter) {
            field = adapter
            invalidateLayouts()
            invalidate()
        }
    internal var currentItem = 0

    // Widths
    private var itemsWidth = 0
    private var labelWidth = 0

    // Count of visible items
    /**
     * Gets count of visible items
     *
     * @return the count of visible items
     */
    var visibleItems = DEF_VISIBLE_ITEMS
        /**
         * Sets count of visible items
         *
         * @param count
         * the new count
         */
        set(count) {
            field = count
            invalidate()
        }

    // Item height
    private var itemHeight = 0

    // Text paints
    private var itemsPaint: TextPaint? = null
        get() {
            return if (field == null) {
                field = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.FAKE_BOLD_TEXT_FLAG)
                field?.textSize = defaultTextSize.toFloat()
                field
            } else {
                field
            }
        }
    private var valuePaint: TextPaint? = null
        get() {
            return if (field == null) {
                field = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.FAKE_BOLD_TEXT_FLAG or Paint.DITHER_FLAG)
                // valuePaint.density = getResources().getDisplayMetrics().density;
                field?.textSize = defaultTextSize.toFloat()
                field?.setShadowLayer(0.1f, 0f, 0.1f, -0x3f3f40)
                field?.color = VALUE_TEXT_COLOR
                field
            } else {
                field
            }
        }

    // Layouts
    private var itemsLayout: StaticLayout? = null
    private var labelLayout: StaticLayout? = null
    private var valueLayout: StaticLayout? = null

    // Label & background
    /**
     * Gets label
     *
     * @return the label
     */
    private var label: String? = null
        /**
         * Sets label
         *
         * @param newLabel
         * the label to set
         */
        set(newLabel) {
            if (this.label == null || this.label != newLabel) {
                field = newLabel
                labelLayout = null
                invalidate()
            }
        }
    private var centerDrawable: Drawable? = null

    // Shadows drawables
    private var topShadow: GradientDrawable? = null
    private var bottomShadow: GradientDrawable? = null

    // Scrolling
    private var isScrollingPerformed: Boolean = false
    private var scrollingOffset: Int = 0

    // Scrolling animation
    private var gestureDetector: GestureDetector? = null
    private lateinit var scroller: Scroller
    private var lastScrollY: Int = 0

    // Cyclic
    internal var isCyclic = false
        set(value) {
            field = value
            invalidate()
            invalidateLayouts()
        }

    // Listeners
    private val changingListeners = LinkedList<OnWheelChangedListener>()
    private val scrollingListeners = LinkedList<OnWheelScrollListener>()

    /**
     * Returns the max item length that can be present
     *
     * @return the max length
     */
    private val maxTextLength: Int
        get() {
            val adapter = adapter ?: return 0

            val adapterLength = adapter.maximumLength
            if (adapterLength > 0) {
                return adapterLength
            }

            var maxText: String? = null
            val addItems = this.visibleItems / 2
            for (i in Math.max(currentItem - addItems, 0) until Math.min(currentItem + this.visibleItems, adapter.itemsCount)) {
                val text = adapter.getItem(i)
                if (text != null && (maxText == null || maxText.length < text.length)) {
                    maxText = text
                }
            }

            return maxText?.length ?: 0
        }

    // gesture listener
    private val gestureListener = object : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            if (isScrollingPerformed) {
                scroller.forceFinished(true)
                clearMessages()
                return true
            }
            return false
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            startScrolling()
            doScroll((-distanceY).toInt())
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            lastScrollY = currentItem * getItemHeight() + scrollingOffset
            val maxY = if (isCyclic) 0x7FFFFFFF else (this@WheelView.adapter?.itemsCount
                    ?: 0) * getItemHeight()
            val minY = if (isCyclic) -maxY else 0
            scroller.fling(0, lastScrollY, 0, (-velocityY).toInt() / 2, 0, 0, minY, maxY)
            setNextMessage(messageScroll)
            return true
        }
    }

    // Messages
    private val messageScroll = 0
    private val messageJustify = 1

    // animation handler
    private val animationHandler = WheelViewHandler(this)

    /**
     * Constructor
     */
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initData(context)
    }

    /**
     * Constructor
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initData(context)
    }

    /**
     * Constructor
     */
    constructor(context: Context) : super(context) {
        initData(context)
    }

    /**
     * Initializes class data
     *
     * @param context
     * the context
     */
    private fun initData(context: Context) {
        defaultTextSize = context.convertDimensionToPixel(R.dimen.wheel_text_size)
        ADDITIONAL_ITEM_HEIGHT = defaultTextSize

        itemOffset = defaultTextSize / 5
        VALUE_TEXT_COLOR = context.getThemeColor(R.color.wheel_value_text_color)
        ITEMS_TEXT_COLOR = context.getThemeColor(R.color.wheel_items_text_color)
        gestureDetector = GestureDetector(context, gestureListener)
        gestureDetector?.setIsLongpressEnabled(false)

        scroller = Scroller(context)
    }

    /**
     * Set the the specified scrolling interpolator
     *
     * @param interpolator
     * the interpolator
     */
    fun setInterpolator(interpolator: Interpolator) {
        scroller.forceFinished(true)
        scroller = Scroller(context, interpolator)
    }

    /**
     * Adds wheel changing listener
     *
     * @param listener
     * the listener
     */
    fun addChangingListener(listener: OnWheelChangedListener) {
        changingListeners.add(listener)
    }

    /**
     * Removes wheel changing listener
     *
     * @param listener
     * the listener
     */
    fun removeChangingListener(listener: OnWheelChangedListener) {
        changingListeners.remove(listener)
    }

    /**
     * Notifies changing listeners
     *
     * @param oldValue
     * the old wheel value
     * @param newValue
     * the new wheel value
     */
    @Suppress("ProtectedInFinal")
    protected fun notifyChangingListeners(oldValue: Int, newValue: Int) {
        for (listener in changingListeners) {
            listener.onChanged(this, oldValue, newValue)
        }
    }

    /**
     * Adds wheel scrolling listener
     *
     * @param listener
     * the listener
     */
    fun addScrollingListener(listener: OnWheelScrollListener) {
        scrollingListeners.add(listener)
    }

    /**
     * Removes wheel scrolling listener
     *
     * @param listener
     * the listener
     */
    fun removeScrollingListener(listener: OnWheelScrollListener) {
        scrollingListeners.remove(listener)
    }

    /**
     * Notifies listeners about starting scrolling
     */
    @Suppress("ProtectedInFinal")
    protected fun notifyScrollingListenersAboutStart() {
        for (listener in scrollingListeners) {
            listener.onScrollingStarted(this)
        }
    }

    /**
     * Notifies listeners about ending scrolling
     */
    @Suppress("ProtectedInFinal")
    protected fun notifyScrollingListenersAboutEnd() {
        for (listener in scrollingListeners) {
            listener.onScrollingFinished(this)
        }
    }

    /**
     * Gets current value
     *
     * @return the current value
     */
    fun getCurrentItem(): Int {
        return currentItem
    }

    /**
     * Sets the current item. Does nothing when index is wrong.
     *
     * @param index
     * the item index
     * @param animated
     * the animation flag
     */
    @JvmOverloads
    fun setCurrentItem(index: Int, animated: Boolean = false) {
        val adapter = adapter ?: return
        if (adapter.itemsCount == 0) return

        var tempIndex = index
        if (tempIndex < 0 || tempIndex >= adapter.itemsCount) {
            if (isCyclic) {
                while (tempIndex < 0) {
                    tempIndex += adapter.itemsCount
                }
                tempIndex %= adapter.itemsCount
            } else {
                return  // throw?
            }
        }
        if (tempIndex != currentItem) {
            if (animated) {
                scroll(tempIndex - currentItem, SCROLLING_DURATION)
            } else {
                invalidateLayouts()

                val old = currentItem
                currentItem = tempIndex

                notifyChangingListeners(old, currentItem)

                invalidate()
            }
        }
    }

    /**
     * Invalidates layouts
     */
    private fun invalidateLayouts() {
        itemsLayout = null
        valueLayout = null
        scrollingOffset = 0
    }

    /**
     * Initializes resources
     */
    private fun initResourcesIfNecessary() {
//        if (itemsPaint == null) {
//            itemsPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.FAKE_BOLD_TEXT_FLAG)
//            // itemsPaint.density = getResources().getDisplayMetrics().density;
//            itemsPaint?.textSize = defaultTextSize.toFloat()
//        }
//
//        if (valuePaint == null) {
//            valuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.FAKE_BOLD_TEXT_FLAG or Paint.DITHER_FLAG)
//            // valuePaint.density = getResources().getDisplayMetrics().density;
//            valuePaint?.textSize = defaultTextSize.toFloat()
//            valuePaint?.setShadowLayer(0.1f, 0f, 0.1f, -0x3f3f40)
//            valuePaint?.color = VALUE_TEXT_COLOR
//        }

        if (centerDrawable == null) {
            centerDrawable = context.getThemeDrawable(R.drawable.share_control_wheel_val)
        }

        if (topShadow == null) {
            topShadow = GradientDrawable(Orientation.TOP_BOTTOM, shadowsColors)
        }

        if (bottomShadow == null) {
            bottomShadow = GradientDrawable(Orientation.BOTTOM_TOP, shadowsColors)
        }

        setBackgroundResource(R.drawable.share_control_wheel_bg)
    }

    /**
     * Calculates desired height for layout
     *
     * @param layout
     * the source layout
     * @return the desired layout height
     */
    private fun getDesiredHeight(layout: Layout?): Int {
        if (layout == null) {
            return 0
        }

        var desired = getItemHeight() * this.visibleItems - itemOffset * 2 - ADDITIONAL_ITEM_HEIGHT

        // Check against our minimum height
        desired = Math.max(desired, suggestedMinimumHeight)

        return desired
    }

    /**
     * Returns text item by index
     *
     * @param index
     * the item index
     * @return the item or null
     */
    private fun getTextItem(index: Int): String? {
        val adapter = this.adapter ?: return null
        if (adapter.itemsCount == 0) return null

        var tempIndex = index
        val count = adapter.itemsCount
        if ((tempIndex < 0 || tempIndex >= count) && !isCyclic) {
            return null
        } else {
            while (tempIndex < 0) {
                tempIndex += count
            }
        }

        tempIndex %= count
        return adapter.getItem(tempIndex)
    }

    /**
     * Builds text depending on current value
     *
     * @param useCurrentValue
     * @return the text
     */
    private fun buildText(useCurrentValue: Boolean, widthItems: Int): String {
        val itemsText = StringBuilder()
        val addItems = this.visibleItems / 2 + 1

        for (i in currentItem - addItems..currentItem + addItems) {
            if (useCurrentValue || i != currentItem) {
                var text = getTextItem(i)
                text = getText(text, itemsPaint, widthItems.toFloat(), false)
                if (text != null) {
                    itemsText.append(text)
                }
            }
            if (i < currentItem + addItems) {
                itemsText.append("\n")
            }
        }

        return itemsText.toString()
    }

    /**
     * Returns height of wheel item
     *
     * @return the item height
     */
    private fun getItemHeight(): Int {
        val itemsLayout = itemsLayout ?: return 0
        if (itemHeight != 0) {
            return itemHeight
        } else if (itemsLayout.lineCount > 2) {
            itemHeight = itemsLayout.getLineTop(2) - itemsLayout.getLineTop(1)
            return itemHeight
        }

        return height / this.visibleItems
    }

    /**
     * Calculates control width and creates text layouts
     *
     * @param widthSize
     * the input layout width
     * @param mode
     * the layout mode
     * @return the calculated control width
     */
    private fun calculateLayoutWidth(widthSize: Int, mode: Int): Int {
        initResourcesIfNecessary()

        var width: Int

        val maxLength = maxTextLength
        itemsWidth = if (maxLength > 0) {
            val textWidth = Math.ceil(Layout.getDesiredWidth("0", itemsPaint).toDouble()).toFloat()
            (maxLength * textWidth).toInt()
        } else {
            0
        }
        itemsWidth += ADDITIONAL_ITEMS_SPACE // make it some more

        labelWidth = 0
        if (this.label?.isNotEmpty() == true) {
            labelWidth = Math.ceil(Layout.getDesiredWidth(this.label, valuePaint).toDouble()).toFloat().toInt()
        }

        var recalculate = false
        if (mode == MeasureSpec.EXACTLY) {
            width = widthSize
            recalculate = true
        } else {
            width = itemsWidth + labelWidth + 2 * PADDING
            if (labelWidth > 0) {
                width += LABEL_OFFSET
            }

            // Check against our minimum width
            width = Math.max(width, suggestedMinimumWidth)

            if (mode == MeasureSpec.AT_MOST && widthSize < width) {
                width = widthSize
                recalculate = true
            }
        }

        if (recalculate) {
            // recalculate width
            val pureWidth = width - LABEL_OFFSET - 2 * PADDING
            if (pureWidth <= 0) {
                labelWidth = 0
                itemsWidth = labelWidth
            }
            if (labelWidth > 0) {
                val newWidthItems = itemsWidth.toDouble() * pureWidth / (itemsWidth + labelWidth)
                itemsWidth = newWidthItems.toInt()
                labelWidth = pureWidth - itemsWidth
            } else {
                itemsWidth = pureWidth + LABEL_OFFSET // no label
            }
        }

        if (itemsWidth > 0) {
            createLayouts(itemsWidth, labelWidth)
        }

        return width
    }

    /**
     * Creates layouts
     *
     * @param widthItems
     * width of items layout
     * @param widthLabel
     * width of label layout
     */
    private fun createLayouts(widthItems: Int, widthLabel: Int) {
        if (itemsLayout?.width ?: Int.MAX_VALUE > widthItems) {
            val itemsPaint = itemsPaint ?: return
            val text = buildText(isScrollingPerformed, widthItems)

            itemsLayout = buildStaticLayout(text, itemsPaint, widthItems, widthLabel)
        } else {
            itemsLayout?.increaseWidthTo(widthItems)
        }

        if (!isScrollingPerformed && valueLayout?.width ?: Int.MAX_VALUE > widthItems) {
            val valuePaint = valuePaint ?: return
            val text = adapter?.getItem(currentItem) ?: ""

            valueLayout = buildStaticLayout(text, getPaint(text, valuePaint, widthItems.toFloat()), widthItems, widthLabel)

        } else if (isScrollingPerformed) {
            valueLayout = null
        } else {
            valueLayout?.increaseWidthTo(widthItems)
        }

        if (widthLabel > 0) {
            if (labelLayout?.width ?: Int.MAX_VALUE > widthLabel) {
                val valuePaint = valuePaint ?: return
                val text = this.label ?: ""

                labelLayout = buildStaticLayout(text, valuePaint, widthLabel, widthLabel)
            } else {
                labelLayout?.increaseWidthTo(widthLabel)
            }
        }
    }

    private fun buildStaticLayout(text: String, paint: TextPaint, itemWidth: Int, labelWidth: Int): StaticLayout
    {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder
                    .obtain(text, 0, text.length, paint, itemWidth)
                    .setAlignment(if (labelWidth > 0) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(ADDITIONAL_ITEM_HEIGHT.toFloat(), 1f)
                    .setIncludePad(false)
                    .build()
        }
        else {
            @Suppress("DEPRECATION")
            StaticLayout(
                    text,
                    paint,
                    itemWidth,
                    if (labelWidth > 0) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_CENTER,
                    1f,
                    ADDITIONAL_ITEM_HEIGHT.toFloat(),
                    false)
        }
    }

    private fun getPaint(text: String, paint: TextPaint?, maxWidth: Float): TextPaint {
        val newPaint = TextPaint()
        newPaint.set(paint)
        val width = newPaint.measureText(text)
        return if (width > maxWidth) {
            newPaint.textSize = newPaint.textSize - 1
            getPaint(text, newPaint, maxWidth)
        } else {
            newPaint
        }
    }

    private fun getText(text: String?, paint: Paint?, maxWidth: Float, dot: Boolean): String? {
        return if (text == null) {
            null
        } else {
            val str = if (dot) "$text..." else text
            val width = paint?.measureText(str) ?: 0f
            if (width > maxWidth) {
                getText(text.substring(0, text.length - 1), paint, maxWidth, true)
            } else {
                str
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = calculateLayoutWidth(widthSize, widthMode)

        var height: Int
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize
        } else {
            height = getDesiredHeight(itemsLayout)

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize)
            }
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (itemsLayout == null) {
            if (itemsWidth == 0) {
                calculateLayoutWidth(width, MeasureSpec.EXACTLY)
            } else {
                createLayouts(itemsWidth, labelWidth)
            }
        }

        if (itemsWidth > 0) {
            canvas.save()
            // Skip padding space and hide a part of top and bottom items
            canvas.translate(PADDING.toFloat(), (-itemOffset).toFloat())
            drawItems(canvas)
            drawValue(canvas)
            canvas.restore()
        }

        drawCenterRect(canvas)
        drawShadows(canvas)
    }

    /**
     * Draws shadows on top and bottom of control
     *
     * @param canvas
     * the canvas for drawing
     */
    private fun drawShadows(canvas: Canvas) {
        topShadow?.setBounds(0, 0, width, height / this.visibleItems)
        topShadow?.draw(canvas)

        bottomShadow?.setBounds(0, height - height / this.visibleItems, width, height)
        bottomShadow?.draw(canvas)
    }

    /**
     * Draws value and label layout
     *
     * @param canvas
     * the canvas for drawing
     */
    private fun drawValue(canvas: Canvas) {
        val itemsLayout = itemsLayout ?: return
        valuePaint?.color = VALUE_TEXT_COLOR
        valuePaint?.drawableState = drawableState

        val bounds = Rect()
        itemsLayout.getLineBounds(this.visibleItems / 2, bounds)

        // draw label
        drawLayout(canvas, labelLayout, (itemsLayout.width + LABEL_OFFSET).toFloat(), bounds.top.toFloat())

        // draw current value
        drawLayout(canvas, valueLayout, 0f, (bounds.top + scrollingOffset).toFloat())
    }

    private fun drawLayout(canvas: Canvas, staticLayout: StaticLayout?, dx: Float, dy: Float)
    {
        val layout = staticLayout ?: return
        canvas.save()
        canvas.translate(dx, dy)
        layout.draw(canvas)
        canvas.restore()
    }

    /**
     * Draws items
     *
     * @param canvas
     * the canvas for drawing
     */
    private fun drawItems(canvas: Canvas) {
        canvas.save()

        val top = itemsLayout?.getLineTop(1) ?: 0
        canvas.translate(0f, (-top + scrollingOffset).toFloat())

        itemsPaint?.color = ITEMS_TEXT_COLOR
        itemsPaint?.drawableState = drawableState
        itemsLayout?.draw(canvas)

        canvas.restore()
    }

    /**
     * Draws rect for current value
     *
     * @param canvas
     * the canvas for drawing
     */
    private fun drawCenterRect(canvas: Canvas) {
        val center = height / 2
        val offset = getItemHeight() / 2
        centerDrawable?.setBounds(0, center - offset, width, center + offset)
        centerDrawable?.draw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        adapter ?: return true
        if (event.action == MotionEvent.ACTION_UP)
        {
            if (gestureDetector?.onTouchEvent(event) != true)
            {
                justify()
            }
        }

//        if (!gestureDetector!!.onTouchEvent(event) && event.action == MotionEvent.ACTION_UP) {
//            justify()
//        }
        return true
    }

    /**
     * Scrolls the wheel
     *
     * @param delta
     * the scrolling value
     */
    private fun doScroll(delta: Int) {
        scrollingOffset += delta
        val itemCount = this.adapter?.itemsCount ?: 0

        var count = scrollingOffset / getItemHeight()
        var pos = currentItem - count
        if (isCyclic && itemCount > 0) {
            // fix position by rotating
            while (pos < 0) {
                pos += itemCount
            }
            pos %= itemCount
        } else if (isScrollingPerformed) {
            //
            if (pos < 0) {
                count = currentItem
                pos = 0
            } else if (pos >= itemCount) {
                count = currentItem - itemCount + 1
                pos = itemCount - 1
            }
        } else {
            // fix position
            pos = Math.max(pos, 0)
            pos = Math.min(pos, itemCount- 1)
        }

        val offset = scrollingOffset
        if (pos != currentItem) {
            setCurrentItem(pos, false)
        } else {
            invalidate()
        }

        // update offset
        scrollingOffset = offset - count * getItemHeight()
        if (scrollingOffset > height) {
            scrollingOffset = scrollingOffset % height + height
        }
    }

    /**
     * Set next message to queue. Clears queue before.
     *
     * @param message
     * the message to set
     */
    private fun setNextMessage(message: Int) {
        clearMessages()
        animationHandler.sendEmptyMessage(message)
    }

    /**
     * Clears messages from queue
     */
    private fun clearMessages() {
        animationHandler.removeMessages(messageScroll)
        animationHandler.removeMessages(messageJustify)
    }

    /**
     * Justifies wheel
     */
    private fun justify() {
        if (this.adapter == null) {
            return
        }

        lastScrollY = 0
        var offset = scrollingOffset
        val itemHeight = getItemHeight()
        val itemCount = this.adapter?.itemsCount ?: 0
        val needToIncrease = if (offset > 0) currentItem < itemCount else currentItem > 0
        if ((isCyclic || needToIncrease) && Math.abs(offset.toFloat()) > itemHeight.toFloat() / 2) {
            if (offset < 0)
                offset += itemHeight + MIN_DELTA_FOR_SCROLLING
            else
                offset -= itemHeight + MIN_DELTA_FOR_SCROLLING
        }
        if (Math.abs(offset) > MIN_DELTA_FOR_SCROLLING) {
            scroller.startScroll(0, 0, 0, offset, SCROLLING_DURATION)
            setNextMessage(messageJustify)
        } else {
            finishScrolling()
        }
    }

    /**
     * Starts scrolling
     */
    private fun startScrolling() {
        if (!isScrollingPerformed) {
            isScrollingPerformed = true
            notifyScrollingListenersAboutStart()
        }
    }

    /**
     * Finishes scrolling
     */
    internal fun finishScrolling() {
        if (isScrollingPerformed) {
            notifyScrollingListenersAboutEnd()
            isScrollingPerformed = false
        }
        invalidateLayouts()
        invalidate()
    }

    /**
     * Scroll the wheel
     *
     * @param itemsToScroll
     * items to scroll
     * @param time
     * scrolling duration
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun scroll(itemsToScroll: Int, time: Int) {
        scroller.forceFinished(true)

        lastScrollY = scrollingOffset
        val offset = itemsToScroll * getItemHeight()

        scroller.startScroll(0, lastScrollY, 0, offset - lastScrollY, time)
        setNextMessage(messageScroll)

        startScrolling()
    }

    companion object {
        /** Scrolling duration  */
        private const val SCROLLING_DURATION = 400

        /** Minimum delta for scrolling  */
        private const val MIN_DELTA_FOR_SCROLLING = 1

        /** Current value & label text color  */
        private var VALUE_TEXT_COLOR = -0xc08b36
        //	private static final int VALUE_TEXT_COLOR = 0xF0000000;

        /** Items text color  */
        private var ITEMS_TEXT_COLOR = -0x3b3b3c
        //	{ 0xFF111111, 0x00AAAAAA, 0x00AAAAAA };

        /** Additional items height (is added to standard text item height)  */
        private var ADDITIONAL_ITEM_HEIGHT = 50

        /** Additional width for items layout  */
        private const val ADDITIONAL_ITEMS_SPACE = 5
        // private static final int ADDITIONAL_ITEMS_SPACE = 10;

        /** Label offset  */
        private const val LABEL_OFFSET = 5
        // private static final int LABEL_OFFSET = 8;

        /** Left and right padding value  */
        private const val PADDING = 5
        // private static final int PADDING = 10;

        /** Default count of visible items  */
        private const val DEF_VISIBLE_ITEMS = 5
    }

    private class WheelViewHandler(wheelView: WheelView) : Handler() {
        private val wheelView: WeakReference<WheelView> = WeakReference(wheelView)

        override fun handleMessage(msg: Message) {
            val wheelView = wheelView.get() ?: return

            wheelView.scroller.computeScrollOffset()
            val currY = wheelView.scroller.currY
            val delta = wheelView.lastScrollY - currY
            wheelView.lastScrollY = currY
            if (delta != 0) {
                wheelView.doScroll(delta)
            }

            // scrolling is not finished when it comes to final Y
            // so, finish it manually
            if (Math.abs(currY - wheelView.scroller.finalY) < MIN_DELTA_FOR_SCROLLING) {
//                currY = wheelView.scroller.finalY
                wheelView.scroller.forceFinished(true)
            }
            if (!wheelView.scroller.isFinished) {
                this.sendEmptyMessage(msg.what)
            } else if (msg.what == wheelView.messageScroll) {
                wheelView.justify()
            } else {
                wheelView.finishScrolling()
            }
        }
    }
}



