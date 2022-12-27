package vn.com.vti.common.util.rx

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Function
import java.util.concurrent.TimeUnit

class RetryWithDelay(
    private val maxRetryCount: Int,
    private val retryDelay: Int,
    private val timeUnit: TimeUnit,
) :
    Function<Flowable<out Throwable>, Flowable<*>> {
    private var retryCount = 0
    override fun apply(attempts: Flowable<out Throwable>): Flowable<*> {
        return attempts.flatMap {
            if (++retryCount < maxRetryCount) {
                return@flatMap Flowable.timer(retryDelay.toLong(), timeUnit)
            }
            return@flatMap Flowable.error(it)
        }
    }
}

fun Disposable?.tryToDispose() {
    this?.run {
        if (!isDisposed) dispose()
    }
}