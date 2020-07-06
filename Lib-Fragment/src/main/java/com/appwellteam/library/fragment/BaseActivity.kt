package com.appwellteam.library.fragment

import android.os.Bundle

/**
 * Created by Sambow on 15/11/8.
 */
abstract class BaseActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.awt_activity_base)
        mainLayout = findViewById(R.id.main_content)
    }

    override fun initViewManager(): ViewManager {
        viewManager = ViewManager(mainLayout, isMultiFragment)
        viewManager!!.initMainFragment(initMainFragment())
        return viewManager!!
    }
}
