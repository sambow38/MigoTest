package com.appwellteam.library.fragment

import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import com.appwellteam.library.common.AWTAppInfo

/**
 * Created by Sambow on 15/11/8.
 */
@Suppress("unused")
abstract class MenuActivity : FragmentActivity() {
    protected lateinit var menu: RelativeLayout
    protected lateinit var shadow: View
    protected lateinit var bgShadow: View
    protected abstract val menuScale: Float
    protected abstract val shadowType: MenuMode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.awt_activity_menu)

        menu = findViewById<View>(R.id.menu_content) as RelativeLayout
        mainLayout = findViewById<View>(R.id.main_content) as RelativeLayout
        mainLayout.setOnClickListener { }
        shadow = findViewById(R.id.main_shadow)
        bgShadow = findViewById(R.id.main_shadow_bg)
        bgShadow.setOnTouchListener { _, _ ->
            viewManager?.showMenu(false)
            true
        }

        initContentSize()
    }

    override fun initViewManager(): ViewManager {
        viewManager = createViewManager()
        viewManager!!.initMainFragment(initMainFragment())
        return viewManager!!
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun createViewManager(): ViewManager {
        val menuManager = object : MenuManager(menu, mainLayout, shadow, bgShadow, isMultiFragment) {
            override fun initMenuFragment(): BaseFragment {
                return this@MenuActivity.initMenuFragment()
            }
        }
        menuManager.setMenuMode(shadowType)

        return menuManager
    }

    protected abstract fun initMenuFragment(): MenuBaseFragment

    private fun initContentSize() {
        val tMenuScale = menuScale
        val tWidth = AWTAppInfo.screenWidth
        var tLP = RelativeLayout.LayoutParams(
                (tWidth * tMenuScale).toInt(),
                RelativeLayout.LayoutParams.MATCH_PARENT)
        menu.layoutParams = tLP
        menu.invalidate()

        tLP = RelativeLayout.LayoutParams(tWidth,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        mainLayout.layoutParams = tLP
        mainLayout.invalidate()

        val tShadowWidth = resources.getDimensionPixelSize(R.dimen.dp_10)
        tLP = RelativeLayout.LayoutParams(tShadowWidth,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        tLP.setMargins(0 - tShadowWidth, 0, 0, 0)
        shadow.layoutParams = tLP
        shadow.invalidate()
    }

    override fun onBackPressed() {
        if ((viewManager as MenuManager).isMenuShown) {
            (viewManager as MenuManager).showMenu(false)
        } else {
            super.onBackPressed()
        }
    }
}
