package vn.com.vti.common.util

import androidx.annotation.IntDef

@IntDef(
    HttpCode.OK,
    HttpCode.BAD_REQUEST,
    HttpCode.NOT_FOUND,
    HttpCode.SERVICE_UNAVAILABLE,
    HttpCode.UNAUTHORIZED,
    HttpCode.FORBIDDEN,
    HttpCode.REFRESH_TOKEN_FAILED,
    HttpCode.OTHER
)
@Retention(AnnotationRetention.SOURCE)
annotation class HttpCode {
    companion object {
        const val OK = 200
        const val BAD_REQUEST = 400
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val NOT_FOUND = 404
        const val SERVICE_UNAVAILABLE = 503
        const val REFRESH_TOKEN_FAILED = 499
        const val OTHER = 999
    }
}

@IntDef(
    ConnectivityCode.NO_NETWORK_CONNECTIONS,
    ConnectivityCode.CONNECT_TIME_OUT,
    ConnectivityCode.SOCKET_TIME_OUT,
    ConnectivityCode.UNABLE_TO_RESOLVE_HOST,
    ConnectivityCode.UNKNOWN
)
@Retention(AnnotationRetention.SOURCE)
annotation class ConnectivityCode {
    companion object {

        /**
         * Signals that device doesn't have any network connections.
         * Wifi, fixed networks, bluetooth,... are not available.
         * It may be turned off in settings and can be resolved by user
         */
        const val NO_NETWORK_CONNECTIONS = 0xe01

        /**
         * Signals that an error occurred while attempting to connect a socket to a remote address and port.
         * Typically, the connection was refused remotely
         * (e.g., no process is listening on the remote address/port).
         */
        const val CONNECT_TIME_OUT = 0xe02

        /**
         * Signals that a timeout has occurred on a socket read or accept.
         */
        const val SOCKET_TIME_OUT = 0xe03

        /**
         * Signals that host-name cannot be resolved
         */
        const val UNABLE_TO_RESOLVE_HOST = 0xe04

        /**
         * Just cannot detect issues
         */
        const val UNKNOWN = 0
    }
}