package vn.com.vti.common.network

import okhttp3.Request

fun interface PreRequestInterceptor {

    fun onPreRequestIntercept(origin: Request, builder: Request.Builder)
}