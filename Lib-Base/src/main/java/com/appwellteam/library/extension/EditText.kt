@file:Suppress("unused")
package com.appwellteam.library.extension

import android.graphics.PorterDuff
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat

fun AppCompatEditText.setCursorColor(@ColorInt color: Int) {
    try {
        // Get the cursor resource id
        var field = AppCompatEditText::class.java.getDeclaredField("mCursorDrawableRes")
        field.isAccessible = true
        val drawableResId = field.getInt(this)

        // Get the editor
        field = AppCompatEditText::class.java.getDeclaredField("mEditor")
        field.isAccessible = true
        val editor = field.get(this)

        // Get the drawable and set a color filter
        val drawable = ContextCompat.getDrawable(this.context, drawableResId) ?: return
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        val drawables = arrayOf(drawable, drawable)

        // Set the drawables
        field = editor.javaClass.getDeclaredField("mCursorDrawable")
        field.isAccessible = true
        field.set(editor, drawables)
    } catch (ignored: Exception) {
    }

}