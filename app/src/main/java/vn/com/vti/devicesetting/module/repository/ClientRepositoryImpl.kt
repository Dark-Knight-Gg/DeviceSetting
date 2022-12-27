package vn.com.vti.devicesetting.module.repository

import io.reactivex.rxjava3.core.Single
import vn.com.vti.devicesetting.model.BaseResponse
import vn.com.vti.devicesetting.model.pojo.authentication.LoginRequest
import vn.com.vti.devicesetting.module.restful.service.PrimaryApiService
import javax.inject.Inject


class ClientRepositoryImpl @Inject constructor(
    private val primaryApiService: PrimaryApiService
) : ClientRepository {
    override fun login(loginRequest: LoginRequest): Single<BaseResponse<String>> {
        return primaryApiService.login(loginRequest)
    }
}