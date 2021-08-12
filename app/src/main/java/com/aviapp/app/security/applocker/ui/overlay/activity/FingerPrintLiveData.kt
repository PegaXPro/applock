package com.aviapp.app.security.applocker.ui.overlay.activity

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.aviapp.app.security.applocker.data.AppLockerPreferences
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint
import javax.inject.Inject

class FingerPrintLiveData @Inject constructor(private val fingerprintIdentify: FingerprintIdentify, val context: Context) :
    MutableLiveData<FingerPrintResultData>(),
    BaseFingerprint.ExceptionListener {

    var appLockerPreferences:AppLockerPreferences = AppLockerPreferences(context)

    init {
        try {
        } catch (exception: Exception) {
        }
    }

    override fun onActive() {
        super.onActive()
       if(appLockerPreferences.getFingerPrintEnabled()){
           fingerprintIdentify.startIdentify(3, object : BaseFingerprint.IdentifyListener {
               override fun onSucceed() {
                   value = FingerPrintResultData.matched()
               }

               override fun onNotMatch(availableTimes: Int) {
                   value = FingerPrintResultData.notMatched(availableTimes)
               }

               override fun onFailed(isDeviceLocked: Boolean) {
                   value = FingerPrintResultData.error("Fingerprint error")
               }

               override fun onStartFailedByDeviceLocked() {
                   value = FingerPrintResultData.error("Fingerprint error")
               }
           })
       }

    }

    override fun onInactive() {
        super.onInactive()
        fingerprintIdentify.cancelIdentify()
    }

    override fun onCatchException(exception: Throwable?) {
        value = FingerPrintResultData.error(exception?.message ?: "")
    }
}