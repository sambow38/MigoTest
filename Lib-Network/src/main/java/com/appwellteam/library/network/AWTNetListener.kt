package com.appwellteam.library.network

import android.os.Handler
import android.os.Message
import com.appwellteam.library.common.AWTLog

/**
 * Created by Sambow on 15/12/11.
 */
abstract class AWTNetListener : Handler() {
    private var manager: AWTNetManager? = null
    private var taskId: Int = 0

    fun setManager(manager: AWTNetManager, taskId: Int) {
        this.manager = manager
        this.taskId = taskId
    }

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        try {
            val bundle = msg.data
            val result = bundle.getString(RESPONSE)
            val code = bundle.getString(CODE)
            val codeInt = Integer.parseInt(code!!)
            if (codeInt == 200) {
                if (manager!!.isTaskAlive(taskId)) {

                    AWTLog.i("AWT-Network", "[onSuccess]-" + result!!)
                    onSuccess(result)
                } else {
                }
            } else {
                AWTLog.i("AWT-Network", "[onError]-" + result!!)
                onError(result, code, result)
            }
        } catch (e: Exception) {
            if (manager!!.isTaskAlive(taskId)) {
                AWTLog.i("AWT-Network", "[onError]-listener error")
                onError("", "1400", "listener error")
            } else {
            }
        } finally {
            manager!!.killTask(taskId)
        }
    }

    abstract fun onStart()
    abstract fun onSuccess(result: String?)
    abstract fun onError(result: String?, code: String, msg: String?)

    companion object {
        const val RESPONSE = "RESPONSE"
        const val CODE = "CODE"
    }
}
