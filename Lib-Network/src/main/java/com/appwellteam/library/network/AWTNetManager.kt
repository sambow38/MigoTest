package com.appwellteam.library.network

import android.net.Uri
import com.appwellteam.library.common.AWTLog
import com.appwellteam.library.common.AWTMath
import org.json.JSONObject
import java.util.*

/**
 * Created by Sambow on 15/12/11.
 */
@Suppress("unused")
class AWTNetManager {

    private val threads: HashMap<Int, AWTNetThread> = HashMap()

    internal enum class ConnectType(var value: String) {
        GET("GET"), POST("POST"), POST_JSON("POST"), UPLOAD("POST")
    }

    fun startGet(host: String, params: Map<String, String>, listener: AWTNetListener): Int {
        return start(host, params, listener, ConnectType.GET, null)
    }

    fun startPost(host: String, params: Map<String, String>, listener: AWTNetListener): Int {
        return start(host, params, listener, ConnectType.POST, null)
    }

    fun startPostJson(host: String, jsonObject: JSONObject, listener: AWTNetListener): Int {
        return start(host, jsonObject, listener, ConnectType.POST_JSON)
    }

    fun startUpload(host: String, params: Map<String, String>, photos: Map<String, Uri>, listener: AWTNetListener): Int {
        return start(host, params, listener, ConnectType.UPLOAD, photos)
    }

    private fun start(host: String, jsonObject: JSONObject?, listener: AWTNetListener, type: ConnectType): Int {
        val jObject = jsonObject ?: JSONObject()
        AWTLog.i("AWT-Network", "[onStart]-$host")
        listener.onStart()
        var taskID = AWTMath.randomInt()
        while (threads.containsKey(taskID)) {
            taskID = AWTMath.randomInt()
        }

        val thread = AWTNetThread(host, jObject, listener, type)
        threads[taskID] = thread
        listener.setManager(this, taskID)
        thread.start()
        return taskID
    }

    private fun start(host: String, params: Map<String, String>?, listener: AWTNetListener, type: ConnectType, photos: Map<String, Uri>?): Int {
        val parameter = params ?: HashMap()
        AWTLog.i("AWT-Network", "[onStart]-$host")
        listener.onStart()
        var taskID = AWTMath.randomInt()
        while (threads.containsKey(taskID)) {
            taskID = AWTMath.randomInt()
        }

        val thread = AWTNetThread(host, parameter, listener, type, photos)
        thread.setPhotos(photos)
        threads[taskID] = thread
        listener.setManager(this, taskID)
        thread.start()
        return taskID
    }

    fun isTaskAlive(taskID: Int): Boolean {
        return threads.containsKey(taskID)
    }

    fun killTask(taskID: Int) {
        threads.remove(taskID)
    }

    fun killAll() {
        threads.clear()
    }
}
