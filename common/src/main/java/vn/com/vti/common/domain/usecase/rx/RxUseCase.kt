package vn.com.vti.common.domain.usecase.rx

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import vn.com.vti.common.domain.usecase.UseCase

abstract class RxUseCase<RESULT, PARAMS> : UseCase<Disposable, RESULT, PARAMS> {
    private var disposables: CompositeDisposable? = null

    protected fun addToDisposable(disposable: Disposable) {
        disposables ?: CompositeDisposable().also {
            disposables = it
        }.add(disposable)
    }

    override fun cancel() {
        disposables?.run {
            if (!isDisposed) dispose()
        }
        disposables = null
    }

    override fun isRunning(): Boolean {
        return disposables?.let {
            !it.isDisposed
        } ?: false
    }
}

fun <R : Any, P> SingleUseCase<R, P>.blockingGet(params: P): R =
    create(params = params).blockingGet()

fun <R : Any, P> SingleUseCase<R, P>.asyncGet(scope: CoroutineScope, params: P): Deferred<R> =
    scope.async {
        blockingGet(params)
    }

fun <R, P> MayBeUseCase<R, P>.blockingGet(params: P): R? =
    create(params = params).blockingGet()

fun <R, P> MayBeUseCase<R, P>.asyncGet(scope: CoroutineScope, params: P): Deferred<R?> =
    scope.async {
        blockingGet(params)
    }

fun CompletableUseCase<Unit>.blockingAwait(): Unit =
    create(Unit).blockingAwait()

fun <P> CompletableUseCase<P>.blockingAwait(params: P): Unit =
    create(params = params).blockingAwait()

fun <R, P> ObservableUseCase<R, P>.asFlow(coroutineScope: CoroutineScope, params: P): Flow<R> =
    flow {
        val consumer: (R) -> Unit = {
            coroutineScope.launch {
                emit(it)
            }
        }
        create(params).blockingForEach(consumer)
    }

fun <R, P> FlowableUseCase<R, P>.asFlow(coroutineScope: CoroutineScope, params: P): Flow<R> =
    flow {
        val consumer: (R) -> Unit = {
            coroutineScope.launch {
                emit(it)
            }
        }
        create(params).blockingForEach(consumer)
    }
