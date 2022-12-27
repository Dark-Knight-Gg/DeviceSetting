package vn.com.vti.common.domain.usecase.rx

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import vn.com.vti.common.domain.callback.Callback

abstract class MayBeUseCase<RESULT, PARAMS> : RxUseCase<RESULT, PARAMS>() {

    abstract fun create(params: PARAMS): Maybe<out RESULT>

    override fun run(
        callback: Callback<in RESULT>,
        params: PARAMS
    ): Disposable {
        return create(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { callback.onStart() }
            .doOnDispose(callback::onComplete)
            .doOnTerminate {
                onUseCaseCompleted()
            }
            .subscribe(callback::onNext, {
                callback.onError(it)
                callback.onComplete()
            }, callback::onComplete)
            .also { addToDisposable(it) }
    }

    protected fun onUseCaseCompleted() {
        //intentionally blank
    }

    protected fun errorParamsObservable(message: String): Maybe<out RESULT> {
        return Maybe.error(IllegalArgumentException("${javaClass.simpleName} invalid params $message"))
    }

    protected fun errorParamsObservable(): Maybe<out RESULT> {
        return Maybe.error(IllegalArgumentException("${javaClass.simpleName} invalid params"))
    }
}