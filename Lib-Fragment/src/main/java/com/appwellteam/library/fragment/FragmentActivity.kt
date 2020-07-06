package com.appwellteam.library.fragment

import android.widget.RelativeLayout
import com.appwellteam.library.AWTActivity

/**
 * Created by Sambow on 15/11/8.
 */
abstract class FragmentActivity : AWTActivity() {
    var viewManager: ViewManager? = null
         protected set
    protected lateinit var mainLayout: RelativeLayout
    private var isFirst = true

    protected val isMultiFragment: Boolean
        get() = false

    override fun onStart() {
        super.onStart()
        if (isFirst || viewManager == null) {
            viewManager = initViewManager()
            if (viewManager == null || !viewManager!!.isInitMainFragment) {
                throw RuntimeException("You should init View Manager with Main Fragment")
            }
            isFirst = false
        }
    }

    protected abstract fun initViewManager(): ViewManager
    protected abstract fun initMainFragment(): BaseFragment

    override fun onBackPressed() {
        val current = viewManager?.currentFragment
        if (current != null && !current.canBack()) {

        } else if (viewManager?.canBack() == true) {
            super.onBackPressed()
        } else {
            FragmentApplication.fragmentApp?.showLeaveDialog()
        }
    }
}
