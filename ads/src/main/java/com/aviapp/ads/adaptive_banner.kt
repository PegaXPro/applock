package com.aviapp.ads

import android.app.Activity
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.ads.*


class BannerUtil {

    private fun getAdSize(activity: Activity, container: FrameLayout): AdSize {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = container.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }


    private fun loadBanner(adView: AdView, uid: String, adSize: AdSize) {

        adView.adUnitId = uid
        adView.adSize = adSize

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }


    fun showBanner(activity: Activity, view: View?) {

        val adHolder = if(view != null) view.findViewById<FrameLayout>(R.id.adHolderB)
        else activity.findViewById(R.id.adHolderB)
        adHolder?: return
        val adView = AdView(activity)
        val uid = BuildConfig.BANNER
        adHolder.addView(adView)
        loadBanner(adView, uid, getAdSize(activity, adHolder))
    }
}