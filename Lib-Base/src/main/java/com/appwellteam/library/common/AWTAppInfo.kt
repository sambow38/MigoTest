package com.appwellteam.library.common

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.DisplayMetrics
import com.appwellteam.library.AWTApplication

@Suppress("unused")
object AWTAppInfo
{
    private var VERSION_NAME: String? = null
    private var VERSION_CODE: String? = null
    private var PACKAGE_NAME: String? = null
    private var SCREEN_WIDTH: Int = 0
    private var SCREEN_HEIGHT: Int = 0
    private var IS_CHECKED_PLATFORM: Boolean = false
    private var IS_TABLET: Boolean = false

    val screenWidth: Int
        get() {
            return if (SCREEN_WIDTH <= 0) {
                val dm = DisplayMetrics()
                AWTApplication.app?.activity?.windowManager?.defaultDisplay?.getMetrics(dm)
                SCREEN_WIDTH = dm.widthPixels
                SCREEN_WIDTH
            } else {
                SCREEN_WIDTH
            }
        }

    val screenHeight: Int
        get() {
            return if (SCREEN_HEIGHT <= 0) {
                val dm = DisplayMetrics()
                AWTApplication.app?.activity?.windowManager?.defaultDisplay?.getMetrics(dm)
                SCREEN_HEIGHT = dm.heightPixels
                SCREEN_HEIGHT
            } else {
                SCREEN_HEIGHT
            }
        }


    val packageName: String
        get() {
            if (PACKAGE_NAME == null) {
                PACKAGE_NAME = AWTApplication.app?.applicationContext?.packageName ?: return ""
            }
            return PACKAGE_NAME ?: return ""
        }

    val versionCode: String
        get() {
            if (VERSION_CODE == null) {
                val packageManager = AWTApplication.app?.packageManager ?: return ""
                val packageInfo: PackageInfo
                try {
                    packageInfo = packageManager.getPackageInfo(packageName, 0)
                    VERSION_CODE =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                                packageInfo.longVersionCode.toString() + ""
                            else {
                                @Suppress("DEPRECATION")
                                packageInfo.versionCode.toString() + ""
                            }
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            }
            return VERSION_CODE ?: return ""
        }

    val versionName: String
        get() {
            if (VERSION_NAME == null) {
                val packageManager = AWTApplication.app?.packageManager ?: return ""
                val packageInfo: PackageInfo
                try {
                    packageInfo = packageManager.getPackageInfo(packageName, 0)
                    VERSION_NAME = packageInfo.versionName
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            }
            return VERSION_NAME ?: return ""
        }

    val isTablet: Boolean
        get() {
            return if (IS_CHECKED_PLATFORM) {
                IS_TABLET
            } else {
                val displayMetrics = DisplayMetrics()
                AWTApplication.app?.activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

                val wInches = displayMetrics.widthPixels / displayMetrics.densityDpi.toDouble()
                val hInches = displayMetrics.heightPixels / displayMetrics.densityDpi.toDouble()

                val screenDiagonal = Math.sqrt(Math.pow(wInches, 2.0) + Math.pow(hInches, 2.0))
                IS_TABLET = screenDiagonal >= 6.5
                IS_CHECKED_PLATFORM = true
                IS_TABLET
            }
        }
}