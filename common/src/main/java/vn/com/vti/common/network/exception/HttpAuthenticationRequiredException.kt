package vn.com.vti.common.network.exception

import java.io.IOException


class HttpAuthenticationRequiredException(message: String?) : IOException(message) {

    constructor() : this("Api required authentication but authentication value is null or empty")

}