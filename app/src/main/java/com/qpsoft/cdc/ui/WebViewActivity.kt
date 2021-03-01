package com.qpsoft.cdc.ui

import android.net.http.SslError
import android.os.Bundle
import android.text.TextUtils
import android.webkit.*
import com.blankj.utilcode.util.LogUtils
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import kotlinx.android.synthetic.main.activity_webview.*

class WebViewActivity : BaseActivity() {

    private var url: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        setBackBtn()

        url = intent.getStringExtra("url")

        if (url?.contains("schoolhealth") == true) {
            setTitle("学校教学环境监测调查")
        } else {
            setTitle("验光仪使用帮助")
        }

        val setting = webView.settings
        setting.javaScriptEnabled = true
        setting.domStorageEnabled = true
        setting.cacheMode = WebSettings.LOAD_NO_CACHE

        webView.webViewClient = object: WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }

            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
//                val title = view.title
//                if (!TextUtils.isEmpty(title)) {
//                    setTitle(title)
//                }
            }
        }

        LogUtils.e("-----------"+url)
        webView.loadUrl(url!!)
    }


    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            finish()
        }
    }
}