package com.example.martclinic_videochat.presentation.ui.components

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PortOneWebView(
    storeId: String,
    channelKey: String,
    onVerified: (String) -> Unit,
    onFailed: (String) -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.setSupportMultipleWindows(true)
                settings.javaScriptCanOpenWindowsAutomatically = true
                
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        url?.let {
                            if (it.startsWith("https://martclinic.com/verify-success")) {
                                val uri = Uri.parse(it)
                                val identityVerificationId = uri.getQueryParameter("identityVerificationId")
                                val code = uri.getQueryParameter("code")
                                val message = uri.getQueryParameter("message")
                                
                                if (code != null) {
                                    onFailed(message ?: "인증 실패")
                                } else if (identityVerificationId != null) {
                                    onVerified(identityVerificationId)
                                } else {
                                    val paymentId = uri.getQueryParameter("paymentId")
                                    if (paymentId != null) {
                                        onVerified(paymentId)
                                    } else {
                                        onFailed("인증 ID를 찾을 수 없습니다.")
                                    }
                                }
                                view?.stopLoading()
                            }
                        }
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        val url = request.url.toString()
                        
                        if (url.startsWith("https://martclinic.com/verify-success")) {
                            val uri = Uri.parse(url)
                            val identityVerificationId = uri.getQueryParameter("identityVerificationId")
                            val code = uri.getQueryParameter("code")
                            val message = uri.getQueryParameter("message")
                            
                            if (code != null) {
                                onFailed(message ?: "인증 실패")
                            } else if (identityVerificationId != null) {
                                onVerified(identityVerificationId)
                            } else {
                                val paymentId = uri.getQueryParameter("paymentId")
                                if (paymentId != null) {
                                    onVerified(paymentId)
                                } else {
                                    onFailed("인증 ID를 찾을 수 없습니다.")
                                }
                            }
                            return true
                        }

                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            try {
                                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                                val packageVal = intent.`package`
                                if (packageVal != null) {
                                    val pm = context.packageManager
                                    if (pm.getLaunchIntentForPackage(packageVal) != null) {
                                        context.startActivity(intent)
                                        return true
                                    }
                                }
                                val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                                if (fallbackUrl != null) {
                                    view.loadUrl(fallbackUrl)
                                    return true
                                }
                                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageVal"))
                                context.startActivity(marketIntent)
                                return true
                            } catch (e: Exception) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                    return true
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                }
                            }
                            return true
                        }
                        return false
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        consoleMessage?.let {
                            Log.d("PortOneWebView", "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}")
                        }
                        return true
                    }

                    override fun onCreateWindow(
                        view: WebView?,
                        isDialog: Boolean,
                        isUserGesture: Boolean,
                        resultMsg: Message?
                    ): Boolean {
                        val newWebView = WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                    val url = request.url.toString()
                                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                        try {
                                            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                                            val packageVal = intent.`package`
                                            if (packageVal != null) {
                                                val pm = context.packageManager
                                                if (pm.getLaunchIntentForPackage(packageVal) != null) {
                                                    context.startActivity(intent)
                                                    return true
                                                }
                                            }
                                            val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                                            if (fallbackUrl != null) {
                                                view.loadUrl(fallbackUrl)
                                                return true
                                            }
                                            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageVal"))
                                            context.startActivity(marketIntent)
                                            return true
                                        } catch (e: Exception) {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                                return true
                                            } catch (ex: Exception) {
                                                ex.printStackTrace()
                                            }
                                        }
                                        return true
                                    }
                                    return false
                                }
                            }
                        }

                        val dialog = Dialog(context, android.R.style.Theme_NoTitleBar_Fullscreen).apply {
                            setContentView(newWebView)
                            show()
                        }

                        newWebView.webChromeClient = object : WebChromeClient() {
                            override fun onCloseWindow(window: WebView?) {
                                dialog.dismiss()
                            }
                        }

                        val transport = resultMsg?.obj as? WebView.WebViewTransport
                        transport?.webView = newWebView
                        resultMsg?.sendToTarget()
                        return true
                    }
                }
                
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onSuccess(identityVerificationId: String) {
                        post {
                            onVerified(identityVerificationId)
                        }
                    }

                    @JavascriptInterface
                    fun onError(message: String) {
                        post {
                            onFailed(message)
                        }
                    }
                }, "Android")

                val html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <script src="https://cdn.portone.io/v2/browser-sdk.js"></script>
                        <style>
                            body { margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f5f5f5; font-family: sans-serif; }
                            .loader { border: 4px solid #f3f3f3; border-radius: 50%; border-top: 4px solid #3498db; width: 40px; height: 40px; animation: spin 1s linear infinite; }
                            @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
                        </style>
                    </head>
                    <body>
                        <div class="loader"></div>
                        <script>
                            window.onload = function() {
                                var verificationId = 'idv-' + Math.random().toString(36).substring(2, 11) + '-' + Date.now();
                                PortOne.requestIdentityVerification({
                                    storeId: "$storeId",
                                    identityVerificationId: verificationId,
                                    channelKey: "$channelKey",
                                    redirectUrl: "https://martclinic.com/verify-success"
                                }).then(function(response) {
                                    if (response && response.code != null) {
                                        Android.onError(response.message || '인증 실패');
                                    } else if (response && response.identityVerificationId) {
                                        Android.onSuccess(response.identityVerificationId);
                                    }
                                }).catch(function(error) {
                                    Android.onError(error.message || '알 수 없는 오류');
                                });
                            };
                        </script>
                    </body>
                    </html>
                """.trimIndent()

                loadDataWithBaseURL("https://martclinic.com", html, "text/html", "UTF-8", null)
            }
        }
    )
}
