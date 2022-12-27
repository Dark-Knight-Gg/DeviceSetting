package vn.com.vti.devicesetting.base.viewmodel

import android.app.Application
import io.reactivex.rxjava3.disposables.Disposable
import vn.com.vti.common.annotation.LoadingType
import vn.com.vti.common.domain.usecase.UseCase
import vn.com.vti.common.ui.list.IListController
import vn.com.vti.common.viewmodel.impl.BaseInteractorViewModel

open class FaceXpressViewModel(application: Application) : BaseInteractorViewModel(application) {

    protected abstract inner class ApiCallback<RESULT>(blocking: Boolean = false) :
        NetworkingCallback<RESULT>(blocking) {
    }

    protected inline fun <R, P> fetch(
        useCase: UseCase<Disposable, R, P>,
        params: P,
        blocking: Boolean = false,
        crossinline foldSuccess: (R) -> Unit,
        crossinline foldError: (Throwable) -> Boolean = { false },
    ) {
        fetch(useCase, object : ApiCallback<R>(blocking) {
            override fun onNext(result: R) {
                foldSuccess(result)
            }

            override fun onError(error: Throwable) {
                if (foldError(error)) {
                    return
                }
                super.onError(error)
            }

        }, params)
    }

    protected inline fun <R, P> fetch(
        useCaseWithController: Pair<UseCase<Disposable, R, P>, IListController>,
        params: P,
        crossinline foldSuccess: (R) -> Unit,
        crossinline foldError: (Throwable) -> Boolean = { false },
    ) {
        val (useCase, controller) = useCaseWithController
        fetch(useCase, object : ApiCallback<R>(false) {

            override fun onStart() {
                super.onStart()
                controller.notifyLoaderStarted(LoadingType.FREE)
            }

            override fun onNext(result: R) {
                foldSuccess(result)
            }

            override fun onError(error: Throwable) {
                if (foldError(error)) {
                    return
                }
                super.onError(error)
            }

            override fun onComplete() {
                super.onComplete()
                controller.notifyLoaderFinished(LoadingType.FREE)
            }

        }, params)
    }

    protected inline fun <R> fetch(
        useCase: UseCase<Disposable, R, Unit>,
        blocking: Boolean = false,
        crossinline foldSuccess: (R) -> Unit,
        crossinline foldError: (Throwable) -> Boolean = { false },
    ) {
        fetch(useCase, Unit, blocking, foldSuccess, foldError)
    }

    protected inline fun <R> fetch(
        useCaseWithController: Pair<UseCase<Disposable, R, Unit>, IListController>,
        crossinline foldSuccess: (R) -> Unit,
        crossinline foldError: (Throwable) -> Boolean = { false },
    ) {
        fetch(useCaseWithController, Unit, foldSuccess, foldError)
    }

    protected inline fun <R, P> fetchBaseCallback(
        useCase: UseCase<Disposable, R, P>,
        params: P,
        blocking: Boolean = false,
        crossinline foldSuccess: (R) -> Unit,
        crossinline foldError: (Throwable) -> Boolean = { false },
    ) {
        fetch(useCase, object : BaseCallback<R>(blocking) {
            override fun onNext(result: R) {
                foldSuccess(result)
            }

            override fun onError(error: Throwable) {
                if (foldError(error)) {
                    return
                }
                super.onError(error)
            }

        }, params)
    }
}