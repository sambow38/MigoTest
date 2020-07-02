package com.example.migotest.popup

import android.content.DialogInterface
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.appwellteam.library.dialog.AWTPopup
import com.example.migotest.R
import com.example.migotest.fragment.Question2

class PassDetailPopup(pass: Question2.Pass, private val dismissCallback: OnDismiss? = null) : AWTPopup<Question2.Pass>(pass) {
    interface OnDismiss {
        fun onDismiss()
    }

    override fun getMainViewID(anyObject: Question2.Pass?): Int {
        return R.layout.popup_pass_detail
    }

    override fun initView(view: View, anyObject: Question2.Pass?) {
        fun updateViews() {
            anyObject?.let {
                view.findViewById<AppCompatTextView>(R.id.name).text = "PASS Card (${it.period})"
                view.findViewById<AppCompatTextView>(R.id.create).text = "create: ${it.createdAtStr}"
                view.findViewById<AppCompatTextView>(R.id.serial).text = "serial: ${it.serialNumber}"
                if (it.isActive) {
                    view.findViewById<AppCompatTextView>(R.id.status).text = "status: Activation"
                    view.findViewById<AppCompatTextView>(R.id.start_end).text =
                        "period: ${it.startAtStr} - ${it.expireAtStr}"
                    view.findViewById<AppCompatTextView>(R.id.start_end).visibility = View.VISIBLE
                    view.findViewById<AppCompatButton>(R.id.active).visibility = View.GONE
                } else {
                    view.findViewById<AppCompatTextView>(R.id.status).text = "status: Inactivated"
                    view.findViewById<AppCompatTextView>(R.id.start_end).text = ""
                    view.findViewById<AppCompatTextView>(R.id.start_end).visibility = View.GONE
                    view.findViewById<AppCompatButton>(R.id.active).visibility = View.VISIBLE
                }
            }
        }

        updateViews()

        view.findViewById<AppCompatButton>(R.id.active).setOnClickListener {
            anyObject?.active()
            updateViews()
        }

        view.findViewById<AppCompatButton>(R.id.close).setOnClickListener {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        dismissCallback?.onDismiss()
    }
}