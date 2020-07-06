package com.appwellteam.library.fragment

import com.appwellteam.library.AWTApplication

/**
 * Created by Sambow on 15/9/19.
 */
@Suppress("unused")
abstract class FragmentApplication : AWTApplication() {

    val fragmentActivity: FragmentActivity?
        get() = activity as? FragmentActivity

    abstract fun showLeaveDialog()

    companion object {
        val viewManager: ViewManager?
            get() = fragmentApp?.fragmentActivity?.viewManager

        val fragmentApp: FragmentApplication?
            get() = app as? FragmentApplication
    }
}
