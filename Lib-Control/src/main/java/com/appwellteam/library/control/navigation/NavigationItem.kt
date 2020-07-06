package com.appwellteam.library.control.navigation

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import androidx.annotation.ColorInt
import com.appwellteam.library.AWTApplication
import com.appwellteam.library.control.R
import com.appwellteam.library.extension.getThemeColor

/**
 * Created by Sambow on 16/8/19.
 */
@Suppress("unused")
class NavigationItem(val key: String) {
    internal var title = ""
    internal var image: Drawable? = null
    internal var colorListTitle: ColorStateList? = null
        private set

    init {
        setTextColor(AWTApplication.app?.getThemeColor(R.color.gray) ?: Color.GRAY, AWTApplication.app?.getThemeColor(R.color.dark_gray) ?: Color.DKGRAY)
    }

//    constructor(key: String, @StringRes title: Int, @DrawableRes image: Int) : this(key, AWTApplication.app?.getString(title) ?: "", AWTApplication.app?.getThemeDrawable(image))
//
//    constructor(key: String, @StringRes title: Int, @DrawableRes normal: Int, @DrawableRes selected: Int) : this(key, AWTApplication.getStr(title), AWTCommon.getThemeDrawable(normal), AWTCommon.getThemeDrawable(selected))
//
//    constructor(key: String, title: String, @DrawableRes normal: Int) : this(key, title, AWTCommon.getThemeDrawable(normal))

    constructor(key: String, title: String, image: Drawable) : this(key) {
        this.title = title
        this.image = image
    }

    constructor(key: String, title: String, image: Drawable, select: Drawable) : this(key) {
        this.title = title
        this.setImage(image, select)
    }

    @JvmOverloads
    fun setTextColor(@ColorInt normal: Int, @ColorInt pressed: Int = normal, @ColorInt selected: Int = normal) {
        val colors = intArrayOf(pressed, selected, normal)
        val states = arrayOfNulls<IntArray>(3)
        states[0] = intArrayOf(android.R.attr.state_pressed)
        states[1] = intArrayOf(android.R.attr.state_selected)
        states[2] = intArrayOf()
        colorListTitle = ColorStateList(states, colors)
    }

    @JvmOverloads
    fun setImage(normal: Drawable, selected: Drawable, pressed: Drawable = selected) {
        val sld = StateListDrawable()
        sld.addState(intArrayOf(android.R.attr.state_pressed), pressed)
        sld.addState(intArrayOf(android.R.attr.state_selected), selected)
        sld.addState(intArrayOf(), normal)
        this.image = sld
    }
}
