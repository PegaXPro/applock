package com.aviapp.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*


class AppOpenAds(private val myApplication: Application, private val adService: AdHelper) : Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private var appOpenAd: AppOpenAd? = null
    private var loadCallback: AppOpenAdLoadCallback? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val LOG_TAG = "AppOpenManager"
    private val AD_UNIT_ID = BuildConfig.APP_OPEN_ADS
    private var currentActivity: Activity? = null
    var loadTime: Long = 0

    init {
        myApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this);
    }

    fun fetchAd() = scope.launch {

       // if (!adService.showAd(0)) return@launch

        loadCallback = object : AppOpenAdLoadCallback() {


            override fun onAdLoaded(p0: AppOpenAd) {
                appOpenAd = p0
                loadTime = Date().time
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
            }
        }

        val request: AdRequest = adRequest
        AppOpenAd.load(
                myApplication, AD_UNIT_ID, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback)
    }


    var isShowingAd = false

    private fun showAdIfAvailable() = scope.launch {
        if (adService.showAd(0) && isAdAvailable && !checkSplash()) {
            val fullScreenContentCallback: FullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    fetchAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {}
                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                }
            }
            currentActivity?.let {
                appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
                appOpenAd?.show(it)
            }

        } else {
            fetchAd()
        }
    }

    private val adRequest: AdRequest
        private get() = AdRequest.Builder().build()

    private val isAdAvailable: Boolean
        get() = appOpenAd != null

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
    override fun onActivityStarted(p0: Activity) {

        currentActivity = p0;
    }

    override fun onActivityResumed(p0: Activity) {
        currentActivity = p0;
    }

    override fun onActivityPaused(p0: Activity) {}
    override fun onActivityStopped(p0: Activity) {}
    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
    override fun onActivityDestroyed(p0: Activity) {
        currentActivity = null;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        showAdIfAvailable()
    }


    private fun checkSplash(): Boolean {

        return if (currentActivity == null) false
        else currentActivity!!::class.java.simpleName == "SplashActivity"
    }

}