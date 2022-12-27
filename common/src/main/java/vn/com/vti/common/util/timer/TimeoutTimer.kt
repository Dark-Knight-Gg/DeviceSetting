package vn.com.vti.common.util.timer

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * Created by VTI Android Team on 5/17/2018.
 * Copyright © 2018 VTI Inc. All rights reserved.
 */
abstract class TimeOutTimer(
    timeOut: Long,
    timeUnit: TimeUnit
) : Timer(timeOut, timeUnit) {
    override fun onCreateTimeCounter(
        timeOut: Long,
        timeUnit: TimeUnit
    ): Disposable? {
        val completable =
            Completable.timer(timeOut, timeUnit, AndroidSchedulers.mainThread())
        return completable.doOnSubscribe { notifyTimeoutStarted() }
            .subscribe { notifyTimeoutComplete() }
    }
}
