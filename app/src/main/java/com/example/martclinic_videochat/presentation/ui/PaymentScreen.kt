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

import com.example.martclinic_videochat.util.KiwoomPayUtil

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    appointmentId: String,
    amount: Int,
    onPaymentSuccess: (String) -> Unit,
    onPaymentFailure: (String) -> Unit,
    onClose: () -> Unit
) {
    var webView: WebView? by remember { mutableStateOf(null) }
    var paymentHtml by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    BackHandler {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            onClose()
        }
    }

    LaunchedEffect(appointmentId) {
        val safeApptId = appointmentId.replace("-", "").take(15)
        val orderNo = "ORD_${safeApptId}_${System.currentTimeMillis().toString().takeLast(6)}"
        
        val hash = KiwoomPayUtil.fetchKiwoomEnc(orderNo, amount)
        if (hash != null) {
            paymentHtml = KiwoomPayUtil.buildPaymentHtml(amount, orderNo, hash)
        } else {
            errorMessage = "결제 준비 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
        }
    }

    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text("진료비 결제") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onClose) {
                        androidx.compose.material3.Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "닫기"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (errorMessage != null) {
                androidx.compose.material3.Text(errorMessage!!)
            } else if (paymentHtml == null) {
                CircularProgressIndicator()
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
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
                            
                            loadDataWithBaseURL("https://api.kiwoompay.co.kr", paymentHtml!!, "text/html", "UTF-8", null)
                        }
                    }
                )
            }
        }
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
            android.widget.Toast.makeText(context, "결제가 취소되거나 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show()
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
