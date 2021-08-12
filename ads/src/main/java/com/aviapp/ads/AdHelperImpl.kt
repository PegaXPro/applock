package com.aviapp.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aviapp.purchase.PremEvents
import com.droidnet.DroidNet
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.*
import kotlin.coroutines.coroutineContext


class AdHelperImpl(private val context: Context, private val premDaoEvents: PremEvents) : AdHelper {

    val LOG = "AD_LOG"
    private var interstitialAd: InterstitialAd? = null
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val bannerUtil = BannerUtil()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var mDroidNet: DroidNet
    var prevNetState: Boolean? = false

    val _adEventLiveData = MutableLiveData<Boolean>()
    override val adEventLiveData: LiveData<Boolean>
        get() = _adEventLiveData

    private var rewardedAd: CompletableDeferred<RewardedInterstitialAd?> = CompletableDeferred()

    private var oldUnifiedNativeAd: NativeAd? = null
    //private var preloadUnifiedNativeAd: CompletableDeferred<NativeAd?> = CompletableDeferred()


    init {
        init()
    }

    override fun init() {

        MobileAds.initialize(context)
        scope.launch {
            val prem = premDaoEvents.prIsNeeded()
            initAdEvents()
            if (prem) {
                DroidNet.init(context);
                mDroidNet = DroidNet.getInstance()

                //preloadNativeAd().await()
                loadInter().await()
                loadReward()
            }
        }
    }

    override fun setOnClickListenerWithInter(view: View, scope: CoroutineScope, probability: Int, animation: Boolean, action: () -> Unit) {

        if (animation) {
            view.setOnClickListenerWIthAnimation {
                scope.launch {
                    val context = (view.context as Activity)
                    showInterstitial(context, probability, action)
                }
            }
        } else {
            view.setOnClickListener {
                scope.launch {
                    val context = (view.context as Activity)
                    showInterstitial(context, probability, action)
                }
            }
        }
    }


    override suspend fun getAdEventFlow() = premDaoEvents
        .prIsNeededFlow()
        .map { it && isOnline() }
        .flowOn(Dispatchers.IO)


    private fun initAdEvents() {
        scope.launch {
            premDaoEvents.prIsNeededFlow()
                .flowOn(Dispatchers.IO)
                .collect {
                    _adEventLiveData.value = it && isOnline()
                }
        }
    }

/*    @ExperimentalCoroutinesApi
    val adEventLiveData by lazy {
        appDatabase.premDao().getPremFlow().map { Log.d("SDFSEF", "%%%"); it?:Prem() }.flowOn(Dispatchers.IO).combine(netFlow) { a, b -> a.prIdNeeded && b }.asLiveData()
    }*/

    override suspend fun showNative(
        adId: String,
        activity: Activity?,
        view: View?,
        show: Boolean?,
        small: Boolean?
    ) {

        val adHolder: FrameLayout? = if (view != null) view.findViewById(R.id.nativeHolder)
        else activity?.findViewById(R.id.nativeHolder)
        adHolder ?: return
        val checkShow = show ?: showAd()
        if (checkShow) {
            val res = when (small) {
                null -> {
                    getLayoutRes(adHolder)
                }
                false -> {
                    R.layout.navive_ad_big
                }
                true -> {
                    R.layout.navive_ad_small
                }
            }

            val inflateNativeAd =
                LayoutInflater.from(activity?:view?.context).inflate(res, adHolder, false) as CardView

            createNativeAd(adId, inflateNativeAd.findViewById(R.id.unifiedNativeAdView)) {

                if (small == false) {
                    val holder_f = LayoutInflater.from(activity?:view?.context)
                        .inflate(R.layout.holder_f, adHolder, false) as ConstraintLayout
                    holder_f.addView(inflateNativeAd)
                    adHolder.removeAllViews()
                    adHolder.addView(holder_f)
                    adHolder.visibility = View.VISIBLE
                } else {
                    adHolder.removeAllViews()
                    adHolder.addView(inflateNativeAd)
                    adHolder.visibility = View.VISIBLE
                }

            }

        } else {
            adHolder.removeAllViews()
            adHolder.visibility = View.INVISIBLE
        }

    }

    data class NativeAdItem(val layoutResLayout: FrameLayout, val small: Boolean? = null)

