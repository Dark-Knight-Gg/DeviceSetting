package vn.com.vti.common.util.timer

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

abstract class IntervalTimer(
    private val interval: Long,
    timeUnit: TimeUnit
) : Timer(1L, timeUnit) {
    override fun onCreateTimeCounter(
        timeOut: Long,
        timeUnit: TimeUnit
    ): Disposable? {
        return Observable.interval(
            interval,
            timeUnit,
            Schedulers.computation()
        )
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { notifyTimeoutStarted() }
            .subscribe { tick: Long ->
                notifyTimeTick(
                    tick
                )
            }
    }

    private fun notifyTimeTick(tick: Long) {
        onTimeTick(tick)
    }

    override fun onTimeoutCompleted(): Int {
        //Just apply forever until stopRecognize manually
        return NextAction.REPEAT
    }

    protected abstract fun onTimeTick(remain: Long)

}
