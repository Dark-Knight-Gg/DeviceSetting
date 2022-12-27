@file:Suppress("unused")

package vn.com.vti.common.network.interceptor

import okhttp3.Request
import vn.com.vti.common.network.PreRequestInterceptor
import vn.com.vti.common.network.Retrofits
import vn.com.vti.common.network.Retrofits.AUTH_MODE_NONE
import vn.com.vti.common.network.Retrofits.AUTH_MODE_REFRESHING
import vn.com.vti.common.network.Retrofits.AUTH_MODE_REQUIRED
import vn.com.vti.common.network.exception.HttpAuthenticationRequiredException
import vn.com.vti.common.util.notNullOrEmptyLet

interface AuthenticationInterceptor : PreRequestInterceptor {

    override fun onPreRequestIntercept(origin: Request, builder: Request.Builder) {
        origin.headers[Retrofits.HEADER_AUTHENTICATION_MODE].let {
            when (it) {
                AUTH_MODE_NONE -> {
                    //intentionally blank
                }
                AUTH_MODE_REQUIRED -> {
                    val auth = provideAuthCredential()
                    val authKey = auth?.getAuthKey()
                    val authValue = auth?.buildAuthValue()
                    if (authKey.isNullOrEmpty() || authValue.isNullOrEmpty()) {
                        throw HttpAuthenticationRequiredException()
                    } else {
                        builder.header(authKey, authValue)
                    }
                }
                AUTH_MODE_REFRESHING -> {
                    provideRefreshCredential()?.let { auth ->
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

    fun provideRefreshCredential(): AuthCredential?
}

const val HTTP_AUTHORIZATION = "Authorization"

const val X_API_AUTHORIZATION = "X-API-Key"

interface AuthCredential {

    fun getAuthKey(): String

    fun buildAuthValue(): String?
}

object NoAuth : AuthCredential {

    override fun getAuthKey() = ""

    override fun buildAuthValue(): String? = null

}

class BasicAuth(private val token: String) : AuthCredential {

    override fun getAuthKey() = HTTP_AUTHORIZATION

    override fun buildAuthValue() = "Basic $token"
}

class BearerAuth(token: String) : AuthCredential {

    private val authValue = "Bearer $token"

    override fun getAuthKey() = HTTP_AUTHORIZATION

    override fun buildAuthValue() = authValue
}

class HeaderApiKeyAuth(private val token: String) : AuthCredential {

    override fun getAuthKey() = X_API_AUTHORIZATION

    override fun buildAuthValue() = token
}

class DigestAuth(
    username: String, realm: String,
    nonce: Long, uri: String,
    response: String,
) : AuthCredential {

    private val authValue =
        "Digest username=\"$username\" Realm=\"$realm\" nonce=\"$nonce\" uri=\"$uri\" response=\"$response\""

    override fun getAuthKey() = HTTP_AUTHORIZATION

    override fun buildAuthValue() = authValue
}

class OAuth2Auth(token: String) : AuthCredential {

    private val authValue = "Bearer $token"

    override fun getAuthKey() = HTTP_AUTHORIZATION

    override fun buildAuthValue() = authValue
}

class HawkAuth(
    id: String, ts: String,
    nonce: Long, mac: String,
) : AuthCredential {

    private val authValue =
        "Hawk id=\"$id\" ts=\"$ts\" nonce=\"$nonce\" mac=\"$mac\""

    override fun getAuthKey() = HTTP_AUTHORIZATION

    override fun buildAuthValue() = authValue
}

class AwsSignatureAuth(
    encryption: String = "AWS4-HMAC-SHA256",
    credential: String,
    signedHeader: String = "host;x-amz-date",
    signature: String,
) : AuthCredential {

    private val authValue =
        "$encryption Credential=\"$credential\" SignedHeaders=\"$signedHeader\" Signature=\"$signature\""

    override fun getAuthKey() = HTTP_AUTHORIZATION

    override fun buildAuthValue() = authValue
}