package com.appwellteam.library.common

import de.greenrobot.event.EventBus

/**
 * Created by Sambow on 15/11/29.
 */
open class AWTEventBus {
    var eventBus: EventBus
        protected set

    enum class EventMode {
        Success, Failed
    }

    init {
        eventBus = BaseEventBus()
    }

    private inner class BaseEventBus : EventBus() {
        override fun register(subscriber: Any) {
            if (!isRegistered(subscriber)) {
                super.register(subscriber)
            }
        }

        @Synchronized
        override fun unregister(subscriber: Any) {
            if (isRegistered(subscriber)) {
                super.unregister(subscriber)
            }
        }
    }
}
