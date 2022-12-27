@file:Suppress("unused")

package vn.com.vti.common.domain.usecase.rx

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import vn.com.vti.common.domain.callback.Callback

abstract class CompletableUseCase<PARAMS> : RxUseCase<Unit, PARAMS>() {

    abstract fun create(params: PARAMS): Completable

    override fun run(
        callback: Callback<in Unit>,
        params: PARAMS
    ): Disposable {
        return create(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                callback.onStart()
            }
            .doOnDispose(callback::onComplete)
            .subscribe({
                callback.onNext(Unit)
                callback.onComplete()
            }, {
                callback.onError(it)
                callback.onComplete()
            }).also { addToDisposable(it) }
    }

    protected fun errorParamsCompletable(): Completable {
        return Completable.error(IllegalArgumentException(javaClass.simpleName + " invalid params"))
    }

    protected fun errorParamsCompletable(message: String): Completable {
        return Completable.error(IllegalArgumentException(message))
    }
}