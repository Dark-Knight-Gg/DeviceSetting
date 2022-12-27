package vn.com.vti.common.network.interceptor

import okhttp3.Request
import vn.com.vti.common.network.PreRequestInterceptor
import vn.com.vti.common.network.Retrofits
import vn.com.vti.common.network.exception.HttpAuthenticationRequiredException
import vn.com.vti.common.util.notNullOrEmptyLet

interface CookiesInterceptor : PreRequestInterceptor {

    override fun onPreRequestIntercept(origin: Request, builder: Request.Builder) {
        origin.headers[Retrofits.HEADER_AUTHENTICATION_MODE].let {
            when (it) {
                Retrofits.AUTH_MODE_NONE -> {
                    //intentionally blank
                }
                Retrofits.AUTH_MODE_REQUIRED -> {
                    val auth = provideAuthCredential()
                    val authKey = auth?.getAuthKey()
                    val authValue = auth?.buildAuthValue()
                    if (authKey.isNullOrEmpty() || authValue.isNullOrEmpty()) {
                        throw HttpAuthenticationRequiredException()
                    } else {
                        builder.header(authKey, authValue)
                    }
                }
                Retrofits.AUTH_MODE_REFRESHING -> {
                    provideRefreshCredential().let { auth ->
                        auth.buildAuthValue().notNullOrEmptyLet { authValue ->
                            builder.header(auth.getAuthKey(), authValue)
                        }
                    }
                }
                else -> {
                    provideAuthCredential()?.let { auth ->
                        auth.buildAuthValue().notNullOrEmptyLet { authValue ->
                            builder.header(auth.getAuthKey(), authValue)
                        }
                    }
                }
            }
            builder.removeHeader(Retrofits.HEADER_AUTHENTICATION_MODE)
        }
    }

    fun provideAuthCredential(): AuthCredential?

    fun provideRefreshCredential(): AuthCredential = NoAuth
}