package com.appwellteam.library.model

import androidx.annotation.StringRes
import com.appwellteam.library.AWTApplication

/**
 *
 *
 * [.title]-[.value]物件，
 * 有[.dataMap]可以儲存多於資料，
 * 並會使用[.value]來當作[.equals]的主要判別值。
 *
 * Created by sambow on 2018/3/20.
 */

@Suppress("unused")
open class AWTItem {
    @Suppress("ProtectedInFinal")
    var title: String
        protected set
    val value: String

    /**
     * 其餘資料
     */
    private var dataMap: MutableMap<String, String> = HashMap()

    /**
     *
     * @param title [.title] 呈現值
     * @param value [.value] 主鍵值
     * */
    constructor(title: String, value: String)
    {
        this.title = title
        this.value = value
        if (this.value.isEmpty()) {
            throw AssertionError("value is null or empty string ")
        }
    }

    /**
     *
     * @param title [StringRes] to [.title]
     * @param value [StringRes] to [.value]
     */
    constructor(@StringRes title: Int, @StringRes value: Int) : this(AWTApplication.app?.getString(title) ?: "", AWTApplication.app?.getString(value) ?: "")


    fun setOtherData(key: String, value: String) {
        dataMap[key] = value
    }

    fun getOtherData(key: String): String? {
        return dataMap[key]
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return if (other != null && other is AWTItem) {
            this.value == other.value
        } else false
    }
}
