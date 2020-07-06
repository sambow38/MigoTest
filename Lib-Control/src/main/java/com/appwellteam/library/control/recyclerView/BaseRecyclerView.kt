package com.appwellteam.library.control.recyclerView

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appwellteam.library.control.AWTView
import com.appwellteam.library.control.recyclerView.adapter.BaseAdapter
import com.appwellteam.library.control.recyclerView.holder.BaseHolder
import com.appwellteam.library.model.AWTItem

/**
 *
 *
 * 簡便型的RecyclerView，
 * 只需設定[.setAdapter]即可呈現。
 *
 * Created by sambow on 2018/4/8.
 */
@Suppress("unused")
class BaseRecyclerView : RecyclerView, AWTView {
    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        initialize(context)
    }

    override fun initialize(context: Context) {
        this.layoutManager = LinearLayoutManager(context, VERTICAL, false)
    }

    /**
     * 此方法已棄用，不再生效<br></br>
     * 請使用 [.setAdapter]
     *
     * @see .setAdapter
     */
    @Deprecated("此方法已棄用，不再生效")
    override fun setAdapter(adapter: Adapter<*>?) {
    }

    /**
     * 改寫原本的 [.setAdapter]
     * @param adapter [BaseAdapter]
     * @see BaseAdapter
     */
    fun <T : AWTItem, K : BaseHolder<T>>setAdapter(adapter: BaseAdapter<T, K>) {
        super.setAdapter(adapter)
    }
}
