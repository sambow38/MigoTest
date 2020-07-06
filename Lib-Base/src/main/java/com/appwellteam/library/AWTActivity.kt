package com.appwellteam.library

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.annotation.Nullable
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.appwellteam.library.base.R
import com.appwellteam.library.extension.convertDpToPixel


/**
 * Created by Sambow on 15/9/19.
 */
@Suppress("unused")
open class AWTActivity : AppCompatActivity() {
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        AWTApplication.app?.activity = this
    }

    override fun onResume() {
        super.onResume()
        if (this != AWTApplication.app?.activity) {
            AWTApplication.app?.activity = this
        }
    }

    @JvmOverloads
    fun showAlertDialog(title: String, msg: String, buttons: ArrayList<DialogButton> = ArrayList(), gravity: Int = Gravity.CENTER) {
        this@AWTActivity.runOnUiThread {
            val tDialog = Dialog(this@AWTActivity, R.style.AWTDialogDesignedStyle)
            tDialog.setContentView(R.layout.awt_dialog)

            if (buttons.size == 0)
            {
                buttons.add(DialogButton(R.string.sys_ok, "0", null))
            }

            //Title
            val tTvTitle: AppCompatTextView = tDialog.findViewById(R.id.tv_title)
            if (!TextUtils.isEmpty(title)) {
                tTvTitle.visibility = View.VISIBLE
                tTvTitle.text = title
            }

            //Message
            val tSvMessage: ScrollView = tDialog.findViewById(R.id.sv_msg)
            if (!TextUtils.isEmpty(msg)) {
                val tTvMessage: AppCompatTextView = tDialog.findViewById(R.id.tv_msg)
                tTvMessage.movementMethod = ScrollingMovementMethod.getInstance()
                tTvMessage.gravity = gravity
                tTvMessage.text = msg
            } else {
                tTvTitle.setPadding(0, 0, 0, convertDpToPixel(24f).toInt())
                tSvMessage.visibility = View.GONE
            }

            //Buttons
            val tFooter: LinearLayout = tDialog.findViewById(R.id.dialog_footer)

            val tHeight = convertDpToPixel(36f).toInt()
            if (buttons.size == 1) {
                val tDialogButton = buttons[0]
                val tBtn = getDialogButton(tDialog, tDialogButton)
                val params = LinearLayout.LayoutParams(convertDpToPixel(124f).toInt(), tHeight, 0f)
                tBtn.setPadding(0, 0, 0, 0)
                tBtn.layoutParams = params
                tFooter.addView(tBtn)
            } else {
                for (i in 0 until buttons.size) {
                    val tDialogButton = buttons[i]
                    val tBtn = getDialogButton(tDialog, tDialogButton)
                    val params = LinearLayout.LayoutParams(0, tHeight, 1f)
                    params.rightMargin = if (i == buttons.size - 1) 0 else convertDpToPixel(8f).toInt()
                    tBtn.layoutParams = params
                    tFooter.addView(tBtn)
                }
            }

            tDialog.setCanceledOnTouchOutside(false)
            tDialog.show()
        }
    }

    @SuppressLint("InflateParams")
    private fun getDialogButton(pDialog: Dialog, pButton: DialogButton): AppCompatButton {
        val tButton: AppCompatButton = if (pButton.action == "0") {
            layoutInflater.inflate(R.layout.awt_dialog_white_button, null) as AppCompatButton
        } else {
            layoutInflater.inflate(R.layout.awt_dialog_blue_button, null) as AppCompatButton
        }
        tButton.text = pButton.title
        tButton.setOnClickListener {
            pButton.doBeforeDismiss()
            pDialog.dismiss()
        }
        tButton.visibility = View.VISIBLE
        return tButton
    }

    class DialogButton {
        var title = ""
            internal set
        var action = ""
            internal set // 0:白底(Close) or 1:藍底(Action)
        var listener: BeforeDismissListener? = null
            internal set

        constructor(title: String?, action: String, listener: BeforeDismissListener?) {
            this.title = title ?: ""
            this.action = if (action.isNotEmpty()) action else "0"
            this.listener = listener
        }

        constructor(@StringRes title: Int, action: String, listener: BeforeDismissListener?) {
            this.title = try {
                AWTApplication.app?.getString(title) ?: ""
            } catch (e: Exception) {
                ""
            }

            this.action = if (!TextUtils.isEmpty(action)) action else "0"
            this.listener = listener
        }

        interface BeforeDismissListener {
            fun beforeDismiss()
        }

        fun doBeforeDismiss() {
            listener?.beforeDismiss()
        }
    }
}
