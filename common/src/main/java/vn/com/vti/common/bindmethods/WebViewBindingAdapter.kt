package vn.com.vti.common.bindmethods

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.BindingAdapter

object WebViewBindingAdapter {

    @BindingAdapter("enableJavaScript")
    @JvmStatic
    fun enableJavaScript(view: WebView, enable: Boolean) {
        view.settings.javaScriptEnabled = enable
    }

    @BindingAdapter(value = ["webClient", "webChromeClient"], requireAll = false)
    @JvmStatic
    fun setupWebView(
        webView: WebView,
        client: WebViewClient?,
        chromeClient: WebChromeClient?
    ) {
        webView.apply {
            client?.let { webViewClient = it }
            chromeClient?.let { webChromeClient = it }
        }
    }

    @BindingAdapter("webUrl")
    @JvmStatic
    fun setWebViewUrl(webView: WebView, url: String?) = url?.let { webView.loadUrl(it) }
}