    override suspend fun showNativeAds(adId: String, activity: Activity, view: View?, list: List<NativeAdItem>) {

        list.forEach {

            if (showAd()) {

                val res = when (it.small) {
                    null -> {
                        getLayoutRes(it.layoutResLayout)
                    }
                    false -> {
                        R.layout.navive_ad_big
                    }
                    true -> {
                        R.layout.navive_ad_small
                    }
                }

                val inflateNativeAd = LayoutInflater.from(activity)
                    .inflate(res, it.layoutResLayout, false) as CardView

                createNativeAd(adId, inflateNativeAd.findViewById(R.id.unifiedNativeAdView)) {

                    if (it.small == false) {

                        val holder_f = LayoutInflater.from(activity).inflate(
                            R.layout.holder_f,
                            it.layoutResLayout,
                            false
                        ) as ConstraintLayout
                        holder_f.addView(inflateNativeAd)
                        it.layoutResLayout.removeAllViews()
                        it.layoutResLayout.addView(holder_f)
                        it.layoutResLayout.visibility = View.VISIBLE
                    } else {

                        it.layoutResLayout.removeAllViews()
                        it.layoutResLayout.addView(inflateNativeAd)
                        it.layoutResLayout.visibility = View.VISIBLE
                    }
                }

            } else {
                it.layoutResLayout.removeAllViews()
                it.layoutResLayout.visibility = View.INVISIBLE
            }
        }
    }


    override suspend fun showInterWhenLoaded(context: Activity, action: (() -> Unit)?) {

        var show = true

        if (showAd(0)) {
            val job = CoroutineScope(coroutineContext + Job()).launch {
                delay(6000)
                action?.invoke()
                show = false
            }

            InterstitialAd.load(
                context,
                BuildConfig.INTERSTITIAL,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        job.cancel()
                        if (show)
                            action?.invoke()
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        if (show) {
                            interstitialAd.show(context)
                            interstitialAd.fullScreenContentCallback =
                                object : FullScreenContentCallback() {
                                    override fun onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent()
                                        action?.invoke()
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        job.cancel()
                                        super.onAdShowedFullScreenContent()
                                    }

                                    override fun onAdFailedToShowFullScreenContent(p0: AdError?) {
                                        super.onAdFailedToShowFullScreenContent(p0)
                                        job.cancel()
                                        action?.invoke()
                                    }
                                }
                        }
                    }

                })

        } else {
            if (show)
                action?.invoke()
        }
    }

    override suspend fun showInterstitial(
        activity: Activity,
        probability: Int,
        adClose: () -> Unit
    ) {

        if (requestNewInterstitial(activity, probability)) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    interstitialAd?.fullScreenContentCallback = null
                    interstitialAd = null
                    loadInter()
                    adClose.invoke()
                }
            }
        } else {
            adClose.invoke()
        }
    }

    override suspend fun showBanner(activity: Activity, view: View?, show: Boolean?) {

        val checkShow = show ?: showAd()
        if (checkShow) {
            try {
                bannerUtil.showBanner(activity, view)
            } catch (e: Throwable) {
                Log.d(LOG, "banner error: ${e.message}")
            }
        } else {
            val adHolder = if (view != null) view.findViewById<FrameLayout>(R.id.adHolderB)
            else activity.findViewById(R.id.adHolderB)
            adHolder?.removeAllViews()
            adHolder?.visibility = View.INVISIBLE
        }
    }

    private fun preloadNativeAd(adId: String): CompletableDeferred<NativeAd> {

        val preloadUnifiedNativeAd = CompletableDeferred<NativeAd>()

        val native = AdLoader.Builder(context, adId)
            .forNativeAd {
                preloadUnifiedNativeAd.complete(it)
            }
            .withAdListener(object: AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError?) {
                    super.onAdFailedToLoad(p0)
                    /*if (oldUnifiedNativeAd != null) {
                        preloadUnifiedNativeAd.complete(oldUnifiedNativeAd)
                    } else {
                        preloadUnifiedNativeAd.complete(null)
                    }*/
                }
            })
            .build()

        native.loadAd(AdRequest.Builder().build())
        return preloadUnifiedNativeAd
    }

    private suspend fun getLayoutRes(view: FrameLayout) = suspendCancellableCoroutine<Int> {
        val callback = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this);
                val min = (150 * context.resources.displayMetrics.density).toInt()
                if (view.height < min) it.resume(R.layout.navive_ad_small) {
                    view.viewTreeObserver.removeOnGlobalLayoutListener(
                        this
                    )
                }

                else it.resume(R.layout.navive_ad_big) {
                    view.viewTreeObserver.removeOnGlobalLayoutListener(
                        this
                    )
                }

            }
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(callback)

        it.invokeOnCancellation {
            view.viewTreeObserver.removeOnGlobalLayoutListener(callback)
        }

    }


