package com.example.migotest.popup

import android.content.DialogInterface
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatRadioButton
import com.appwellteam.library.common.AWTCommon
import com.appwellteam.library.dialog.AWTPopup
import com.example.migotest.R

class PassPopup(private val callback: OnPassCreated) : AWTPopup<Any>() {
    interface OnPassCreated
    {
        fun onCreated(isDay: Boolean, number: Int)
    }

    private lateinit var radioGroup: RadioGroup
    private lateinit var radioDay: AppCompatRadioButton
    private lateinit var radioHour: AppCompatRadioButton
    private lateinit var editText: AppCompatEditText
    private lateinit var btnOK: AppCompatButton
    private lateinit var btnCancel: AppCompatButton
//    private var currentSelectRes: Int? = null

    private val isDataComplete: Boolean
        get() {
            return when {
                radioGroup.checkedRadioButtonId != radioDay.id &&
                        radioGroup.checkedRadioButtonId != radioHour.id -> {
                    false
                }
                editText.text.toString().isEmpty() -> {
                    false
                }
                editText.text.toString().toInt() == 0 -> {
                    false
                }
                else -> true
            }
        }

    override fun getMainViewID(anyObject: Any?): Int {
        return R.layout.popup_pass
    }

    override fun initView(view: View, anyObject: Any?) {
        radioGroup = view.findViewById(R.id.radio)
        editText = view.findViewById(R.id.edt)
        btnOK = view.findViewById(R.id.ok)
        btnCancel = view.findViewById(R.id.cancel)
        radioDay = view.findViewById(R.id.radio_btn_day)
        radioHour = view.findViewById(R.id.radio_btn_hour)
        radioGroup.check(radioDay.id)
//        radioGroup.setOnCheckedChangeListener { _, i -> currentSelectRes = i }

        btnOK.setOnClickListener {
            if (isDataComplete)
            {
//                val scale = when (radioGroup.checkedRadioButtonId) {
//                    R.id.radio_hour -> 60 * 60
//                    R.id.radio_day -> 60 * 60 * 24
//                    else -> 0
//                }
//                val number = editText.text.toString().toInt()
                callback.onCreated(radioGroup.checkedRadioButtonId == radioDay.id, editText.text.toString().toInt())
                dismiss()
            }
            else
            {
                AWTCommon.showToast("data not completion")
            }
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {

    }

}