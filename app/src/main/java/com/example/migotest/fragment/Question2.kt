package com.example.migotest.fragment

import android.graphics.Color
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.appwellteam.library.common.AWTCommon
import com.appwellteam.library.control.recyclerView.BaseRecyclerView
import com.appwellteam.library.control.recyclerView.adapter.BaseAdapter
import com.appwellteam.library.control.recyclerView.holder.BaseHolder
import com.appwellteam.library.fragment.BaseFragment
import com.appwellteam.library.model.AWTItem
import com.example.migotest.R
import com.example.migotest.popup.PassPopup
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class Question2: BaseFragment() {

    private lateinit var btnCreate: AppCompatButton
    private lateinit var recycler: BaseRecyclerView
    private lateinit var tvEmpty: AppCompatTextView

    private val dataArray = ArrayList<Pass>()

    override val mainLayout: Int
        get() = R.layout.fragment_q2

    override fun findViews(view: View) {
        btnCreate = view.findViewById(R.id.create)
        recycler = view.findViewById(R.id.recycler)
        tvEmpty = view.findViewById(R.id.empty)
    }

    override fun initialize(view: View, savedInstanceState: Bundle?) {
        view.setBackgroundColor(Color.RED)
        btnCreate.setOnClickListener {
            PassPopup(
                object :
                PassPopup.OnPassCreated{
                    override fun onCreated(isDay: Boolean, number: Int) {
                        dataArray.add(Pass(Date().time, isDay, number))
                        reloadData()
                    }
                }
            ).show()
        }

        recycler.setAdapter(PassAdapter(dataArray))
        reloadData()
    }

    override fun getTitle(inflater: LayoutInflater): View? {
        return null
    }

    private fun reloadData()
    {
        if (dataArray.count() <= 0)
        {
            tvEmpty.visibility = View.VISIBLE
            recycler.visibility = View.INVISIBLE
        }
        else
        {
            tvEmpty.visibility = View.INVISIBLE
            recycler.visibility = View.VISIBLE
            recycler.adapter?.notifyDataSetChanged()
        }
    }

    data class Pass(val createdAt: Long, val isDay: Boolean, val number: Int): AWTItem(createdAt.toString(), createdAt.toString())
    {
        var isActive: Boolean = false
        var startAt: Long? = null
        var expiredAt: Long? = null
        val serialNumber: String = AWTCommon.randomStr()
    }

    class PassAdapter(items: ArrayList<Pass>) : BaseAdapter<Pass, PassHolder>(items)
    {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PassHolder {
            return PassHolder(parent)
        }
    }

    class PassHolder(parent: ViewGroup): BaseHolder<Pass>(parent, R.layout.holder_pass)
    {
        private lateinit var serial: AppCompatTextView
        private lateinit var createdAt: AppCompatTextView
        private lateinit var status: AppCompatTextView
        private lateinit var statusPeriod: AppCompatTextView
        private lateinit var active: AppCompatButton

        private val formatter = SimpleDateFormat("yyyy/MM/dd hh:mm")
        override fun findViews(view: View) {
            serial = view.findViewById(R.id.serial)
            createdAt = view.findViewById(R.id.created_at)
            status = view.findViewById(R.id.status)
            statusPeriod = view.findViewById(R.id.status_period)
            active = view.findViewById(R.id.active)
        }

        override fun fillData(item: Pass, position: Int) {
            updateViews(item)
            active.setOnClickListener {
                val now = Date().time
                val scale = (if (item.isDay) 24 else 1) * 60 * 60 * 1000.toLong()
                var end = now + scale
                end -= end % scale

                item.isActive = true
                item.startAt = now
                item.expiredAt = end

                updateViews(item)
            }
        }

        private fun updateViews(item: Pass)
        {
            createdAt.text = "created at (${formatter.format(item.createdAt)})"
            serial.text = item.serialNumber
            if (item.isActive)
            {
                status.text = "Activation"
                statusPeriod.text = "(${formatter.format(item.startAt)} ~ ${formatter.format(item.expiredAt)})"
                active.visibility = View.INVISIBLE
            }
            else
            {
                status.text = "Inactivated"
                statusPeriod.text = ""
                active.visibility = View.VISIBLE
            }
        }
    }
}