/*    @ExperimentalCoroutinesApi
    fun showAdFlow() = appDatabase.premDao().getPremFlow().flowOn(Dispatchers.IO).combine(netFlow) { a, b -> a.prIdNeeded && b }.asLiveData()*/

    private suspend fun requestNewInterstitial(activity: Activity, probability: Int): Boolean {
        if (showAd(probability)) {
            return try {
                if (interstitialAd != null) {
                    interstitialAd?.show(activity)
                    true
                } else
                    false
            } catch (e: Throwable) {
                false
            }
        }
        return false
    }


    private fun loadInter(): CompletableDeferred<Boolean> {

        val res = CompletableDeferred<Boolean>()

        InterstitialAd.load(
            context,
            BuildConfig.INTERSTITIAL,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    interstitialAd = null
                    res.complete(true)
                }

                override fun onAdLoaded(p0: InterstitialAd) {
                    super.onAdLoaded(p0)
                    interstitialAd = p0
                    res.complete(true)
                }
            })
        return res
    }


    override suspend fun requestReward(
        activity: Activity,
        action: (onUserEarnedReward: Boolean) -> Unit
    ) {

        var rewardRez = false

        if (showAd()) {

            val rewAd = rewardedAd.await()
            if (rewAd == null) {
                action.invoke(!premDaoEvents.prIsNeeded())
                loadReward()
                return
            }

            rewAd.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    action.invoke(rewardRez)
                    loadReward()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError?) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    action.invoke(rewardRez)
                }
            }

            rewAd.show(activity, OnUserEarnedRewardListener {
                rewardRez = true
            })

        } else {
            action.invoke(!premDaoEvents.prIsNeeded())
        }
    }

    private fun loadReward() {

        rewardedAd = CompletableDeferred()

        RewardedInterstitialAd.load(
            context,
            BuildConfig.REWARDED,
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(p0: RewardedInterstitialAd) {
                    super.onAdLoaded(p0)
                    rewardedAd.complete(p0)
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    rewardedAd.complete(null)
                }
            })

    }

    override suspend fun showAd(frequency: Int): Boolean {
        return if (frequency == 0) {
            val bb = premDaoEvents.prIsNeeded()
            bb && isOnline()
        } else {
            val random = Random()
            val ran = random.nextInt(frequency)
            if (ran == 0) {
                val bb = premDaoEvents.prIsNeeded()
                bb && isOnline()
            } else {
                false
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun isOnline(): Boolean {
        return try {
            cm.activeNetworkInfo?.isConnectedOrConnecting ?: false
        } catch (unused: Exception) {
            false
        }
    }


    private suspend fun createNativeAd(adId: String, adView: NativeAdView, done: () -> Unit) {

        val preloadedAd = preloadNativeAd(adId).await()

        if (preloadedAd != null) {
            populateUnifiedNativeAdView(
                preloadedAd,
                adView
            )
            done.invoke()
            //preloadNativeAd()
        } else {
            //preloadNativeAd()
        }
    }


    private fun populateUnifiedNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

        val nativeButton: View? = adView.findViewById(R.id.nativeButton)
        val buttonText: TextView? = adView.findViewById(R.id.callToActionText)
        val nativeHeadline: View? = adView.findViewById(R.id.nativeHeadline)
        val nativeBody: View? = adView.findViewById(R.id.nativeBody)
        val nativeIcon: View? = adView.findViewById(R.id.nativeIcon)
        val nativeMedia: MediaView? = adView.findViewById(R.id.nativeMedia)

        buttonText?.let {
            val cta = nativeAd.callToAction
            it.text = cta
        }

        adView.callToActionView = nativeButton
        adView.headlineView = nativeHeadline
        adView.bodyView = nativeBody
        adView.imageView = nativeIcon

        if (nativeMedia != null) {
            adView.mediaView = nativeMedia
            adView.mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
        }

        (adView.headlineView as TextView).text = nativeAd.headline
        if (nativeAd.body == null) {
            adView.bodyView.visibility = View.INVISIBLE
        } else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        if (nativeAd.images.size == 0) {
            adView.imageView.visibility = View.GONE
        } else {
            (adView.imageView as ImageView).setImageDrawable(
                nativeAd.images[0].drawable
            )
            (adView.imageView as ImageView).clipToOutline = true
            adView.imageView.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)
    }
}