package com.aviapp.app.security.applocker.ui.callblocker.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import com.android.internal.telephony.ITelephony
import com.aviapp.app.security.applocker.data.database.callblocker.blacklist.BlackListItemEntity
import com.aviapp.app.security.applocker.data.database.callblocker.calllog.CallLogItemEntity
import com.aviapp.app.security.applocker.repository.CallBlockerRepository
import com.google.i18n.phonenumbers.PhoneNumberUtil
import dagger.android.DaggerBroadcastReceiver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class CallReceiver : DaggerBroadcastReceiver() {

    @Inject
    lateinit var callBlockerRepository: CallBlockerRepository

    private var blackListDisposable: Disposable? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent == null) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return
        }
        val stateStr = intent.extras!!.getString(TelephonyManager.EXTRA_STATE)
        val number = intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
        when (stateStr) {
            TelephonyManager.EXTRA_STATE_RINGING -> onPhoneRinging(context, number!!)
        }
    }

    private fun onPhoneRinging(context: Context?, incomingCallNumber: String) {

        blackListDisposable?.let {
            if (it.isDisposed.not()) {
                it.dispose()
            }
        }

        blackListDisposable = callBlockerRepository.getBlackList()
            .firstOrError()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { blackList ->
                    var isInBlackList = false
                    var itemEntity: BlackListItemEntity? = null
                    blackList
                        .takeWhile { isInBlackList.not() }
                        .forEach {
                            val matchType =
                                PhoneNumberUtil.getInstance()
                                    .isNumberMatch(it.phoneNumber, incomingCallNumber)
                            if (isPhoneMatch(matchType)) {
                                isInBlackList = true
                                itemEntity = it
                            }
                        }
                    if (isInBlackList) {
                        if (isInBetweenTimeLimit(itemEntity)) {
                            reject(context)
                            itemEntity?.let { saveCallLog(it) }
                        }
                    }
                },
                { error -> "df" })
    }

    private fun isInBetweenTimeLimit(itemEntity: BlackListItemEntity?): Boolean {
        val calendar = Calendar.getInstance()
        val currentHour = calendar[Calendar.HOUR_OF_DAY]
        val startTime = itemEntity?.startTime?.toInt()
        val endTime = itemEntity?.stopTime?.toInt()
        if (startTime != null && endTime != null) {
            if (currentHour >= startTime && currentHour <= endTime) {
                return true
            }
        }
        return false
    }

    private fun isPhoneMatch(matchResult: PhoneNumberUtil.MatchType): Boolean {
        return matchResult == PhoneNumberUtil.MatchType.EXACT_MATCH ||
                matchResult == PhoneNumberUtil.MatchType.NSN_MATCH ||
                matchResult == PhoneNumberUtil.MatchType.SHORT_NSN_MATCH
    }

    private fun saveCallLog(blackListItemEntity: BlackListItemEntity) {
        CallLogItemEntity(
            logDate = Date(),
            userName = blackListItemEntity.userName,
            phoneNumber = blackListItemEntity.phoneNumber
        ).also {
            callBlockerRepository.addToCallLog(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        }
    }

    private fun reject(context: Context?) {
        val telephonyManager =
            context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val clazz = Class.forName(telephonyManager.javaClass.name)
        val method = clazz.getDeclaredMethod("getITelephony")
        method.isAccessible = true
        val telephonyService = method.invoke(telephonyManager) as ITelephony
        telephonyService.endCall()
    }
}

