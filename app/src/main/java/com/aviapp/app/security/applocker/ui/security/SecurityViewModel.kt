package com.aviapp.app.security.applocker.ui.security

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aviapp.app.security.applocker.data.AppDataProvider
import com.aviapp.app.security.applocker.data.database.lockedapps.LockedAppEntity
import com.aviapp.app.security.applocker.data.database.lockedapps.LockedAppsDao
import com.aviapp.app.security.applocker.ui.RxAwareViewModel
import com.aviapp.app.security.applocker.ui.security.function.AddSectionHeaderViewStateFunction
import com.aviapp.app.security.applocker.ui.security.function.LockedAppListViewStateCreator
import com.aviapp.app.security.applocker.util.extensions.doOnBackground
import com.aviapp.app.security.applocker.util.extensions.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SecurityViewModel @Inject constructor(
    appDataProvider: AppDataProvider,
    val lockedAppsDao: LockedAppsDao
) : RxAwareViewModel() {

    private val appDataViewStateListLiveData = MutableLiveData<List<AppLockItemBaseViewState>>()
    private val appDataViewStateListLiveDataProtected = MutableLiveData<List<AppLockItemBaseViewState>>()

    init {
        val installedAppsObservable = appDataProvider.fetchInstalledAppList().toObservable()
        val lockedAppsObservable = lockedAppsDao.getLockedApps().toObservable()

        disposables += LockedAppListViewStateCreator.create(installedAppsObservable, lockedAppsObservable)
            //.map(AddSectionHeaderViewStateFunction())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val p0 = it as List<AppLockItemItemViewState>

                val list1 = mutableListOf<AppLockItemItemViewState>()
                val list2 = mutableListOf<AppLockItemItemViewState>()

                p0.forEach {
                    if (it.isLocked)
                        list1.add(it)
                    else {
                        list2.add(it)
                    }
                }

                appDataViewStateListLiveData.value = list2
                appDataViewStateListLiveDataProtected.value = list1

            }
    }

    fun getProtectedApp(): LiveData<List<AppLockItemBaseViewState>> = appDataViewStateListLiveDataProtected
    fun getAppDataListLiveData(): LiveData<List<AppLockItemBaseViewState>> = appDataViewStateListLiveData

    fun lockApp(appLockItemViewState: AppLockItemItemViewState) {
        disposables += doOnBackground {
            lockedAppsDao.lockApp(
                LockedAppEntity(
                    appLockItemViewState.appData.packageName
                )
            )
        }
    }

    fun unlockApp(appLockItemViewState: AppLockItemItemViewState) {
        disposables += doOnBackground {
            lockedAppsDao.unlockApp(appLockItemViewState.appData.packageName)
        }
    }
}