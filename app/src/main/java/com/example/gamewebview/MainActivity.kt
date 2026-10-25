package com.example.gamewebview

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Sembunyikan Status Bar & Navigation Bar untuk Layar Penuh (Immersive)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        val webView = WebView(this).apply {
            // 2. Paksa akselerasi hardware
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                
                // 3. MATIKAN ZOOM agar koordinat sentuhan/aim akurat
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
                
                // 4. Biarkan game menyesuaikan rasionya sendiri
                loadWithOverviewMode = true
                useWideViewPort = true
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)
            }

            val assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", AssetsPathHandler(this@MainActivity))
                .build()

            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    val url = request?.url ?: return null
                    return assetLoader.shouldInterceptRequest(url)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // 5. Suntikkan JS untuk memicu resize agar game menyesuaikan layar
                    val js = """
                        setTimeout(function() {
                            window.dispatchEvent(new Event('resize'));
                            if(document.documentElement) {
                                document.documentElement.style.height = '100vh';
                                document.body.style.height = '100vh';
                                document.body.style.margin = '0';
                                document.body.style.overflow = 'hidden';
                            }
                        }, 1000);
                    """.trimIndent()
                    view?.evaluateJavascript(js, null)
                }
            }

            loadUrl("https://pbmobile.app")
        }

        setContentView(webView)
    }
}
