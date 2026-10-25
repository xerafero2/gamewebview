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

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen Immersive Mode
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        val webView = WebView(this).apply {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
                setSupportZoom(false)
                builtInZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    // Trik devicePixelRatio untuk meringankan GPU
                    val hackScript = """
                        (function() {
                            try {
                                Object.defineProperty(window, 'devicePixelRatio', {
                                    get: function() { return 1; }
                                });
                            } catch(e) {}
                        })();
                    """.trimIndent()
                    view?.evaluateJavascript(hackScript, null)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    
                    // Script untuk memunculkan FPS Counter dan memicu resize
                    val fpsScript = """
                        setTimeout(function() {
                            window.dispatchEvent(new Event('resize'));
                            
                            // --- MULAI SCRIPT FPS COUNTER ---
                            if (!document.getElementById('fps-counter-overlay')) {
                                var div = document.createElement('div');
                                div.id = 'fps-counter-overlay';
                                // Styling: Pojok kanan atas, hijau, menembus sentuhan (pointer-events: none)
                                div.style.cssText = 'position:fixed; top:10px; right:10px; z-index:999999; color:#00FF00; font-family:monospace; font-size:20px; font-weight:bold; text-shadow:1px 1px 2px black; pointer-events:none; background:rgba(0,0,0,0.5); padding:4px 8px; border-radius:4px;';
                                div.innerText = 'FPS: ...';
                                document.body.appendChild(div);

                                var lastTime = performance.now();
                                var frames = 0;
                                
                                function updateFps() {
                                    var now = performance.now();
                                    frames++;
                                    if (now - lastTime >= 1000) {
                                        var fps = Math.round((frames * 1000) / (now - lastTime));
                                        div.innerText = 'FPS: ' + fps;
                                        frames = 0;
                                        lastTime = now;
                                    }
                                    requestAnimationFrame(updateFps);
                                }
                                requestAnimationFrame(updateFps);
                            }
                            // --- AKHIR SCRIPT FPS COUNTER ---
                        }, 1500);
                    """.trimIndent()
                    view?.evaluateJavascript(fpsScript, null)
                }
            }

            loadUrl("https://pbmobile.app")
        }

        setContentView(webView)
    }
}
