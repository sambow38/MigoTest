package com.appwellteam.library.network.new

import com.appwellteam.library.common.AWTEventBus

@Suppress("unused")
class AWTEventBaseModel(@Suppress("ProtectedInFinal") protected var eventMode: AWTEventBus.EventMode) {

    val isSuccess: Boolean
        get() = eventMode === AWTEventBus.EventMode.Success
}