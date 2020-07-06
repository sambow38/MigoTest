package com.appwellteam.library.common

import android.os.Build
import android.util.Log

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by Sambow on 15/12/13.
 */
@Suppress("unused")
object AWTLog {
    @Suppress("MemberVisibilityCanBePrivate")
    var canLog = true

    fun d(tag: String, vararg messages: Any) {
        if (canLog) {
            Log.d(tag, getString(*messages))
        }
    }

    fun i(tag: String, vararg messages: Any) {
        if (canLog) {
            Log.i(tag, getString(*messages))
        }
    }

    fun v(tag: String, vararg messages: Any) {
        if (canLog) {
            Log.v(tag, getString(*messages))
        }
    }

    fun e(tag: String, vararg messages: Any) {
        Log.e(tag, getString(*messages))
    }

    private fun getString(vararg messages: Any): String {
        val sb = StringBuilder("")
        for (msg in messages) {
            sb.append(msg)
            sb.append("\n")
        }
        return sb.toString()
    }

    fun json(tag: String, source: Any) {
        val obj = getJsonObjFromStr(source) ?: format(tag, source)
        try {
            when (obj) {
                is JSONObject -> format(tag, obj.toString(2))
                is JSONArray -> format(tag, obj.toString(2))
                else -> format(tag, source)
            }
        } catch (e: JSONException) {
            format(tag, source)
        }

    }

    private fun format(tag: String, source: Any) {
        val result = " $String "
        log(" ", getSplitter(25) + result + getSplitter(25))
        log(" ", "" + source)
        log(" ", getSplitter(50 + tag.length))
    }

    private fun log(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    private fun getSplitter(length: Int): String {
        val builder = StringBuilder()
        for (i in 0 until length) {
            builder.append("-")
        }
        return builder.toString()
    }

    private fun getJsonObjFromStr(test: Any): Any? {
        var obj: Any? = null
        try {
            obj = JSONObject(test.toString())
        } catch (ex: JSONException) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    obj = JSONArray(test)
                }
            } catch (ex1: JSONException) {
                return null
            }

        }

        return obj
    }
}
