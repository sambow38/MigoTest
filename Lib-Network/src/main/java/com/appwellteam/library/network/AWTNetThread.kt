package com.appwellteam.library.network

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import org.json.JSONObject
import java.util.*

/**
 * Created by Sambow on 15/12/11.
 */
@Suppress("unused")
internal class AWTNetThread : Thread {
    //	private String apiUniKey;
    private var host: String
    private var params: Map<String, String>? = null
    private var uriMap: Map<String, Uri>? = null
    private var listener: AWTNetListener
    private var type: AWTNetManager.ConnectType
    private var photos: Map<String, Uri>? = null
    private var jsonObject: JSONObject? = null

    constructor(host: String, params: Map<String, String>?, listener: AWTNetListener, type: AWTNetManager.ConnectType, uriMap: Map<String, Uri>?) : super() {
        //		this.apiUniKey = genApiKey(host, params);
        this.host = host
        this.params = params ?: HashMap()
        this.listener = listener
        this.type = type
        this.uriMap = uriMap
    }

    constructor(host: String, jsonObject: JSONObject?, listener: AWTNetListener, type: AWTNetManager.ConnectType) : super() {
        //		this.apiUniKey = genApiKey(host, params);
        this.host = host
        this.jsonObject = jsonObject ?: JSONObject()
        this.listener = listener
        this.type = type
    }

    fun setPhotos(photos: Map<String, Uri>?) {
        this.photos = photos
    }

    private fun genApiKey(host: String, params: Map<String, String>): String {
        val sortedParamMap = TreeMap(params)
        val canonicalQS = canonicalizeForSign(sortedParamMap)
        val key = "$host::$canonicalQS"
        return Base64.encodeToString(key.toByteArray(), Base64.DEFAULT)
    }

    private fun canonicalizeForSign(sortedParamMap: SortedMap<String, String>): String {
        if (sortedParamMap.isEmpty()) {
            return ""
        }

        val buffer = StringBuffer()
        val iterator = sortedParamMap.entries.iterator()

        while (iterator.hasNext()) {
            buffer.append(iterator.next().value)
        }
        return buffer.toString()
    }

    override fun run() {
        try {
            val httpHelper = AWTHttpRequestHelper(listener)

            //			for (String key : params.keySet())
            //			{
            //				AWTLog.d("sambow", "[params]-" + key + " | " + params.get(key));
            //			}

            val tResult = when (type) {
                AWTNetManager.ConnectType.GET -> httpHelper.performGet(host, params)
                AWTNetManager.ConnectType.POST -> httpHelper.performPost(host, params)
                AWTNetManager.ConnectType.POST_JSON -> httpHelper.performPostJson(host, jsonObject)
                else -> httpHelper.performPost(host, params, uriMap)
            }

            val message = listener.obtainMessage()
            val bundle = Bundle()
            bundle.putString(AWTNetListener.CODE, "200")
            bundle.putString(AWTNetListener.RESPONSE, tResult)
            message.data = bundle
            listener.sendMessage(message)
        } catch (e: Exception) {
            val message = listener.obtainMessage()
            val bundle = Bundle()
            bundle.putString(AWTNetListener.CODE, "1300")
            bundle.putString(AWTNetListener.RESPONSE, e.toString())
            message.data = bundle
            listener.sendMessage(message)
        } catch (error: OutOfMemoryError) {
            val message = listener.obtainMessage()
            val bundle = Bundle()
            bundle.putString(AWTNetListener.CODE, "1200")
            bundle.putString(AWTNetListener.RESPONSE, "Out Of Memory")
            message.data = bundle
            listener.sendMessage(message)
        }

    }
}
