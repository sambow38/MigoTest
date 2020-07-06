package com.appwellteam.library.control.recyclerView.holder

import android.view.View
import android.view.ViewGroup

import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

import com.appwellteam.library.model.AWTItem

/**
 *
 * 簡便型ViewHolder，
 * 已跟BaseAdapter綁定互動，
 * 僅需在[.fillData]內完成邏輯即可。
 *
 * Created by sambow on 2018/3/20.
 * @see com.appwellteam.library.control.recyclerView.adapter.BaseAdapter
 *
 * @param <T> [AWTItem]
</T> */

abstract class BaseHolder<T : AWTItem>
/**
 *
 * 建構子，
 * 會使用 layout 這個參數來製作畫面。
 *
 * @param parent 上層容器
 * @param layout [LayoutRes]
 */
(parent: ViewGroup, @LayoutRes layout: Int) : RecyclerView.ViewHolder(View.inflate(parent.context, layout, null)) {

    @Suppress("MemberVisibilityCanBePrivate")
    protected lateinit var rowItem: T
        private set

    init {
        this.findViews(itemView)
    }

    internal fun setRowItem(rowItem: T)
    {
        this.rowItem = rowItem
    }

    /**
     *
     * 尋找畫面元件的方法，可在此處將需要的元件找出。
     *
     * @param view 最底層容器
     */
    protected abstract fun findViews(view: View)

    /**
     *
     * 可在此處將資料物件與畫面做結合。
     *
     * @param item [AWTItem] 資料物件
     */
    abstract fun fillData(item: T, position: Int)
}
