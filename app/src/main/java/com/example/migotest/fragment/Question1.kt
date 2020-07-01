package com.example.migotest.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import com.appwellteam.library.common.AWTCommon
import com.appwellteam.library.fragment.BaseFragment
import com.appwellteam.library.network.new.util.AWTWebCommon
import com.example.migotest.R
import com.example.migotest.network.ApiResponse
import com.example.migotest.network.EventModel
import com.example.migotest.network.NetworkManager
import com.google.gson.Gson
import java.util.ArrayList

class Question1: BaseFragment() {
    private val networkManager = NetworkManager()
    private lateinit var btnWifi: AppCompatButton
    private lateinit var btnApi: AppCompatButton
    override val mainLayout: Int
        get() = R.layout.fragment_q1
    override fun findViews(view: View) {
        btnWifi = view.findViewById(R.id.btn_wifi)
        btnApi = view.findViewById(R.id.btn_api)
    }

    override fun initialize(view: View, savedInstanceState: Bundle?) {
        view.setBackgroundColor(Color.BLUE)
        btnWifi.setOnClickListener {
            AWTCommon.showToast(String.format("isConnected: %s\nisWifi: %s", AWTWebCommon.isNetworkConnect, AWTWebCommon.isWifiConnect))
        }
        btnApi.setOnClickListener {
            networkManager.callApi()
        }
    }

    override fun getTitle(inflater: LayoutInflater): View? {
        return null
    }

    override fun onResume() {
        super.onResume()
        networkManager.eventBus.register(this)
    }

    override fun onPause() {
        super.onPause()
        networkManager.eventBus.unregister(this)
    }

    fun onEvent(event: EventModel) {
        if (event.success)
        {
            AWTCommon.showToast(String.format("isWifi: %s\nresponse: %s", event.isWifi.toString(), Gson().toJson(event.model)))
        }
        else
        {
            AWTCommon.showToast(String.format("isWifi: %s\napi error"))
        }
    }
}