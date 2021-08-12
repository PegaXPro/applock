package com.aviapp.app.security.applocker.ui.browser

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.aviapp.app.security.applocker.BuildConfig
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.database.bookmark.BookmarkEntity
import com.aviapp.app.security.applocker.databinding.ActivityBrowserBinding
import com.aviapp.app.security.applocker.ui.BaseActivity
import com.aviapp.app.security.applocker.ui.browser.analytics.BrowserAnalytics
import com.aviapp.app.security.applocker.ui.browser.bookmarks.BookmarksDialog
import com.aviapp.app.security.applocker.ui.browser.resolver.UrlResolver.FACEBOOK
import com.aviapp.app.security.applocker.ui.browser.resolver.UrlResolver.INSTAGRAM
import com.aviapp.app.security.applocker.ui.browser.resolver.UrlResolver.TWITTER
import com.aviapp.app.security.applocker.ui.browser.resolver.UrlResolver.YOUTUBE
import com.aviapp.app.security.applocker.ui.browser.resolver.UrlResolver.SNAPCHAT
import com.aviapp.app.security.applocker.util.extensions.hideKeyboard


class BrowserActivity : BaseActivity<BrowserViewModel>(), BookmarksDialog.BookmarkItemSelectListener {

    private lateinit var binding: ActivityBrowserBinding

    private val browserClient = object : WebViewClient() {

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            viewModel.complete()
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            viewModel.load(url!!)
            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            removeFocusFromBar()
            hideKeyboard(binding.edittextUrl)
        }

    }

    private val chromeClient = object : WebChromeClient() {

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            viewModel.updateLoadingProgress(newProgress)
        }
    }

    override fun getViewModel(): Class<BrowserViewModel> = BrowserViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adId = if (!BuildConfig.DEBUG) "ca-app-pub-4875591253190011/4560402780" else "ca-app-pub-3940256099942544/1044960115"
        binding = DataBindingUtil.setContentView(this, R.layout.activity_browser)

        initAd()

        initializeWebView()

        viewModel.getBrowserViewStateLiveData().observe(this) {
            if (it.isBookmarked) {
                binding.selectStar.setImageResource(R.drawable.select_start)
                //binding.imageViewBookmarks.setImageResource(R.drawable.ic_add_to_b_m)
            } else {
                binding.selectStar.setImageResource(R.drawable.star_unselect)
                //binding.imageViewBookmarks.setImageResource(R.drawable.ic_bookmarks)
            }
        }

        binding.imageViewBack.setOnClickListener { onBackPressed() }

        binding.layoutFacebook.setOnClickListener {
            viewModel.load(FACEBOOK)
            BrowserAnalytics.sendQuickBrowsingClicked(this, FACEBOOK)
        }

        binding.layoutInstagram.setOnClickListener {
            viewModel.load(INSTAGRAM)
            BrowserAnalytics.sendQuickBrowsingClicked(this, INSTAGRAM)
        }

        binding.layoutTwitter.setOnClickListener {
            viewModel.load(TWITTER)
            BrowserAnalytics.sendQuickBrowsingClicked(this, TWITTER)
        }

        binding.layoutYoutube.setOnClickListener {
            viewModel.load(YOUTUBE)
            BrowserAnalytics.sendQuickBrowsingClicked(this, YOUTUBE)
        }

        binding.layoutSnapchat.setOnClickListener {
            viewModel.load(SNAPCHAT)
            BrowserAnalytics.sendQuickBrowsingClicked(this, SNAPCHAT)
        }

        binding.selectStar.setOnClickListener {
            if (viewModel.isCurrentBookmarked()) {
                viewModel.removeFromBookmark()
            } else {
                viewModel.addToBookmarks()
            }
        }

        binding.imageViewBookmarks.setOnClickListener {

            BookmarksDialog
                .newInstance()
                .show(supportFragmentManager, "")
        }

        binding.edittextUrl.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.load(binding.edittextUrl.text.toString())
                true
            }
            false
        }

        viewModel.getBrowserViewStateLiveData().observe(this) {
            binding.edittextUrl.clearFocus()
            binding.viewState = it
            binding.executePendingBindings()

            if (it.webPageState == WebPageState.REQUEST) {
                binding.selectStar.visibility = View.VISIBLE
                binding.webView.loadUrl(it.url)
            }
        }

    }


    override fun onBackPressed() {

        if (binding.webView.copyBackForwardList().size == 2) {
            binding.webView.clearHistory()
        }

        when {
            binding.webView.canGoBack() -> goBackInWebView()
            viewModel.isResetMode() -> super.onBackPressed()
            else ->  {
                binding.selectStar.visibility = View.GONE
                viewModel.reset()
            }
        }

    }

/*    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    when {
                        binding.webView.canGoBack() -> goBackInWebView()
                        viewModel.isResetMode() -> finish()
                        else -> viewModel.reset()
                    }
                    return true
                }
            }

        }
        return super.onKeyDown(keyCode, event)
    }*/

    override fun onBookmarkItemSelected(bookmarkEntity: BookmarkEntity) {
        viewModel.load(bookmarkEntity.url)
    }

    private fun initializeWebView() {

        CookieManager.getInstance().setAcceptCookie(false)

        with(binding.webView) {
            webChromeClient = chromeClient
            webViewClient = browserClient
            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.setAppCacheEnabled(false)
            clearHistory();
            clearCache(true)
            clearFormData();
            settings.savePassword = false
            settings.saveFormData = false
        }
    }

    private fun removeFocusFromBar() {
        with(binding.edittextUrl) {
            isFocusableInTouchMode = false
            isFocusable = false
            isFocusableInTouchMode = true
            isFocusable = true
        }
    }

    private fun goBackInWebView() {
        val mWebBackForwardList = binding.webView.copyBackForwardList()
        val historyUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.currentIndex - 1).url
        viewModel.completeWithUrl(historyUrl)
        binding.webView.goBack()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, BrowserActivity::class.java)
        }
    }
}