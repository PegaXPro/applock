package com.aviapp.app.security.applocker.data.database.callblocker.addtoblacklist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aviapp.app.security.applocker.data.database.callblocker.blacklist.BlackListItemEntity
import com.aviapp.app.security.applocker.repository.CallBlockerRepository
import com.aviapp.app.security.applocker.ui.RxAwareViewModel
import com.aviapp.app.security.applocker.util.extensions.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AddToBlackListViewModel @Inject constructor(val callBlockerRepository: CallBlockerRepository) :
    RxAwareViewModel() {

    private val addToBlacklistViewStateLiveData = MutableLiveData<AddToBlacklistViewState>()

    fun blockNumber(name: String, phoneNumber: String, startTime: String, stopTime: String) {
        val userName = if (name.isNullOrEmpty()) DEFAULT_NAME else name
        val blackListItemEntity = BlackListItemEntity(
            userName = userName,
            phoneNumber = phoneNumber,
            startTime = startTime,
            stopTime = stopTime
        )
        disposables += callBlockerRepository.addToBlackList(blackListItemEntity)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    addToBlacklistViewStateLiveData.value =
                        AddToBlacklistViewState(BlockState.BLOCKED)
                },
                { t ->
                    addToBlacklistViewStateLiveData.value =
                        AddToBlacklistViewState(BlockState.ERROR)
                })
    }

    fun getViewStateLiveData(): LiveData<AddToBlacklistViewState> = addToBlacklistViewStateLiveData

    companion object {
        private const val DEFAULT_NAME = ""
    }
}