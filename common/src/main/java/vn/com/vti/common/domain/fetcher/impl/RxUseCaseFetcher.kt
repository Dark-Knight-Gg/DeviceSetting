package vn.com.vti.common.domain.fetcher.impl

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import vn.com.vti.common.domain.callback.Callback
import vn.com.vti.common.domain.fetcher.UseCaseFetcher
import vn.com.vti.common.domain.usecase.UseCase
import javax.inject.Inject

class RxUseCaseFetcher @Inject constructor() : UseCaseFetcher<Disposable> {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun addCancelable(cancelable: Disposable?) {
        cancelable?.let {
            compositeDisposable.add(it)
        }
    }

    override fun <RESULT, PARAMS> fetch(
        useCase: UseCase<Disposable, RESULT, PARAMS>,
        callback: Callback<in RESULT>,
        params: PARAMS
    ) {
        useCase.run(callback, params).let {
            compositeDisposable.add(it)
        }
    }

    override fun <RESULT> fetch(
        useCase: UseCase<Disposable, RESULT, Unit>,
        callback: Callback<in RESULT>
    ) {
        useCase.run(callback, Unit).let {
            compositeDisposable.add(it)
        }
    }

    override fun cancelAll() {
        compositeDisposable.clear()
    }
}