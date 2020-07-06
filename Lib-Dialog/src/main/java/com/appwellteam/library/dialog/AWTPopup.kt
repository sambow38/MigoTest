package com.appwellteam.library.dialog

import android.content.DialogInterface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.appwellteam.library.AWTApplication
import com.appwellteam.library.common.AWTCommon

/**
 * Created by Sambow on 15/12/5.
 */
@Suppress("unused")
abstract class AWTPopup<T: Any?> @JvmOverloads constructor(anyObject: T? = null) {
    protected var activity: AppCompatActivity = AWTApplication.app?.activity ?: throw RuntimeException("activity is null")
        private set
    private val awtDialog: AWTDialog

    private val isShowing: Boolean
        get() = awtDialog.isShowing

    init {
        awtDialog = AWTDialog(activity, android.R.style.Theme_Translucent_NoTitleBar)
        run {
            val tFrameLayout = FrameLayout(activity)
            val layoutInflater = LayoutInflater.from(activity)
            val tView = layoutInflater.inflate(getMainViewID(anyObject), tFrameLayout)
            tView.setOnTouchListener { _, _ -> true }
            initView(tView, anyObject)
            awtDialog.setOnDismissListener { dialog -> this@AWTPopup.onDismiss(dialog) }
            awtDialog.setModal()

            //        int screenHeight = AWTApplication.getScreenHeight();
            var statusHeight = 0
            val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                statusHeight = activity.resources.getDimensionPixelSize(resourceId)
            }

            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            params.setMargins(0, statusHeight, 0, 0)

            //		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            awtDialog.setContentView(tFrameLayout, params, Gravity.CENTER_HORIZONTAL)
        }
    }


    abstract fun getMainViewID(anyObject: T?): Int

    abstract fun initView(view: View, anyObject: T?)

    abstract fun onDismiss(dialog: DialogInterface)

    private fun showDialog() {
        awtDialog.show()
    }

    private fun dismissDialog() {
        awtDialog.window ?: return
        awtDialog.dismiss()
    }

    fun show() {
        if (!activity.isFinishing && !isShowing) {
            AWTCommon.hideKeyboard()
            showDialog()
        }
    }

    fun dismiss() {
        if (!activity.isFinishing && isShowing) {
            dismissDialog()
        }
    }

    fun setCancelable(cancelable: Boolean) {
        awtDialog.setCancelable(cancelable)
    }

    fun setOnKeyDownListener(keyListener: DialogInterface.OnKeyListener) {
        awtDialog.setOnKeyListener(keyListener)
    }
}
