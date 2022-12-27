package vn.com.vti.common.domain.fetcher

import vn.com.vti.common.domain.callback.Callback
import vn.com.vti.common.domain.usecase.UseCase

interface UseCaseFetcher<WATCHER> {

    fun addCancelable(cancelable: WATCHER?)

    fun <RESULT, PARAMS> fetch(
        useCase: UseCase<WATCHER, RESULT, PARAMS>,
        callback: Callback<in RESULT>,
        params: PARAMS
    )

    fun <RESULT> fetch(
        useCase: UseCase<WATCHER, RESULT, Unit>,
        callback: Callback<in RESULT>
    )

    fun cancelAll()
}