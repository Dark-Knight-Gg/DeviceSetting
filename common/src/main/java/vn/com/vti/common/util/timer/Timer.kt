package vn.com.vti.common.util.timer

import androidx.annotation.IntDef
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * Created by VTI Android Team on 5/17/2018.
 * Copyright © 2018 VTI Inc. All rights reserved.
 */
abstract class Timer(private val timeOut: Long, private val timeUnit: TimeUnit) {
    @IntDef(
        State.READY,
        State.RUNNING,
        State.INTERRUPTED,
        State.CANCELED,
        State.COMPLETED
    )
    @Retention(AnnotationRetention.SOURCE)
    internal annotation class State {
        companion object {
            const val READY = 0
            const val RUNNING = 1
            const val INTERRUPTED = 2
            const val CANCELED = 3
            const val COMPLETED = 4
        }
    }

    @IntDef(
        NextAction.PENDING,
        NextAction.REPEAT,
        NextAction.DISPOSE
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class NextAction {
        companion object {
            const val PENDING = 0
            const val REPEAT = 1
            const val DISPOSE = 2
        }
    }

    private var state: Int
    private var counterDisposable: Disposable? = null
    private var startedMillis: Long = 0
    private var interruptedMillis: Long = 0

    fun start(): Disposable? {
        if (state == State.READY || state == State.CANCELED) {
            counterDisposable?.let {
                if (!it.isDisposed) {
                    it.dispose()
                }
            }
            counterDisposable = onCreateTimeCounter(timeOut, timeUnit)
        }
        return counterDisposable
    }

    fun resume(): Disposable? {
        if (state != State.INTERRUPTED) {
            return counterDisposable
        }
        val timeOutInMillis = timeUnit.toMillis(timeOut)
        val now = System.currentTimeMillis()
        val timeDiff = now - startedMillis
        if (timeDiff >= timeOutInMillis) {
            notifyTimeoutComplete()
        } else {
            val remainingTimeMillis = timeOutInMillis - (interruptedMillis - startedMillis)
            val remainTimeOut =
                timeUnit.convert(remainingTimeMillis, TimeUnit.MILLISECONDS)
            counterDisposable = onCreateTimeCounter(remainTimeOut, timeUnit)
        }
        return counterDisposable
    }

    fun interrupt() {
        if (state == State.RUNNING) {
            state = State.INTERRUPTED
            interruptedMillis = System.currentTimeMillis()
            terminate()
        }
    }

    fun cancel() {
        if (state == State.COMPLETED) {
            return
        }
        terminate()
        state = State.CANCELED
    }

    private fun terminate() {
        counterDisposable?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
    }

    fun notifyTimeoutStarted() {
        startedMillis = System.currentTimeMillis()
        state = State.RUNNING
        onTimeoutStarted()
    }

    fun notifyTimeoutComplete() {
        when (onTimeoutCompleted()) {
            NextAction.PENDING -> {
                state = State.READY
                terminate()
            }
            NextAction.REPEAT -> {
                state = State.READY
                terminate()
                start()
            }
            NextAction.DISPOSE -> {
                state = State.COMPLETED
                terminate()
            }
            else -> {
                state = State.COMPLETED
                terminate()
            }
        }
    }

    protected abstract fun onCreateTimeCounter(
        timeOut: Long,
        timeUnit: TimeUnit
    ): Disposable?

    private fun onTimeoutStarted() = Unit

    @NextAction
    protected abstract fun onTimeoutCompleted(): Int

    init {
        state = State.READY
    }
}
