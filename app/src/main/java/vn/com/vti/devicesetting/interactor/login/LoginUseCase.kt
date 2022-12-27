package vn.com.vti.devicesetting.interactor.login

import io.reactivex.rxjava3.core.Single
import vn.com.vti.common.domain.usecase.rx.SingleUseCase
import vn.com.vti.devicesetting.model.pojo.authentication.LoginRequest
import vn.com.vti.devicesetting.module.repository.ClientRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor() : SingleUseCase<String, LoginRequest>() {
    @Inject
    lateinit var repository: ClientRepository

    override fun create(params: LoginRequest): Single<out String> {
        return repository.login(params)
            .map { it.result }
    }
}