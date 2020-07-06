package com.appwellteam.library

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.multidex.MultiDexApplication

/**
 * Created by Sambow on 15/9/19.
 */
@Suppress("unused")
open class AWTApplication : MultiDexApplication(), Application.ActivityLifecycleCallbacks {
    var activity: AWTActivity? = null
    private var count = 0

    val isAppActive: Boolean
        get() = count > 0

    override fun onCreate() {
        super.onCreate()
        count = 0
        app = this
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle) {

    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {
        count += 1
    }

    override fun onActivityPaused(activity: Activity) {
        count -= 1
    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    companion object {
        var app: AWTApplication? = null
            private set
    }
}
