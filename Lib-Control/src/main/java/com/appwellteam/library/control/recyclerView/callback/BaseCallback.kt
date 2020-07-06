package com.appwellteam.library.control.recyclerView.callback

import com.appwellteam.library.model.AWTItem

/**
 * Created by sambow on 2018/4/15.
 */

@Suppress("unused")
interface BaseCallback<T : AWTItem> {
    fun onItemClick(item: T)
}
