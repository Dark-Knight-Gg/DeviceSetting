@file:Suppress("MemberVisibilityCanBePrivate")

package vn.com.vti.common.network

import android.app.Application
import android.net.Uri
import androidx.annotation.IntRange
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import vn.com.vti.common.CommonApplication
import vn.com.vti.common.appInstance
import vn.com.vti.common.util.notNullOrEmptyLet
import java.util.concurrent.TimeUnit

object Retrofits {

    const val HEADER_AUTHENTICATION_MODE = "Authentication-Mode"
    const val AUTH_MODE_NONE = "none"
    const val AUTH_MODE_REQUIRED = "required"
    const val AUTH_MODE_REFRESHING = "refreshing"

    const val HEADER_AUTH_NONE = "$HEADER_AUTHENTICATION_MODE:$AUTH_MODE_NONE"
    const val HEADER_AUTH_REQUIRED = "$HEADER_AUTHENTICATION_MODE:$AUTH_MODE_REQUIRED"
    const val HEADER_AUTH_REFRESHING = "$HEADER_AUTHENTICATION_MODE:$AUTH_MODE_REFRESHING"

    fun defaultOkHttpClient(
        logLevel: HttpLoggingInterceptor.Level? = null,
        preRequestInterceptor: Collection<PreRequestInterceptor>? = null,
        externalInterceptor: Collection<Interceptor>? = null,
    ): OkHttpClient {

        return OkHttpClient.Builder().apply {
            preRequestInterceptor.notNullOrEmptyLet {
                interceptors().add(PreRequestInterceptDispatcher(it))
            }
            externalInterceptor.notNullOrEmptyLet {
                interceptors().addAll(it)
            }
            appInstance().let {
                this.cache(defaultCache(it))
                    .interceptors().add(defaultLogger(it, logLevel))
            }
            addNetworkInterceptor(PreRequestInterceptDispatcher(listOf(FormatHeaderInterceptor())))
            readTimeout(30, TimeUnit.SECONDS)
            connectTimeout(30, TimeUnit.SECONDS)
        }.build()
    }

    fun newClient(
        domain: String,
        converterFactory: Converter.Factory = defaultGsonConverter(),
        callAdapterFactory: Collection<CallAdapter.Factory> = defaultCallFactory(),
        preRequestInterceptor: Collection<PreRequestInterceptor>? = null,
        externalInterceptor: Collection<Interceptor>? = null,
        logLevel: HttpLoggingInterceptor.Level? = null
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(domain)
            .addConverterFactory(converterFactory)
            .apply {
                callAdapterFactory.forEach {
                    this.addCallAdapterFactory(it)
                }
            }
            .client(defaultOkHttpClient(logLevel, preRequestInterceptor, externalInterceptor))
            .build()
    }

    fun defaultGsonConverter(): GsonConverterFactory =
        GsonConverterFactory.create(GsonBuilder().excludeFieldsWithoutExposeAnnotation().create())

    fun defaultCallFactory() = listOf<CallAdapter.Factory>(RxJava3CallAdapterFactory.create())

    fun defaultCache(
        application: Application,
        @IntRange(from = 1024 * 1024) size: Long = 10 * 1024 * 1024
    ) = Cache(application.cacheDir, size)

    fun defaultLogger(
        application: CommonApplication,
        logLevel: HttpLoggingInterceptor.Level? = null
    ): Interceptor =
        HttpLoggingInterceptor(logger = {
            val length = it.length
            if (length > 51200 /*50 kB*/)
                Platform.get().log(
                    level = Platform.WARN,
                    message = "Log omitted cause tldr; $length in bytes"
                )
            else Platform.get().log(Uri.decode(it))
        }).apply {
            level = logLevel ?: if (application.isDebugMode()) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
}