@file:Suppress("unused")
package com.appwellteam.library.extension

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.appwellteam.library.common.AWTAppInfo
import java.io.File

private val fileProviderAuthority = "${AWTAppInfo.packageName}.fileprovider"

fun Context.cameraUri(target: File): Uri
{
    return when (Build.VERSION.SDK_INT) {
        in Build.VERSION_CODES.N..Int.MAX_VALUE -> {
            FileProvider.getUriForFile(
                    this,
                    fileProviderAuthority,
                    target)
        }
        else -> Uri.fromFile(target)
    }
}

fun Context.convertDpToPixel(dp: Float): Float {
    val metrics = this.resources.displayMetrics
    return dp * (metrics.densityDpi / 160f)
}

fun Context.convertDimensionToPixel(@DimenRes dimen: Int): Int {
    return this.resources.getDimensionPixelOffset(dimen)
}

@ColorInt
fun Context.getThemeColor(@ColorRes res: Int, theme: Resources.Theme? = null): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) //api 23
    {
        this.resources.getColor(res, theme)
    } else {
        @Suppress("DEPRECATION")
        this.resources.getColor(res)
    }
}

fun Context.getThemeColorStateList(@ColorRes res: Int, theme: Resources.Theme? = null): ColorStateList {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) //api 23
    {
        this.resources.getColorStateList(res, theme)
    } else {
        @Suppress("DEPRECATION")
        this.resources.getColorStateList(res)
    }
}

fun Context.getThemeDrawable(@DrawableRes res: Int, theme: Resources.Theme? = null): Drawable {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) //api 23
    {
        this.resources.getDrawable(res, theme)
    } else {
        @Suppress("DEPRECATION")
        this.resources.getDrawable(res)
    }
}


@LayoutRes
fun Context.getIDFromLayout(pRes: String): Int {
    return this.resources.getIdentifier(pRes, "layout", AWTAppInfo.packageName)
}

@IdRes
fun Context.getIDFromID(pRes: String): Int {
    return this.resources.getIdentifier(pRes, "id", AWTAppInfo.packageName)
}

@ColorRes fun Context.getIDFromColor(pRes: String): Int {
    return this.resources.getIdentifier(pRes, "color", AWTAppInfo.packageName)
}

@DrawableRes fun Context.getIDFromDrawable(pRes: String): Int {
    return this.resources.getIdentifier(pRes, "drawable", AWTAppInfo.packageName)
}

@DimenRes fun Context.getIDFromDimen(pRes: String): Int {
    return this.resources.getIdentifier(pRes, "dimen", AWTAppInfo.packageName)
}

@StyleRes
fun Context.getIDFromStyle(pRes: String): Int {
    return this.resources.getIdentifier(pRes, "style", AWTAppInfo.packageName)
}

fun Context.getDimenValue(pRes: String): Float {
    return this.resources.getDimension(getIDFromDimen(pRes))
}

@ColorInt
fun Context.getThemeColor(pRes: String, theme: Resources.Theme? = null): Int {
    return getThemeColor(getIDFromColor(pRes), theme)
}

fun Context.getExternalFilesDirPath(type: String): String
{
    return ContextCompat.getExternalFilesDirs(this, "").firstOrNull()?.absolutePath ?: ""
}