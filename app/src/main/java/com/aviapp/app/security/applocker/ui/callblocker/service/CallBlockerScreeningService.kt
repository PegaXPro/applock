package com.aviapp.app.security.applocker.ui.callblocker.service

import android.annotation.TargetApi
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.aviapp.app.security.applocker.AppLockerApplication
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.aviapp.app.security.applocker.data.database.callblocker.blacklist.BlackListItemEntity
import com.aviapp.app.security.applocker.data.database.callblocker.calllog.CallLogItemEntity
import com.aviapp.app.security.applocker.repository.CallBlockerRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

@TargetApi(Build.VERSION_CODES.N)
class CallBlockerScreeningService : CallScreeningService() {

    lateinit var callBlockerRepository: CallBlockerRepository

    private var blackListDisposable: Disposable? = null

    private var TAG: String = "CallBlockerScreeningService"

    override fun onCreate() {
        super.onCreate()

        callBlockerRepository = (application as AppLockerApplication).callBlockerRepository

        //AndroidInjection.inject(this)
    }

    override fun onScreenCall(details: Call.Details) {

        if (details.handle == null || details.handle.schemeSpecificPart == null) {
            releaseCall(details = details)
            return
        }

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
                            val incomingPhoneNumber = details.handle.schemeSpecificPart
                            val matchType =
                                PhoneNumberUtil.getInstance()
                                    .isNumberMatch(it.phoneNumber, incomingPhoneNumber)
                            if (isPhoneMatch(matchType)) {
                                isInBlackList = true
                                itemEntity = it
                            }
                        }

                    Log.d(TAG, "isInBlackList $isInBlackList")
                    Log.d(TAG, "isInBetweenTimeLimit ${isInBetweenTimeLimit(itemEntity)}")

                    if (isInBlackList && isInBetweenTimeLimit(itemEntity)) {
                        rejectCall(details = details)
                        itemEntity?.let { saveCallLog(it) }

                    } else {
                        releaseCall(details = details)
                    }
                },
                { error ->  })
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

    private fun rejectCall(details: Call.Details) {
        respondToCall(
            details,
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipNotification(true)
                .setSkipCallLog(true)
                .build()
        )
    }

    private fun releaseCall(details: Call.Details) {
        respondToCall(details, CallResponse.Builder().build())
    }
}