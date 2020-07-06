package com.appwellteam.library.common

import android.content.Context
import android.content.SharedPreferences
import com.appwellteam.library.AWTApplication
import java.util.*

/**
 * Created by Sambow on 15/10/12.
 */
@Suppress("unused")
object AWTStorage {
    private val prefHashMap = HashMap<String, SharedPreferences>()

    private fun getPref(file: String, context: Context?): SharedPreferences {
        val preferences = prefHashMap[file]
        return if (preferences == null)
        {
            val pref = (context ?: AWTApplication.app)?.getSharedPreferences(file, Context.MODE_PRIVATE) ?: throw RuntimeException("context is null")
            prefHashMap[file] = pref
            pref
        }
        else
        {
            preferences
        }
    }

    @JvmOverloads fun putString(key: String, file: String, value: String, context: Context? = null): Boolean {
        val ed = getPref(file, context).edit()
        ed.putString(key, value)
        return ed.commit()
    }

    @JvmOverloads fun putLong(key: String, file: String, value: Long, context: Context? = null): Boolean {
        val ed = getPref(file, context).edit()
        ed.putLong(key, value)
        return ed.commit()
    }

    @JvmOverloads fun putInt(key: String, file: String, value: Int, context: Context? = null): Boolean {
        val ed = getPref(file, context).edit()
        ed.putInt(key, value)
        return ed.commit()
    }

    @JvmOverloads fun putBoolean(key: String, file: String, value: Boolean, context: Context? = null): Boolean {
        val ed = getPref(file, context).edit()
        ed.putBoolean(key, value)
        return ed.commit()
    }

    @JvmOverloads fun getString(key: String, file: String, defValue: String?, context: Context? = null): String? {
        return getPref(file, context).getString(key, defValue)
    }

    @JvmOverloads fun getLong(key: String, file: String, defValue: Long, context: Context? = null): Long {
        return getPref(file, context).getLong(key, defValue)
    }

    @JvmOverloads fun getInt(key: String, file: String, defValue: Int, context: Context? = null): Int {
        return getPref(file, context).getInt(key, defValue)
    }

    @JvmOverloads fun getBoolean(key: String, file: String, defValue: Boolean, context: Context? = null): Boolean {
        return getPref(file, context).getBoolean(key, defValue)
    }

    @JvmOverloads fun clearAll(file: String, context: Context? = null): Boolean {
        return getPref(file, context).edit().clear().commit()
    }
}