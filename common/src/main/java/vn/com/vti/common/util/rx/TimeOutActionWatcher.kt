package vn.com.vti.common.util.rx

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class TimeOutActionWatcher(
    private val timeOut: Long,
    private val timeUnit: TimeUnit,
    private val onTimeOut: () -> Unit
) {

    private val debouncePublisher = BehaviorSubject.createDefault(System.currentTimeMillis())

    private var disposable: Disposable? = null

    fun debounce() {
        debouncePublisher.onNext(System.currentTimeMillis())
    }

    fun start() {
        cancel()
        disposable = debouncePublisher.toFlowable(BackpressureStrategy.LATEST)
            .debounce(timeOut, timeUnit)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .take(1)
            .ignoreElements()
            .subscribe(onTimeOut)
    }

    fun cancel() {
        disposable?.run {
            if (!isDisposed) dispose()
        }
    }
}