package vn.com.vti.common.network

import retrofit2.HttpException
import timber.log.Timber
import vn.com.vti.common.serializer.Serializer

inline fun <reified FAILURE> Throwable.classifyResponseException(
    serializer: Serializer,
    onFailureResponse: (Int, FAILURE, Throwable) -> Throwable?,
    onParserError: ((Int, String, Throwable) -> Throwable?) = { _, _, _: Throwable -> null }
): Throwable? =
    (this as? HttpException)?.let { httpException ->
        httpException.response()?.errorBody()?.string()?.let { errorBody ->
            serializer
                .runCatching {
                    deserialize(errorBody, FAILURE::class.java)
                }.fold({ response: FAILURE ->
                    return onFailureResponse(httpException.code(), response, this)
                }, {
                    Timber.w(it, "Json parse error...")
                    return onParserError(httpException.code(), errorBody, this)
                })
        } ?: run {
            Timber.e("Http exception empty body!!!")
            null
        }
    }