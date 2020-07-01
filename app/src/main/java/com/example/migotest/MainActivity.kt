package com.example.migotest

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import com.appwellteam.library.AWTActivity

class MainActivity: AWTActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun clickQues1(view: View)
    {
        if (view is AppCompatButton)
        {
            Log.d("click", view.text.toString())
        }
    }

    fun clickQues2(view: View)
    {
        if (view is AppCompatButton)
        {
            Log.d("click", view.text.toString())
        }
    }
}