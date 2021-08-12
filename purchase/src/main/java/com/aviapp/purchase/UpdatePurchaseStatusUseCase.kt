package com.aviapp.purchase

import com.android.billingclient.api.BillingClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class UpdatePurchaseStatusUseCase(private val premEvents: PremEvents) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    fun restore(billingClient: BillingClient) {

        scope.launch {
            val details = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
            if (!details.purchasesList.isNullOrEmpty()) {
                update(false)
                return@launch
            }
            val oldPr = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
            if (!oldPr.purchasesList.isNullOrEmpty()) {
                update(false)
                return@launch
            }
            update(true)
        }
    }

    fun update(prIsNeeded: Boolean) {
        scope.launch {
            premEvents.updatePrem(prIsNeeded)
        }
    }

}