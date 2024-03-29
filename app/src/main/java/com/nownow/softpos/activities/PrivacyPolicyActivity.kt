package com.nownow.softpos.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.android.material.tabs.TabLayout
import com.nownow.softpos.R
import com.nownow.softpos.databinding.ActivityPrivacyPolicyBinding
import com.nownow.softpos.utils.SharedPrefUtils

class PrivacyPolicyActivity : AppCompatActivity() ,OnClickListener{
    lateinit var binding:ActivityPrivacyPolicyBinding
    private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpUI()
        binding.layoutTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                loadWebViewContent(tab.position)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) { }
            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
        webView = binding.webview
        // Enable JavaScript (if required)
        webView.settings.javaScriptEnabled = true

        // Load the initial WebView content for the selected tab
        loadWebViewContent(binding.layoutTab.selectedTabPosition)

        webView.webViewClient = object : WebViewClient() { }
        webView.webChromeClient = object : WebChromeClient() { }
    }

    private fun setUpUI() {
        binding.backIcon.setOnClickListener(this)
    }

    private fun loadWebViewContent(tabPosition: Int) {
        val htmlContent = when (tabPosition) {
            0 -> {
                SharedPrefUtils.TermAndConditionURL.let {
                    webView.loadUrl(it.toString())
                }

            }
            1 -> {
                SharedPrefUtils.PrivacyPolicyURL.let {
                    webView.loadUrl(it.toString())
                }

            }
            else -> ""
        }

        //if (htmlContent.isNotEmpty()) {
        //    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        //}
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.back_icon -> onBackPressed()
        }
    }
}