package com.appwellteam.library.dialog.base

import android.content.DialogInterface
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import com.appwellteam.library.dialog.AWTPopup
import com.appwellteam.library.dialog.R
import com.appwellteam.library.dialog.button.BaseDialogButton

/**
 * Android-Test
 * Created by sambow on 2017/3/31.
 */
@Suppress("unused")
class AWTBaseDialog : AWTPopup<Nothing>() {
    private lateinit var txtTitle: AppCompatTextView
    private lateinit var txtMsg: AppCompatTextView
    private lateinit var txtLeft: AppCompatTextView
    private lateinit var txtRight: AppCompatTextView
    private lateinit var panelBtn: View

    override fun getMainViewID(anyObject: Nothing?): Int {
        return R.layout.popup_base
    }

    override fun initView(view: View, anyObject: Nothing?) {
        txtTitle = view.findViewById(R.id.popup_title)
        txtMsg = view.findViewById(R.id.popup_msg)
        txtLeft = view.findViewById(R.id.popup_btn_left)
        txtRight = view.findViewById(R.id.popup_btn_right)

        panelBtn = view.findViewById(R.id.popup_panel_btn)
    }

    override fun onDismiss(dialog: DialogInterface) {

    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun show(@StringRes title: Int, @StringRes msg: Int, btn1: BaseDialogButton, btn2: BaseDialogButton) {

        show(activity.getString(title), activity.getString(msg), btn1, btn2)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun show(title: String, msg: String, btn1: BaseDialogButton?, btn2: BaseDialogButton?) {
        if (btn1 == null && btn2 != null) {
            show(title, msg, btn2, btn1)
            return
        }

        setTextData(txtTitle, title)
        setTextData(txtMsg, msg)

        when {
            btn1 == null -> panelBtn.visibility = View.GONE
            btn2 == null -> {
                panelBtn.visibility = View.VISIBLE
                setTextData(txtLeft, "")
                setTextData(txtRight, btn1)
            }
            else -> {
                panelBtn.visibility = View.VISIBLE
                setTextData(txtLeft, btn2)
                setTextData(txtRight, btn1)
            }
        }

        show()
    }

    private fun setTextData(textView: AppCompatTextView, action: BaseDialogButton) {
        setTextData(textView, action.title)
        textView.setOnClickListener {
            dismiss()
            action.action?.onClicked()
        }
    }

    private fun setTextData(textView: AppCompatTextView, data: String?) {
        if (data?.isEmpty() != false) {
            textView.visibility = View.GONE
            textView.text = ""
        } else {
            textView.visibility = View.VISIBLE
            textView.text = data
        }
    }
}
