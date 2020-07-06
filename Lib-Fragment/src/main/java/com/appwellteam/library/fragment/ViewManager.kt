package com.appwellteam.library.fragment

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout

import com.appwellteam.library.AWTApplication

import java.lang.reflect.InvocationTargetException

/**
 * Created by Sambow on 15/9/19.
 */
@Suppress("unused")
open class ViewManager(protected var mainLayout: RelativeLayout, isMultiFragment: Boolean) {
    protected var mainWidth: Int = 0
    //endregion

    //region Public Method
    var isInitMainFragment = false
        private set
    private var mainKey: String? = null
    private var needAnimation = false

    private var multiFragment = false

    val currentFragment: BaseFragment?
        get() {
            val tFM = FragmentApplication.fragmentApp?.fragmentActivity?.supportFragmentManager
            val count = tFM?.backStackEntryCount ?: 0
            return if (count > 0) {
                tFM?.findFragmentByTag(tFM.getBackStackEntryAt(count - 1).name) as? BaseFragment
            } else {
                null
            }
        }

    open val isMenuShown: Boolean
        get() = false

    init {
        mainWidth = mainLayout.layoutParams.width
        multiFragment = isMultiFragment
    }
    //endregion

    //region Protected Method
    internal open fun initMainFragment(pStartUp: BaseFragment) {
        var bundle: Bundle? = pStartUp.arguments
        if (bundle == null) {
            bundle = Bundle()
        }
        pStartUp.arguments = bundle
        mainKey = pStartUp.javaClass.name
        val tFM = FragmentApplication.fragmentApp?.fragmentActivity?.supportFragmentManager
        val tFT = tFM?.beginTransaction()

        //		tFT = tFM.beginTransaction();
        tFT?.replace(mainLayout.id, pStartUp, mainKey)
        tFT?.addToBackStack(pStartUp.javaClass.name)
        tFT?.commit()
        isInitMainFragment = true
    }
    //endregion

    //region Private Method
    private fun createFragment(pClass: Class<out BaseFragment>): BaseFragment {
        try {
            val c = pClass.getConstructor()
            return c.newInstance() as BaseFragment
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

        throw RuntimeException("Create Fragment : " + pClass.name + " Fail")
    }

    fun setNeedAnimation(needAnimation: Boolean) {
        this.needAnimation = needAnimation
    }

    fun canBack(): Boolean {
        val tFM = FragmentApplication.fragmentApp?.fragmentActivity?.supportFragmentManager
        return tFM?.backStackEntryCount ?: 0 > 1
    }

    @JvmOverloads
    fun back(bundle: Bundle? = null) {
        hideKeyboard(AWTApplication.app?.activity?.currentFocus)
        val tFM = FragmentApplication.fragmentApp?.fragmentActivity?.supportFragmentManager
        if (bundle != null) {
            val temp = tFM?.findFragmentByTag(tFM.getBackStackEntryAt(tFM.backStackEntryCount - 2).name)
            temp?.arguments?.putAll(bundle)
        }
        tFM?.popBackStack()
    }

    @JvmOverloads
    fun backToMain(bundle: Bundle? = null) {
        val tFM = FragmentApplication.fragmentApp?.fragmentActivity?.supportFragmentManager

        if (bundle != null) {
            tFM?.findFragmentByTag(mainKey)?.arguments?.putAll(bundle)
        }

        val backCount = tFM?.backStackEntryCount ?: 0
        for (ii in backCount - 1 downTo 0) {
            if (mainKey == tFM?.getBackStackEntryAt(ii)?.name) {
                return
            } else {
                tFM?.popBackStack()
            }
        }
    }

    fun hideKeyboard(focusView: View?) {
        focusView ?: return

        val imm = AWTApplication.app?.activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(focusView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun changeMainView(pClass: Class<out BaseFragment>, pBundle: Bundle?) {

        hideKeyboard(AWTApplication.app?.activity?.currentFocus)

        val tFM = FragmentApplication.fragmentApp?.fragmentActivity?.supportFragmentManager
        val temp = tFM?.findFragmentByTag(pClass.name)


        if (temp == null || multiFragment) {
            val current = currentFragment
            val tBF = createFragment(pClass)
            val bundle = pBundle ?: Bundle()
            tBF.arguments = bundle

            if (tBF.isMenu) {
                backToMain()
            } else if (current != null && !current.isAddHistory) {
                tFM?.popBackStack()
            }
            val tFT = tFM?.beginTransaction()
            if (needAnimation) {
                //				tFT.setCustomAnimations(
                //						R.anim.fragment_in_from_right_side, 0,
                //						R.anim.fragment_in_from_left_side, 0);
                tFT?.setCustomAnimations(
                        R.anim.fragment_in_from_right_side, R.anim.fragment_out_from_left_side,
                        R.anim.fragment_in_from_left_side, R.anim.fragment_out_from_right_side)
            }
            tFT?.replace(mainLayout.id, tBF, pClass.name)
            tFT?.addToBackStack(tBF.javaClass.name)
            tFT?.commit()
            showMenu(false)
        } else {
            val tBF = temp as BaseFragment
            tBF.arguments?.putAll(pBundle ?: Bundle())
            val tCount = tFM.backStackEntryCount
            for (ii in tCount - 1 downTo 0) {
                if (tBF.javaClass.name == tFM.getBackStackEntryAt(ii).name) {
                    showMenu(false)
                    break
                } else {
                    tFM.popBackStack()
                }
            }
        }
    }

    open fun showMenu(pShow: Boolean) {}
    //endregion
}
