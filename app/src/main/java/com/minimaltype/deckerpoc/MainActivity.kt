package com.minimaltype.deckerpoc

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import android.os.Environment
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
import android.webkit.ValueCallback
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var fileChooserLauncher: ActivityResultLauncher<Intent>
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.decorView.post {
            window.insetsController?.let { controller ->
                controller.hide(
                    android.view.WindowInsets.Type.statusBars() or
                            android.view.WindowInsets.Type.navigationBars()
                )
                controller.systemBarsBehavior =
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        fileChooserLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val uris: Array<Uri>? = if (result.resultCode == RESULT_OK) {
                WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
            } else null

            filePathCallback?.onReceiveValue(uris)
            filePathCallback = null
        }

        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview)
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun saveBlob(base64Data: String, filename: String) {
                val bytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename)
                file.writeBytes(bytes)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Saved $filename", Toast.LENGTH_SHORT).show()
                }
            }
        }, "Android")

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true

        val injectViewportJs = """(function() {
                if (!document.querySelector('meta[name="viewport"]')) {
                    var meta = document.createElement('meta');
                    meta.name = 'viewport';
                    meta.content = 'width=device-width, initial-scale=1.0, user-scalable=no';
                    document.getElementsByTagName('head')[0].appendChild(meta);
                }
            })();""".trimIndent()
        val fileSaverJs = """
            (function() {
                const orig = target.click;
                target.click = function() {
                    if (this.href.startsWith('blob:')) {
                        const blobUrl = this.href;
                        const filename = this.download || "downloaded_file";
                        fetch(blobUrl)
                            .then(res => res.arrayBuffer())
                            .then(buf => {
                                const base64 = btoa(
                                    new Uint8Array(buf)
                                    .reduce((data, byte) => data + String.fromCharCode(byte), '')
                                );
                                Android.saveBlob(base64, filename);
                            });
                        return; // prevent the default download
                    }
                    orig.apply(this, arguments);
                };
            })();
        """.trimIndent()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                webView.evaluateJavascript(injectViewportJs, null)
                webView.evaluateJavascript(fileSaverJs, null)
            }
        }
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimeType)
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Downloading file...")
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType))
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = filePathCallback

                val accept = fileChooserParams?.acceptTypes?.firstOrNull() ?: "*/*"
                val mimeType = if (accept.startsWith(".")) {
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(accept.removePrefix(".")) ?: "*/*"
                } else accept

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = mimeType
                }

                return try {
                    fileChooserLauncher.launch(intent)
                    true
                } catch (e: ActivityNotFoundException) {
                    this@MainActivity.filePathCallback?.onReceiveValue(null)
                    this@MainActivity.filePathCallback = null
                    false
                }
            }
        }

        webView.apply {
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
        }
        val intentData = intent.data
        if (intentData != null) {
            webView.loadUrl(intentData.toString())
        } else {
            webView.loadUrl("file:///android_asset/tour.html")
        }
    }
}
