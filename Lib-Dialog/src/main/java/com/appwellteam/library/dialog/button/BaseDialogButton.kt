package com.appwellteam.library.dialog.button

import androidx.annotation.StringRes

import com.appwellteam.library.AWTApplication

/**
 * Android-Test
 * Created by sambow on 2017/3/31.
 */
@Suppress("unused")
class BaseDialogButton(var title: String, var action: BaseDialogButtonAction?) {

    interface BaseDialogButtonAction {
        fun onClicked()
    }

//    constructor(@StringRes title: Int, action: BaseDialogButtonAction?) : this(AWTApplication.app.getString(title), action)

}
