package vn.com.vti.common.viewmodel.impl

import android.app.Application
import androidx.annotation.CallSuper
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber
import vn.com.vti.common.domain.callback.Callback
import vn.com.vti.common.domain.fetcher.UseCaseFetcher
import vn.com.vti.common.domain.usecase.UseCase
import vn.com.vti.common.network.exception.NoConnectivityException
import vn.com.vti.common.util.ConnectivityCode
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

open class BaseInteractorViewModel(application: Application) : BaseAndroidViewModel(application) {

    @Inject
    lateinit var fetcher: UseCaseFetcher<Disposable>

    fun <RESULT, PARAMS> fetch(
        useCase: UseCase<Disposable, RESULT, PARAMS>,
        callback: Callback<in RESULT>,
        params: PARAMS
    ) = fetcher.fetch(useCase, callback, params)

    fun <RESULT> fetch(
        useCase: UseCase<Disposable, RESULT, Unit>,
        callback: Callback<in RESULT>
    ) = fetcher.fetch(useCase, callback)

    fun addCancelable(cancelable: Disposable?) = fetcher.addCancelable(cancelable)

    @CallSuper
    open fun cancelAll() {
        fetcher.cancelAll()
        notifyAllTaskCleared()
    }

    override fun onCleared() {
        super.onCleared()
        cancelAll()
    }

    protected open fun onNetworkConnectFailed(@ConnectivityCode cause: Int, error: Throwable) {
    }

    protected abstract inner class BaseCallback<RESULT>(private val blocking: Boolean = false) :
        Callback<RESULT> {

        @CallSuper
        override fun onStart() {
            super.onStart()
            if (blocking) {
                notifyTaskStart()
            }
        }

        @CallSuper
        override fun onComplete() {
            super.onComplete()
            if (blocking) {
                notifyTaskFinish()
            }
        }

        override fun onError(error: Throwable) {
            Timber.e(
                error,
                "onError -> ${this.javaClass.simpleName} ${error.javaClass.simpleName} ${error.message}"
            )
        }
    }

    protected abstract inner class NetworkingCallback<RESULT>(blocking: Boolean = false) :
        BaseCallback<RESULT>(blocking) {

        @Suppress("MemberVisibilityCanBePrivate")
        protected fun shouldHandleNetworkConnection(): Boolean = true

        override fun onError(error: Throwable) {
            super.onError(error)
            if (error is IOException && shouldHandleNetworkConnection()) {
                onNetworkConnectFailed(error.resolveAsNetworkConnectivityIssue(), error)
            }
        }
    }
}

fun IOException.resolveAsNetworkConnectivityIssue(): Int = when (this) {
    is NoConnectivityException -> ConnectivityCode.NO_NETWORK_CONNECTIONS
    is ConnectException -> ConnectivityCode.CONNECT_TIME_OUT
    is SocketTimeoutException -> ConnectivityCode.SOCKET_TIME_OUT
    is UnknownHostException -> ConnectivityCode.UNABLE_TO_RESOLVE_HOST
    else -> ConnectivityCode.UNKNOWN
}