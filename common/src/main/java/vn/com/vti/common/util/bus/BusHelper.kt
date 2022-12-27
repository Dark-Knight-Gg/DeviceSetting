@file:Suppress("unused")

package vn.com.vti.common.util.bus

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.greenrobot.eventbus.EventBus

object BusHelper {
    fun getBus(): EventBus = EventBus.getDefault()
}

fun Any.sendToBus() {
    BusHelper.getBus().post(this)
}

fun Any.sendStickyToBus() {
    BusHelper.getBus().postSticky(this)
}

fun LifecycleOwner.subscribeBusEntireLifecycle(subscribe: Any): LifecycleObserver {
    return object : DefaultLifecycleObserver {

        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            BusHelper.getBus().register(subscribe)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            BusHelper.getBus().unregister(subscribe)
        }
    }.also {
        lifecycle.addObserver(it)
    }
}

fun LifecycleOwner.subscribeBusVisibleLifecycle(subscribe: Any): LifecycleObserver {
    return object : DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            BusHelper.getBus().register(subscribe)
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            BusHelper.getBus().unregister(subscribe)
        }
    }.also {
        lifecycle.addObserver(it)
    }
}

fun LifecycleOwner.subscribeBusForegroundLifecycle(subscribe: Any): LifecycleObserver {
    return object : DefaultLifecycleObserver {

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            BusHelper.getBus().register(subscribe)
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            BusHelper.getBus().unregister(subscribe)
        }

    }.also {
        lifecycle.addObserver(it)
    }
}

