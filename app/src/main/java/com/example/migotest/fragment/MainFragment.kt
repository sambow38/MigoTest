package com.example.migotest.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import com.appwellteam.library.fragment.BaseFragment
import com.example.migotest.R

class MainFragment: BaseFragment() {
    private lateinit var btnQuestion1: AppCompatButton
    private lateinit var btnQuestion2: AppCompatButton

    private val fragmentQ1 = Question1()
    private val fragmentQ2 = Question2()

    override val mainLayout: Int
        get() = R.layout.fragment_main

    override fun findViews(view: View) {
        btnQuestion1 = view.findViewById(R.id.btn_q1)
        btnQuestion2 = view.findViewById(R.id.btn_q2)
    }

    override fun initialize(view: View, savedInstanceState: Bundle?) {
        view.setBackgroundColor(Color.WHITE)
        btnQuestion1.setOnClickListener {
            changePage(0)
        }
        btnQuestion2.setOnClickListener {
            changePage(1)
        }
        changePage(0)
    }

    override fun getTitle(inflater: LayoutInflater): View? {
        return null
    }

    private fun changePage(index: Int)
    {
        val transaction= childFragmentManager.beginTransaction()
        transaction.replace(R.id.container, if (index == 0) fragmentQ1 else fragmentQ2)
        transaction.commit()
    }
}