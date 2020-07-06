package com.appwellteam.library.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

/**
 * Created by Sambow on 15/12/5.
 */
class AWTDialog(context: Context, theme: Int) : Dialog(context, theme) {
    private var view: View? = null
    private var modal = false

    fun setContentView(view: View, params: FrameLayout.LayoutParams, gravity: Int) {
        super.setContentView(view, params)

        this.view = window?.decorView
        val lp = window?.attributes
        lp?.gravity = gravity
        lp?.x = params.leftMargin
        lp?.y = params.topMargin
        lp?.width = params.width
        lp?.height = params.height
        window?.attributes = lp

        params.setMargins(0, 0, 0, 0)
        view.layoutParams = params
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (this.modal) {
            return super.onTouchEvent(event)
        }
        val outRect = Rect()
        this.view?.getHitRect(outRect)
        if (outRect.contains(event.x.toInt(), event.y.toInt())) {
            return super.onTouchEvent(event)
        }
        dismiss()
        return true
    }

    fun setModal() {
        this.modal = true
    }
}
