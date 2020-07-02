package com.example.migotest.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.appwellteam.library.common.AWTCommon
import com.appwellteam.library.control.recyclerView.BaseRecyclerView
import com.appwellteam.library.control.recyclerView.adapter.BaseAdapter
import com.appwellteam.library.control.recyclerView.holder.BaseHolder
import com.appwellteam.library.fragment.BaseFragment
import com.appwellteam.library.model.AWTItem
import com.example.migotest.R
import com.example.migotest.popup.PassDetailPopup
import com.example.migotest.popup.PassPopup
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Question2 : BaseFragment() {

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
        btnCreate.setOnClickListener {
            PassPopup(
                object :
                    PassPopup.OnPassCreated {
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

    private fun reloadData() {
        if (dataArray.count() <= 0) {
            tvEmpty.visibility = View.VISIBLE
            recycler.visibility = View.INVISIBLE
        } else {
            tvEmpty.visibility = View.INVISIBLE
            recycler.visibility = View.VISIBLE
            recycler.adapter?.notifyDataSetChanged()
        }
    }

    data class Pass(val createdAt: Long, val isDay: Boolean, val number: Int) :
        AWTItem(createdAt.toString(), createdAt.toString()) {
        private val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm")

        var isActive: Boolean = false
            private set
        var startAt: Long? = null
            private set
        var expireAt: Long? = null
            private set

        val serialNumber: String = AWTCommon.randomStr()

        val period: String
            get() {
                return if (isDay) {
                    if (number == 1) {
                        "$number day"
                    } else {
                        "$number days"
                    }
                } else {
                    if (number == 1) {
                        "$number hour"
                    } else {
                        "$number hours"
                    }
                }
            }

        val createdAtStr: String
            get() {
                return formatter.format(createdAt)
            }

        val startAtStr: String?
            get() {
                startAt?.let {
                    return formatter.format(it)
                }
                return null
            }

        val expireAtStr: String?
            get() {
                expireAt?.let {
                    return formatter.format(it)
                }
                return null
            }

        fun active() {
            val now = Date().time
            val scale = (if (isDay) 24 else 1) * 60 * 60 * 1000.toLong()
            var end = now + scale * number
            end -= end % scale

            isActive = true
            startAt = now
            expireAt = end
        }
    }

    class PassAdapter(items: ArrayList<Pass>) : BaseAdapter<Pass, PassHolder>(items) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PassHolder {
            return PassHolder(parent)
        }
    }

    class PassHolder(parent: ViewGroup) : BaseHolder<Pass>(parent, R.layout.holder_pass) {
        private lateinit var name: AppCompatTextView
        private lateinit var date: AppCompatTextView
        private lateinit var status: AppCompatTextView
        private lateinit var active: AppCompatButton

        override fun findViews(view: View) {
            name = view.findViewById(R.id.name)
            date = view.findViewById(R.id.date)
            status = view.findViewById(R.id.status)
            active = view.findViewById(R.id.active)
        }

        override fun fillData(item: Pass, position: Int) {
            updateViews(item)

            itemView.setOnClickListener {
                PassDetailPopup(
                    item,
                    object :
                        PassDetailPopup.OnDismiss {
                        override fun onDismiss() {
                            updateViews(item)
                        }
                    }
                ).show()
            }

            active.setOnClickListener {
                item.active()
                updateViews(item)
            }
        }

        private fun updateViews(item: Pass) {
            name.text = "PASS Card (${item.period})"
            if (item.isActive) {
                date.text =
                    "Period (${item.startAtStr} ~ ${item.expireAtStr})"
                status.text = "Activation"
                active.visibility = View.GONE
            } else {
                date.text = "created at (${item.createdAtStr})"
                status.text = "Inactivated"
                active.visibility = View.VISIBLE
            }
        }
    }
}