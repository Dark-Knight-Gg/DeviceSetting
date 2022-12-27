package vn.com.vti.common.domain.callback

import timber.log.Timber

fun interface Callback<RESULT> {

    fun onStart() {}

    fun onNext(result: RESULT)

    fun onError(error: Throwable) {
        Timber.e(error)
    }

    fun onComplete() {}
}