package com.appwellteam.library.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appwellteam.library.AWTApplication

/**
 * Created by Sambow on 15/9/19.
 */
@Suppress("unused")
abstract class MenuBaseFragment : BaseFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var _adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>

    private var panelTitle: LinearLayout? = null
    private var panelFooter: LinearLayout? = null

    //region Override
    override val mainLayout: Int
        get() = R.layout.awt_fragment_menu
    //endregion

    protected abstract val adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>

    protected abstract val titleView: View?
    protected abstract val footerView: View?

    override fun findViews(view: View) {
        panelTitle = view.findViewById(R.id.menu_title_panel)
        panelFooter = view.findViewById(R.id.menu_footer_panel)
        recyclerView = view.findViewById(R.id.menu_list)
    }

    override fun initialize(view: View, savedInstanceState: Bundle?) {
        val tTitleView = titleView
        if (tTitleView != null) {
            panelTitle!!.addView(tTitleView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        val tFooterView = footerView
        if (tFooterView != null) {
            panelFooter!!.addView(tFooterView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        _adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(AWTApplication.app)
        recyclerView.adapter = _adapter
        recyclerView.itemAnimator = DefaultItemAnimator()
    }

    override fun getTitle(inflater: LayoutInflater): View? {
        return null
    }

}
