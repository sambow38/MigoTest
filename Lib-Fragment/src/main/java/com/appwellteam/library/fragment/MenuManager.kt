package com.appwellteam.library.fragment

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout

import com.appwellteam.library.AWTApplication


/**
 * Created by Sambow on 15/11/8.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class MenuManager(protected var m_menu: RelativeLayout, pMain: RelativeLayout, protected var m_vShadow: View, protected var m_vBGShadow: View, isMultiFragment: Boolean) : ViewManager(pMain, isMultiFragment) {

    protected var menuWidth: Int = m_menu.layoutParams.width
    protected var shadowWidth: Int = m_vShadow.layoutParams.width
    protected var menuMode = MenuMode.FRONT

    override val isMenuShown: Boolean
        get() = when (menuMode) {
            MenuMode.FRONT -> m_menu.x == 0f
            MenuMode.BACK -> mainLayout.x != 0f
        }

    @Suppress("RedundantVisibilityModifier")
    internal override fun initMainFragment(pStartUp: BaseFragment) {
        val tMenu = initMenuFragment()

        val tFM = FragmentApplication.fragmentApp?.fragmentActivity?.supportFragmentManager
        val tFT = tFM?.beginTransaction()
        tFT?.add(m_menu.id, tMenu, tMenu.javaClass.name)
        tFT?.commit()

        super.initMainFragment(pStartUp)
    }

    override fun showMenu(pShow: Boolean) {
        hideKeyboard(AWTApplication.app?.activity?.currentFocus)
        val isMenuShown = isMenuShown
        if (pShow && !isMenuShown) {
            menuAnimationShow()
        } else if (!pShow && isMenuShown) {
            menuAnimationHide()
        }
    }

    protected fun menuAnimationShow() {
        if (menuMode == MenuMode.FRONT) {
            m_menu.bringToFront()
            val tLp = RelativeLayout.LayoutParams(
                    menuWidth, RelativeLayout.LayoutParams.MATCH_PARENT)
            tLp.setMargins(0, 0, 0, 0)
            m_menu.layoutParams = tLp
            m_menu.invalidate()

            m_vBGShadow.visibility = View.VISIBLE

            val translate = TranslateAnimation(
                    (0 - menuWidth).toFloat(), 0f, 0f, 0f)
            translate.duration = 300
            m_menu.startAnimation(translate)

            val alpha = AlphaAnimation(0f, 1f)
            alpha.duration = 300
            m_vBGShadow.startAnimation(alpha)
        } else if (menuMode == MenuMode.BACK) {
            var tLp = RelativeLayout.LayoutParams(
                    mainWidth, RelativeLayout.LayoutParams.MATCH_PARENT)
            tLp.setMargins(menuWidth, 0, 0 - menuWidth, 0)
            mainLayout.layoutParams = tLp
            mainLayout.invalidate()

            tLp = RelativeLayout.LayoutParams(shadowWidth,
                    RelativeLayout.LayoutParams.MATCH_PARENT)
            tLp.setMargins(menuWidth - shadowWidth, 0, 0, 0)
            m_vShadow.layoutParams = tLp
            m_vShadow.invalidate()

            val translate = TranslateAnimation(
                    (0 - menuWidth).toFloat(), 0f, 0f, 0f)
            translate.duration = 300
            mainLayout.startAnimation(translate)

            val translate2 = TranslateAnimation(
                    (0 - menuWidth).toFloat(), 0f, 0f, 0f)
            translate2.duration = 300
            m_vShadow.startAnimation(translate2)
        }
    }

    protected fun menuAnimationHide() {
        if (menuMode == MenuMode.FRONT) {
            m_menu.bringToFront()
            val tLp = RelativeLayout.LayoutParams(
                    menuWidth, RelativeLayout.LayoutParams.MATCH_PARENT)
            tLp.setMargins(0 - menuWidth, 0, 0, 0)
            m_menu.layoutParams = tLp
            m_menu.invalidate()

            m_vBGShadow.visibility = View.INVISIBLE

            val translate = TranslateAnimation(menuWidth.toFloat(),
                    0f, 0f, 0f)
            translate.duration = 300
            m_menu.startAnimation(translate)

            val alpha = AlphaAnimation(1f, 0f)
            alpha.duration = 300
            m_vBGShadow.startAnimation(alpha)
        } else if (menuMode == MenuMode.BACK) {
            var tLp = RelativeLayout.LayoutParams(
                    mainWidth, RelativeLayout.LayoutParams.MATCH_PARENT)
            tLp.setMargins(0, 0, 0, 0)
            mainLayout.layoutParams = tLp
            mainLayout.invalidate()

            tLp = RelativeLayout.LayoutParams(shadowWidth,
                    RelativeLayout.LayoutParams.MATCH_PARENT)
            tLp.setMargins(0 - shadowWidth, 0, 0, 0)
            m_vShadow.layoutParams = tLp
            m_vShadow.invalidate()

            val translate = TranslateAnimation(menuWidth.toFloat(),
                    0f, 0f, 0f)
            translate.duration = 300
            mainLayout.startAnimation(translate)

            val translate2 = TranslateAnimation(
                    menuWidth.toFloat(), 0f, 0f, 0f)
            translate2.duration = 300
            m_vShadow.startAnimation(translate2)
        }
    }

    internal fun setMenuMode(menuMode: MenuMode) {
        this.menuMode = menuMode

        if (menuMode == MenuMode.FRONT) {
            val tLp = RelativeLayout.LayoutParams(
                    menuWidth, RelativeLayout.LayoutParams.MATCH_PARENT)
            tLp.setMargins(0 - menuWidth, 0, 0, 0)
            m_menu.layoutParams = tLp
            m_menu.invalidate()
        }
    }

    protected abstract fun initMenuFragment(): BaseFragment
}
