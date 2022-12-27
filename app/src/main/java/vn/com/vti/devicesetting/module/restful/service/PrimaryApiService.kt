@file:Suppress("unused")

package vn.com.vti.devicesetting.module.restful.service

import io.reactivex.rxjava3.core.Single
import retrofit2.http.*
import vn.com.vti.devicesetting.model.BaseResponse
import vn.com.vti.devicesetting.model.pojo.authentication.LoginRequest
import java.util.*

interface PrimaryApiService {
    @POST("/api/login")
    fun login(@Body request: LoginRequest): Single<BaseResponse<String>>
}

