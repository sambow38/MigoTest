package com.example.migotest.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.appwellteam.library.fragment.BaseFragment
import com.example.migotest.R

class Question2: BaseFragment() {
    override val mainLayout: Int
        get() = R.layout.fragment_q2

    override fun findViews(view: View) {

    }

    override fun initialize(view: View, savedInstanceState: Bundle?) {
        view.setBackgroundColor(Color.RED)
    }

    override fun getTitle(inflater: LayoutInflater): View? {
        return null
    }
}