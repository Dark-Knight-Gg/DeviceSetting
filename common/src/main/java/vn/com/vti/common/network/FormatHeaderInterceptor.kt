package vn.com.vti.common.network

import okhttp3.Request

class FormatHeaderInterceptor : PreRequestInterceptor {
    override fun onPreRequestIntercept(origin: Request, builder: Request.Builder) {
        val contentType = origin.header("Content-Type")
        val isNeedUpdateContentType = contentType == "application/json; charset=UTF-8"
        if (isNeedUpdateContentType) {
            builder.header("Content-Type", "application/json").build()
        }
    }
}