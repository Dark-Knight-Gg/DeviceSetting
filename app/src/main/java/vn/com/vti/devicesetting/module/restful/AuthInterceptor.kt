package vn.com.vti.devicesetting.module.restful

import vn.com.vti.common.network.interceptor.AuthCredential
import vn.com.vti.common.network.interceptor.AuthenticationInterceptor
import vn.com.vti.common.network.interceptor.BasicAuth
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : AuthenticationInterceptor {
    override fun provideAuthCredential(): AuthCredential? {
        return BasicAuth("")
    }

    override fun provideRefreshCredential(): AuthCredential? {
        return BasicAuth("")
    }
}