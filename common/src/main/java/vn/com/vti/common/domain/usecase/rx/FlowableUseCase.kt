package vn.com.vti.common.domain.usecase.rx

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import vn.com.vti.common.domain.callback.Callback

abstract class FlowableUseCase<RESULT, PARAMS> : RxUseCase<RESULT, PARAMS>() {

    abstract fun create(params: PARAMS): Flowable<out RESULT>

    override fun run(
        callback: Callback<in RESULT>,
        params: PARAMS
    ): Disposable {
        return create(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { callback.onStart() }
            .doOnCancel(callback::onComplete)
            .subscribe(callback::onNext, {
                callback.onError(it)
                callback.onComplete()
            }, callback::onComplete)
            .also { addToDisposable(it) }
    }
}