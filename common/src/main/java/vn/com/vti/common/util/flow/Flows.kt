package vn.com.vti.common.util.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

fun <T> MutableSharedFlow<T>.emitOnScope(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    value: T
): Job = scope.launch {
    emit(value)
}

fun <T> MutableSharedFlow<T>.tryEmitOnScope(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    value: T
): Job = scope.launch {
    tryEmit(value)
}
