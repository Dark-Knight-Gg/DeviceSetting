package vn.com.vti.devicesetting.module.repository

import io.reactivex.rxjava3.core.Single
import vn.com.vti.devicesetting.model.BaseResponse
import vn.com.vti.devicesetting.model.pojo.authentication.LoginRequest

interface ClientRepository {
    fun login(loginRequest: LoginRequest): Single<BaseResponse<String>>
}