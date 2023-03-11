package com.arnyminerz.filmagentaproto.ui.components

import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

fun interface ErrorReceiver {
    operator fun invoke(view: WebView, request: WebResourceRequest, error: WebResourceError)
}

fun interface LoadReceiver {
    operator fun invoke(view: WebView, url: String)
}

fun interface LoadProgressReceiver {
    operator fun invoke(view: WebView, url: String, loaded: Boolean)
}

@Composable
fun WebView(
    url: String,
    modifier: Modifier = Modifier,
    loadProgressReceiver: LoadProgressReceiver? = null,
    errorReceiver: ErrorReceiver? = null,
    resourceLoadReceiver: LoadReceiver? = null,
    finishedReceiver: LoadReceiver? = null,
) {
    AndroidView(
        modifier = modifier,
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {
                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest,
                        error: WebResourceError,
                    ) { errorReceiver?.invoke(view, request, error) }

                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                        loadProgressReceiver?.invoke(view, url, false)
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        finishedReceiver?.invoke(view, url)
                        loadProgressReceiver?.invoke(view, url, true)
                    }

                    override fun onLoadResource(view: WebView, url: String) {
                        resourceLoadReceiver?.invoke(view, url)
                    }
                }
                loadUrl(url)
            }
        },
        update = {
            it.loadUrl(url)
        }
    )
}
