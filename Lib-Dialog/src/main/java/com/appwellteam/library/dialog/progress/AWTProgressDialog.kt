package com.appwellteam.library.dialog.progress

import android.content.DialogInterface
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatTextView
import com.appwellteam.library.dialog.AWTPopup
import com.appwellteam.library.dialog.R
import java.util.*

@Suppress("unused")
class AWTProgressDialog : AWTPopup<Nothing>() {
    private lateinit var title: AppCompatTextView
    private lateinit var percent: AppCompatTextView
    private lateinit var process: ProgressBar

    override fun getMainViewID(anyObject: Nothing?): Int {
        return R.layout.pupop_progress
    }

    override fun initView(view: View, anyObject: Nothing?) {
        title = view.findViewById(R.id.progress_title)
        percent = view.findViewById(R.id.progress_right)
        process = view.findViewById(R.id.progress)
    }

    override fun onDismiss(dialog: DialogInterface) {

    }

    fun show(title: String, message: String)
    {
        this.title.text = String.format("%s\n%s", title, message, Locale.getDefault())
        super.show()
    }
}
