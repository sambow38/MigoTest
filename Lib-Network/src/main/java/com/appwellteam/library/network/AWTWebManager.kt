package com.appwellteam.library.network

import com.appwellteam.library.common.AWTEventBus

/**
 * Created by Sambow on 15/12/13.
 */
@Suppress("unused")
abstract class AWTWebManager protected constructor(protected var nm: AWTNetManager) : AWTEventBus() {
    abstract fun onClear()
}
