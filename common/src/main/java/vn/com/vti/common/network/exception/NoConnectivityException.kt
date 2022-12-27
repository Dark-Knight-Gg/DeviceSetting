package vn.com.vti.common.network.exception

import java.io.IOException

class NoConnectivityException(message: String?) : IOException(message) {

    constructor() : this("No Internet Connection")
}