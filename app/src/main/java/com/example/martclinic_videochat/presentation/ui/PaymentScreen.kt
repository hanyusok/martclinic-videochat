package com.example.martclinic_videochat.presentation.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.net.URISyntaxException

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    url: String,
    onPaymentSuccess: (String) -> Unit,
    onPaymentFailure: (String) -> Unit,
    onClose: () -> Unit
) {
    // 뒤로가기 제어: 웹뷰 뒤로가기가 가능하면 뒤로가고, 아니면 화면 닫기
    var webView: WebView? = null
    
    BackHandler {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            onClose()
        }
    }

    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text("키움페이 결제") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onClose) {
                        androidx.compose.material3.Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "닫기"
                        )
                    }
                },
                actions = {
                    // For testing purpose: trigger success
                    androidx.compose.material3.TextButton(onClick = { onPaymentSuccess(url) }) {
                        androidx.compose.material3.Text("테스트: 결제 완료")
                    }
                }
            )
        }
    ) { paddingValues ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            factory = { context ->
                WebView(context).apply {
                    webView = this
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    
                    settings.apply {
                        javaScriptEnabled = true
                        javaScriptCanOpenWindowsAutomatically = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        cacheMode = WebSettings.LOAD_NO_CACHE
                    }

                    // Lollipop 이상 쿠키 및 혼합 콘텐츠 설정
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                    }

                    webViewClient = createPaymentWebViewClient(
                        context = context,
                        onSuccess = onPaymentSuccess,
                        onFailure = onPaymentFailure
                    )
                    webChromeClient = WebChromeClient()
                    
                    loadUrl(url)
                }
            }
        )
    }
}

private fun createPaymentWebViewClient(
    context: Context,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) = object : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        return handleUri(view, request.url.toString())
    }

    @Suppress("DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        return handleUri(view, url)
    }

    private fun handleUri(view: WebView, url: String): Boolean {
        if (url.isEmpty()) return false
        Log.d("PaymentWebView", "URL: $url")

        // 1. 성공/실패 콜백 처리 (사용자의 비즈니스 로직에 맞춰 Return URL 확인)
        // 실제 운영 환경에서는 PG사로부터 설정한 callback url을 체크해야 합니다.
        if (url.contains("payment_success") || url.contains("v1/payments/confirm")) { 
            onSuccess(url)
            return true
        } else if (url.contains("payment_fail") || url.contains("v1/payments/fail")) {
            onFailure(url)
            return true
        }

        // 2. HTTP/HTTPS 스킴은 웹뷰 내에서 이동
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return false
        }

        // 3. Intent 스킴 처리 (카드사 앱 호출 등)
        if (url.startsWith("intent://")) {
            try {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                val packageName = intent.`package`

                // 1) 앱이 설치되어 있으면 실행
                if (packageName != null && isAppInstalled(context, packageName)) {
                    sanitizeIntent(intent)
                    context.startActivity(intent)
                    return true
                }

                // 2) 브라우저 Fallback URL 확인
                val fallbackUrl = try {
                    intent.getStringExtra("browser_fallback_url")
                } catch (e: Exception) {
                    // Integer cannot be cast to String exception workaround
                    intent.extras?.get("browser_fallback_url")?.toString()
                }
                if (!fallbackUrl.isNullOrEmpty()) {
                    view.loadUrl(fallbackUrl)
                    return true
                }

                // 3) 마켓으로 이동
                if (packageName != null) {
                    val marketUri = Uri.parse("market://details?id=$packageName")
                    context.startActivity(Intent(Intent.ACTION_VIEW, marketUri))
                    return true
                }
            } catch (e: URISyntaxException) {
                Log.e("PaymentWebView", "URI Syntax Error", e)
            } catch (e: ActivityNotFoundException) {
                Log.e("PaymentWebView", "Activity Not Found", e)
            }
            return true
        }

        // 4. 기타 커스텀 스킴 처리 (visp://, bankpay://, ispmobile:// 등)
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("PaymentWebView", "Unhandled scheme: $url", e)
            false
        }
    }

    private fun sanitizeIntent(intent: Intent) {
        // Samsung SystemUI (GlobalActionsDialogLite) crash workaround
        // If certain extras are not Strings, convert them to prevent ClassCastException in system components
        val bundle = intent.extras ?: return
        val problematicKeys = listOf("browser_fallback_url", "status")
        for (key in problematicKeys) {
            try {
                if (bundle.containsKey(key)) {
                    val value = bundle.get(key)
                    if (value != null && value !is String) {
                        intent.putExtra(key, value.toString())
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
