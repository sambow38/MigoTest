package com.example.migotest.network

import com.appwellteam.library.network.new.AWTRequestManager
import com.appwellteam.library.network.new.callback.AWTWebCallback
import com.appwellteam.library.network.new.util.AWTWebCommon

class NetworkManager: AWTRequestManager() {
    private val publicServer: NetworkService =
        getRetrofit("https://code-test.migoinc-dev.com").create(NetworkService::class.java)
    private val privateServer: NetworkService =
        getRetrofit("http://192.168.2.2").create(NetworkService::class.java)

    fun callApi()
    {
        val isWifi = AWTWebCommon.isWifiConnect
        val call = (if (isWifi) privateServer else publicServer).restfulApi()
        call.enqueue(
            object :
                AWTWebCallback<ApiResponse>(this@NetworkManager, "publicApi"){
                override fun onSuccess(response: ApiResponse) {
                    this@NetworkManager.eventBus.post(
                        EventModel(
                            true,
                            isWifi,
                            response)
                    )
                }

                override fun onError(errorCode: Int, t: Throwable) {
                    this@NetworkManager.eventBus.post(
                        EventModel(
                            false,
                            isWifi,
                            null)
                    )
                }
            }
        )
    }
}

data class EventModel(val success: Boolean, val isWifi: Boolean, val model: ApiResponse?)