package com.example.presentation.screens.stream.component

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.presentation.screens.stream.FloatingWindowService

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InternalWebView(
    url: String,
    modifier: Modifier,
    direction: Int,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                WebView.setWebContentsDebuggingEnabled(true)

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        Log.d("WEBVIEW", "Finished: $url")
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        Log.e(
                            "WEBVIEW",
                            "Error URL=${request?.url} code=${error?.errorCode} description=${error?.description}"
                        )
                    }
                }

                loadUrl(url)
            }
        }
    )
}