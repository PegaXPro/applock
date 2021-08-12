package com.aviapp.ads

import android.app.Activity
import android.view.View
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface AdHelper {

    val adEventLiveData: LiveData<Boolean>
    suspend fun showNative (adId: String, activity: Activity?, view: View? = null, show: Boolean? = null, small: Boolean? = false)
    suspend fun showInterWhenLoaded (context: Activity, action: (() -> Unit)? = null)
    suspend fun showInterstitial (activity: Activity, probability: Int = 0, adClose: () -> Unit)
    suspend fun showBanner(activity: Activity, view: View? = null, show: Boolean? = null)
    suspend fun requestReward(activity: Activity, action: (onUserEarnedReward: Boolean) -> Unit)
    suspend fun showNativeAds(adId: String, activity: Activity, view: View?, list: List<AdHelperImpl.NativeAdItem>)
    suspend fun showAd(frequency: Int = 0): Boolean
    fun isOnline(): Boolean
    suspend fun getAdEventFlow(): Flow<Boolean>
    fun init()
    fun setOnClickListenerWithInter(view: View, scope: CoroutineScope, probability: Int = 0, animation: Boolean = false, action: () -> Unit)
}