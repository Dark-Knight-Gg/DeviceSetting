package vn.com.vti.common.network

import okhttp3.Interceptor

internal class PreRequestInterceptDispatcher(private val interceptors: Collection<PreRequestInterceptor>) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain) = chain.run {
        val origin = chain.request()
        origin.newBuilder().apply {
            interceptors.forEach {
                it.onPreRequestIntercept(origin, this)
            }
        }.let {
            proceed(it.build())
        }
    }
}