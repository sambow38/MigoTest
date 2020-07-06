package com.appwellteam.library.fragment


import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.appwellteam.library.AWTApplication
import com.appwellteam.library.control.imageView.AutoBgImageView
import com.appwellteam.library.extension.convertDimensionToPixel

/**
 * Created by Sambow on 15/9/19.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class BaseFragment : Fragment() {
    protected lateinit var tvTitle: TextView
    protected lateinit var tvTitleRight: TextView
    protected lateinit var tvTitleLeft: TextView
    protected lateinit var imgLeft: AutoBgImageView
    protected lateinit var imgRight: AutoBgImageView
    var isAddHistory = true
        protected set
    var isMenu = false
        protected set
    private lateinit var rootView: View
    private var defaultRootViewHeight = -1
    private var tempHeight = -1

    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        if (defaultRootViewHeight <= 0) {
            defaultRootViewHeight = rootView.height
            tempHeight = defaultRootViewHeight
        } else {
            val currentHeight = rootView.height
            if (tempHeight != currentHeight) {
                val heightDiff = defaultRootViewHeight - currentHeight
                if (heightDiff >= keyboardDiff) {
                    keyboardShown()
                } else {
                    keyboardHidden()
                }
                tempHeight = currentHeight
            }
        }
    }

    @get:LayoutRes
    protected abstract val mainLayout: Int

    protected val isPortrait: Boolean
        get() = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val tView = inflater.inflate(R.layout.awt_fragment_base, container, false)

        activity?.requestedOrientation = if (isPortrait) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

//        if (keyboardDiff == -1) {
//            keyboardDiff = context?.convertDimensionToPixel(R.dimen.dp_100) ?: -1
//        }

        val tTitle = getTitle(inflater)

        rootView = inflater.inflate(mainLayout, null)

        val titlePanel: LinearLayout = tView.findViewById(R.id.base_fragment_title_content)
        if (tTitle != null)
        {
            titlePanel.addView(tTitle)
        }

        findViews(rootView)
        initialize(rootView, savedInstanceState)

        val panel: LinearLayout = tView.findViewById(R.id.base_fragment_main_content)
        panel.addView(rootView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT))

        return tView
    }

    override fun onResume() {
        super.onResume()
        rootView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        val permissions = requiredPermissions()
        if (permissions != null && permissions.isNotEmpty()) {
            var needPermission = false
            for (i in permissions.indices) {
                needPermission = this.activity?.let { ActivityCompat.checkSelfPermission(it, permissions[i]) } != PackageManager.PERMISSION_GRANTED
                if (needPermission) {
                    break
                }
            }
            if (needPermission) {
                AWTApplication.app?.activity?.let { ActivityCompat.requestPermissions(it, permissions, CHECK_PERMISSION) }
            }
        }
    }

    protected fun checkPermission(permission: String): Boolean {
        val needPermission = this.activity?.let { ActivityCompat.checkSelfPermission(it, permission) } != PackageManager.PERMISSION_GRANTED

        if (needPermission) {
            AWTApplication.app?.activity?.let { ActivityCompat.requestPermissions(it, arrayOf(permission), CHECK_PERMISSION) }
        }
        return !needPermission
    }

    override fun onPause() {
        super.onPause()
        rootView.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
    }

    protected abstract fun findViews(view: View)

    protected abstract fun initialize(view: View, savedInstanceState: Bundle?)

    @SuppressLint("InflateParams")
    protected open fun getTitle(inflater: LayoutInflater): View? {
        val tView = inflater.inflate(R.layout.awt_title_bar, null)
        tvTitle = tView.findViewById(R.id.title_bar_tv_title)
        tvTitleRight = tView.findViewById(R.id.title_bar_tv_right)
        tvTitleLeft = tView.findViewById(R.id.title_bar_tv_left)
        imgLeft = tView.findViewById(R.id.title_bar_img_left)
        imgRight = tView.findViewById(R.id.title_bar_img_right)

        imgLeft.visibility = View.VISIBLE
        imgLeft.setOnClickListener {
            val isMenuShown = FragmentApplication.fragmentApp?.fragmentActivity?.viewManager?.isMenuShown ?: true
            FragmentApplication.fragmentApp?.fragmentActivity?.viewManager?.showMenu(!isMenuShown)
        }


        customTitle()

        return tView
    }

    protected open fun customTitle() {

    }

    protected fun setTitle(pRes: Int) {
        setTitle(view?.context?.resources?.getString(pRes))
    }

    protected fun setTitle(pTitle: String?) {
        tvTitle.text = pTitle
    }

    fun canBack(): Boolean {
        return true
    }

    fun requiredPermissions(): Array<String>? {
        return null
    }

    protected fun keyboardShown() {}

    protected fun keyboardHidden() {}

    companion object {
        private const val CHECK_PERMISSION = 123

        private var keyboardDiff = -1
            get() {
                if (field == -1) {
                    keyboardDiff = AWTApplication.app?.convertDimensionToPixel(R.dimen.dp_100) ?: -1
                }
                return field
            }
    }
}
