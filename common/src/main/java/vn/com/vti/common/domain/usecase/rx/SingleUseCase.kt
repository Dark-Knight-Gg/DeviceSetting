package vn.com.vti.common.domain.usecase.rx

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import vn.com.vti.common.domain.callback.Callback

abstract class SingleUseCase<RESULT : Any, PARAMS> : RxUseCase<RESULT, PARAMS>() {

    private var callback: Callback<in RESULT>? = null

    abstract fun create(params: PARAMS): Single<out RESULT>

    override fun run(
        callback: Callback<in RESULT>,
        params: PARAMS,
    ): Disposable {
        this.callback = callback
        return create(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { callback.onStart() }
            .subscribe({
                callback.onNext(it)
                callback.onComplete()
            }, {
                callback.onError(it)
                callback.onComplete()
            })
            .also { addToDisposable(it) }
    }

    override fun cancel() {
        super.cancel()
        callback?.onComplete()
    }

    protected fun errorParamsSingle(message: String): Single<out RESULT> {
        return Single.error(IllegalArgumentException(javaClass.simpleName + " invalid params " + message))
    }

    protected fun errorParamsSingle(): Single<out RESULT> {
        return Single.error(IllegalArgumentException(javaClass.simpleName + " invalid params"))
    }
}

