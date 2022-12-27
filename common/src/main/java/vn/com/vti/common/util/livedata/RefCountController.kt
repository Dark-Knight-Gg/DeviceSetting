package vn.com.vti.common.util.livedata

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

class RefCountController {

    private val refCount = AtomicInteger(0)

    private val stateOfRefCount = MutableStateFlow(0)

    val counter: StateFlow<Int> = stateOfRefCount.asStateFlow()

    fun increase() {
        stateOfRefCount.value = refCount.incrementAndGet()
    }

    fun decrease() {
        stateOfRefCount.value = synchronized(refCount) {
            refCount.decrementAndGet().let {
                if (it < 0) {
                    refCount.set(0)
                    0
                } else it
            }
        }
    }

    fun clearRefCount() {
        synchronized(refCount) {
            refCount.set(0)
            stateOfRefCount.value = 0
        }
    }
}

class BlockingTaskCountObserver(
    val name: String,
    val onBlocking: () -> Unit,
    val onUnblocking: () -> Unit
) : (Int) -> Unit {

    private val mLock = Any()
    private var lastCount: Int = 0

    override fun invoke(count: Int) {
        synchronized(mLock) {
            if (count != lastCount) {
                if (count == 0) {
                    Timber.d("$name::BlockingTaskCountObserver new=$count last=$lastCount -> onUnblocking")
                    onUnblocking()
                } else if (lastCount == 0 && count > 0) {
                    if (count == 1) onBlocking()
                    Timber.d("$name::BlockingTaskCountObserver new=$count last=$lastCount -> onBlocking")
                } else {
                    Timber.d("$name::BlockingTaskCountObserver new=$count last=$lastCount")
                }
                lastCount = count
            } else {
                Timber.d("$name::BlockingTaskCountObserver new=$count last=$lastCount")
            }
        }
    }

}