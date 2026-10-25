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

        val webView = WebView(this).apply {
            // 1. Paksa akselerasi hardware paling dasar (lapisan GPU)
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true

                // 2. Matikan pembatasan zoom & viewport agar kita bisa atur resolusi
                setUseWideViewPort(true)
                setLoadWithOverviewMode(true)
                // 3. Paksa render pada ~75% resolusi untuk meringankan GPU Mali-G52
                setInitialScale(75) 

                // 4. Minta WebView untuk tidak "tidur" (throttling) saat game berjalan
                setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)
            }

            // 5. Kunci prioritas renderer ke level TERTINGGI agar tidak di-kill OS
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
            }

            // GANTI URL jika perlu. Pastikan game Anda mendukung WebGL.
            loadUrl("https://pbmobile.app")
        }

        setContentView(webView)
    }
}
