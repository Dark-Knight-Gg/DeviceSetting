package vn.com.vti.common.network.interceptor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import okhttp3.Request
import vn.com.vti.common.network.PreRequestInterceptor
import vn.com.vti.common.network.exception.NoConnectivityException
import vn.com.vti.common.network.interceptor.ConnectivityMonitor.NetworkMonitor

class ConnectivityMonitor(context: Context) : PreRequestInterceptor {

    private val networkMonitor: NetworkMonitor by lazy {
        context.getSystemService<ConnectivityManager>()?.let {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> OreoMr1NetworkMonitor(it)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> MarshmallowNetworkMonitor(it)
                else -> LegacyNetworkMonitor(it)
            }
        } ?: NetworkMonitor { false }
    }

    override fun onPreRequestIntercept(origin: Request, builder: Request.Builder) {
        if (!networkMonitor.isConnected()) {
            throw NoConnectivityException()
        }
    }

    fun interface NetworkMonitor {

        fun isConnected(): Boolean
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private class MarshmallowNetworkMonitor(private val connectivityManager: ConnectivityManager) :
        NetworkMonitor {

        override fun isConnected(): Boolean {
            return connectivityManager.run {
                activeNetwork?.let {
                    getNetworkCapabilities(it)
                }
            }?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                        || hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                        || hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)
            } ?: false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    private class OreoMr1NetworkMonitor(private val connectivityManager: ConnectivityManager) :
        NetworkMonitor {

        override fun isConnected(): Boolean {
            return connectivityManager.run {
                activeNetwork?.let {
                    getNetworkCapabilities(it)
                }
            }?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                        || hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                        || hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)
                        || hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN)
            } ?: false
        }

    }

    @Suppress("DEPRECATION")
    private class LegacyNetworkMonitor(private val connectivityManager: ConnectivityManager) :
        NetworkMonitor {

        override fun isConnected(): Boolean {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }

    }
}