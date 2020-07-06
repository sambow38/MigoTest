package com.appwellteam.library.network.new.callback


import com.appwellteam.library.network.new.AWTRequestManager
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection
@Suppress("unused")
abstract class AWTWebCallback<T>(private val requestManager: AWTRequestManager, private val taskKey: String) : Callback<T> {
    private val taskID: Int = requestManager.updateApiTaskID(taskKey, false)

    abstract fun onSuccess(response: T)

    abstract fun onError(errorCode: Int, t: Throwable)

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun preSuccess(result: T?): T? {
        return result
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (taskID > 0) {
            if (!requestManager.containsApiTaskID(taskKey, taskID)) {
                return
            }
        }

        val code = response.code()

        try {
            if (response.isSuccessful) {
                if (code == HttpURLConnection.HTTP_OK) {
                    var result = response.body()
                    //                    CYLog.d(TAG, "[onSuccess] [" + taskKey + "] response = " + result);

                    result = preSuccess(result)

                    if (result != null) {
                        onSuccess(result)
                    }
                } else {
//                    var message: String? = response.message()
                    //                    CYLog.d(TAG, "[onError] [" + taskKey + "] error = " + message);
//                    if (message == null) {
                        //>"<
//                        message = "time out and retry"
//                    }
                    //>"< error handle
                    //                    onError(NetworkConstant.CUSTOM_HTTP_STATUS_CODE_CONNECTION_TIMEOUT, new Throwable(message));
                }
            } else {
                //>"< 額外做 647拆解
                val errorBody = response.errorBody()!!.string()
                //                CYLog.d(TAG, "[onError] [" + taskKey + "] error = " + errorBody);
                val json = JSONObject(errorBody)
                // if check specific error status code that need to refresh token, do it here
                var message: String? = null
                try {
                    message = json.optString("message")
                    if (message == null) {
                        //>"<
                        message = "time out and retry"
                        //                        message = MainApplication.getStr(R.string.network_timeout_retry);
                    }

                } catch (e: Exception) {
                }

                val errorStatusCode = json.optInt("statusCode")
                //>"< error handle
                //                if (errorStatusCode == NetworkConstant.CUSTOM_HTTP_STATUS_CODE_GENERAL_ERROR) {
                //                    MainApplication.getCYApp().getMainActivity().showToast(message);
                //                }
                onError(errorStatusCode, Throwable(message))
            }
        } catch (e: Exception) {
            try {
                onFailure(call, Throwable(e.message + ""))
            } catch (e1: Exception) {
            }

        }

        requestManager.clearApiTaskID(taskKey)
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        //        CYLog.d(TAG, "[onFailure] [" + taskKey + "] error = " + t);
        try {
            //>"<
            //            Exception throwable = new Exception(MainApplication.getStr(R.string.network_timeout_retry));
            //            onError(NetworkConstant.CUSTOM_HTTP_STATUS_CODE_CONNECTION_TIMEOUT, throwable);
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getMessage(t: Throwable?): String? {
        var msg: String? = null
        if (t != null) {
            msg = t.message
        }
        return msg
    }

    companion object {
        private const val TAG = "REST-API"
    }
}