package com.aviapp.purchase

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import java.util.*

class PurchaseService(
    context: Context,
    private val premDaoEvents: PremEvents,
    private val updatePurchaseStatusUseCase: UpdatePurchaseStatusUseCase
) : PurchasesUpdatedListener, AcknowledgePurchaseResponseListener {

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    val priseLiveData = MutableLiveData<String>()
    private val firebaseAnalytics: FirebaseAnalytics

    init {

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}
            override fun onBillingSetupFinished(p0: BillingResult) {
                if (p0.responseCode == BillingClient.BillingResponseCode.OK) {
                    updatePurchaseStatusUseCase.restore(billingClient)
                    val sku = mutableListOf(prId)
                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(sku).setType(BillingClient.SkuType.INAPP)
                    billingClient.querySkuDetailsAsync(params.build()) { p0, p1 ->
                        p1?.forEach {
                            if (it.sku == prId) {
                                priseLiveData.postValue(it.price)
                            }
                        }
                    }
                }
            }
        })

        firebaseAnalytics = Firebase.analytics
        initPrEvents()
    }

    private val _adEventLiveData = MutableLiveData<Boolean>()
    val prEventLiveData: LiveData<Boolean>
        get() = _adEventLiveData

    private fun initPrEvents() {
        coroutineScope.launch {
            premDaoEvents.prIsNeededFlow()
                    .flowOn(Dispatchers.IO)
                    .collect {
                        _adEventLiveData.value = it
                    }
        }
    }

    private fun firebaseLogPr(prId: String, pr: String, otherData: String) {
        val itemJeggings = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, prId)
            putString(FirebaseAnalytics.Param.PRICE, pr)
            putString(FirebaseAnalytics.Param.CONTENT, otherData)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, itemJeggings)
    }

    suspend fun prFlow() {}

    suspend fun prIsNeeded() = premDaoEvents.prIsNeeded()

    var currentDialog: BottomSheetDialogFragment? = null

    fun createPurchase(activity: Activity) {
        coroutineScope.launch {
            if (!prIsNeeded()) return@launch
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        val skuId = prId
                        val skuList: MutableList<String> = ArrayList()
                        skuList.add(skuId)
                        val params = SkuDetailsParams.newBuilder()
                        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
                        billingClient.querySkuDetailsAsync(
                            params.build()
                        ) { _: BillingResult?, skuDetailsList: List<SkuDetails>? ->
                            skuDetailsList ?: return@querySkuDetailsAsync

                            for (skuDetails in skuDetailsList) {
                                if (skuDetails.sku == skuId) {
                                    val bilParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetails)
                                        .build()
                                    billingClient.launchBillingFlow(activity, bilParams)
                                }
                            }
                        }
                    }
                }
                override fun onBillingServiceDisconnected() {}
            })
        }
    }


    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
        val respCode: Int = p0.responseCode
        if (respCode == BillingClient.BillingResponseCode.OK && !p1.isNullOrEmpty()) {
            handlePurchase(p1.first())
        } else if (respCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            updatePurchaseStatusUseCase.update(false)
            handleDialog()
        }
    }

    private fun handleDialog() {

        if (currentDialog != null && currentDialog?.isVisible == false) {
            try {
                currentDialog?.dismiss()
            } catch (e: Throwable) {}
        }

        currentDialog = null
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {

                val acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, this);
                updatePurchaseStatusUseCase.update(false)
                handleDialog()
                firebaseLogPr(prId, priseLiveData.value ?: "empty", purchase.originalJson)
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
        } else if (purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) { }
    }

    override fun onAcknowledgePurchaseResponse(p0: BillingResult) {}

    companion object {
        const val prId = "com.app.lock.password.applocker.removeads"
    }
}