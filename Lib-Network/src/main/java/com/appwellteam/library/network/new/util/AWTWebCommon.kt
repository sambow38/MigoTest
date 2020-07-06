package com.appwellteam.library.network.new.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.appwellteam.library.AWTApplication
import retrofit2.Retrofit


object AWTWebCommon {
    val isNetworkConnect: Boolean
        get() {
            val context = AWTApplication.app ?: return false
            val conMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = conMgr.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnected
        }

    private val activeNetwork: Network?
        get() {
            val context = AWTApplication.app ?: return null
            val conMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    return conMgr.activeNetwork
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    conMgr.allNetworks.forEach {
                        if (conMgr.getNetworkInfo(it).isConnected)
                        {
                            return it
                        }
                    }
                    return null
                }
                else -> {
                    return null
                }
            }
        }

    val isWifiConnect: Boolean
        get() {
            val context = AWTApplication.app ?: return false
            val conMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (activeNetwork != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                conMgr.getNetworkCapabilities(activeNetwork!!).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            } else {
                conMgr.activeNetworkInfo.isConnected && conMgr.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
            }
        }
}
