package com.appwellteam.library.control.recyclerView.adapter

import com.appwellteam.library.model.AWTItem

import java.util.ArrayList
import androidx.recyclerview.widget.RecyclerView
import com.appwellteam.library.control.recyclerView.holder.BaseHolder

/**
 *
 *
 * BaseRecyclerView的基本adapter，
 * 需要指定泛型[BaseHolder]以及[AWTItem]，
 * 若需多種類也可自行複寫[.getItemViewType]，
 * 並於[.onBindViewHolder]的方法做區別。
 *
 * Created by sambow on 2018/3/20.
 *
 * @param <K> [BaseHolder]
 * @param <T> [AWTItem]
</T></K> */
@Suppress("unused")
abstract class BaseAdapter<T : AWTItem, K : BaseHolder<T>>(
    @Suppress("MemberVisibilityCanBePrivate")
    protected var items: ArrayList<T>
) : RecyclerView.Adapter<K>() {

//    constructor()

    override fun onBindViewHolder(holder: K, position: Int) {
        val item = items[position]
        holder.setRowItem(item)
        holder.fillData(item, position)
    }

    override fun getItemCount(): Int {
        return this.items.size
    }

    fun appendItem(item: T) {
        items.add(item)
    }

    fun insertFirst(item: T) {
        insert(item, 0)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun insert(item: T, index: Int) {
        if (index < items.size) {
            items.add(index, item)
        } else {
            items.add(item)
        }
    }

    fun getItem(index: Int): T? {
        return if (items.size > index) {
            items[index]
        } else {
            null
        }
    }

    fun getItems(): List<T> {
        return items
    }
}
