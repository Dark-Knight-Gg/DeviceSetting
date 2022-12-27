@file:Suppress("unused")

package vn.com.vti.common.viewmodel

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun <T> Flow<T>.collectOnLifecycle(
    lifecycleOwner: LifecycleOwner,
    repeatOnState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: FlowCollector<T>,
): Job = lifecycleOwner.run {
    lifecycleScope.launch {
        repeatOnLifecycle(repeatOnState) {
            collect(collector)
        }
    }
}

fun <T> Flow<T>.collectLatestOnLifecycle(
    lifecycleOwner: LifecycleOwner,
    repeatOnState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend (T) -> Unit,
): Job = lifecycleOwner.run {
    lifecycleScope.launch {
        repeatOnLifecycle(repeatOnState) {
            collectLatest(collector)
        }
    }
}

fun LifecycleOwner.repeatOnLifecycleScope(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    launcher: suspend CoroutineScope.() -> Unit,
): Job = lifecycleScope.launch {
    repeatOnLifecycle(state, launcher)
}

inline fun LifecycleOwner.repeatOnLifecycleWithDisposableEffect(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline disposable: () -> Unit = { },
    noinline launcher: suspend CoroutineScope.() -> Unit,
): Job = lifecycleScope.launch {
    launch { repeatOnLifecycle(state, launcher) }
    when (state) {
        Lifecycle.State.CREATED -> object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                disposable()
            }
        }
        Lifecycle.State.STARTED -> object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                disposable()
            }
        }
        Lifecycle.State.RESUMED -> object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                disposable()
            }
        }
        else -> null
    }?.let {
        launch { repeatOnLifecycle(state) { lifecycle.addObserver(it) } }
    }
}

fun OnBackPressedCallback.activeAtState(
    state: Lifecycle.State = Lifecycle.State.RESUMED,
    dispatcher: OnBackPressedDispatcher,
    owner: LifecycleOwner,
) {
    owner.repeatOnLifecycleWithDisposableEffect(state, disposable = {
        remove()
    }) {
        dispatcher.addCallback(owner, this@activeAtState)
    }
}