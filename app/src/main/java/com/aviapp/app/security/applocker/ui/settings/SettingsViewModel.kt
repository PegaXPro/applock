package com.aviapp.app.security.applocker.ui.settings

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aviapp.app.security.applocker.data.AppDataProvider
import com.aviapp.app.security.applocker.data.AppLockerPreferences
import com.aviapp.app.security.applocker.data.database.lockedapps.LockedAppEntity
import com.aviapp.app.security.applocker.data.database.lockedapps.LockedAppsDao
import com.aviapp.app.security.applocker.ui.RxAwareAndroidViewModel
import com.aviapp.app.security.applocker.ui.security.AppLockItemItemViewState
import com.aviapp.app.security.applocker.util.extensions.doOnBackground
import com.aviapp.app.security.applocker.util.extensions.plusAssign
import com.google.zxing.common.StringUtils
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    val app: Application,
    val appDataProvider: AppDataProvider,
    val lockedAppsDao: LockedAppsDao,
    val appLockerPreferences: AppLockerPreferences
) : RxAwareAndroidViewModel(app) {

    val settingsViewStateLiveData = MutableLiveData<SettingsViewState>()
        .apply {
            value = SettingsViewState(
                isHiddenDrawingMode = appLockerPreferences.getHiddenDrawingMode(),
                isFingerPrintEnabled = appLockerPreferences.getFingerPrintEnabled()
            )
        }

    private val fingerPrintStatusViewStateLiveData = MutableLiveData<FingerPrintStatusViewState>()
        .apply {
            with(FingerprintIdentify(app)) {
                init()
                value = FingerPrintStatusViewState(
                    isFingerPrintSupported = isHardwareEnable,
                    isFingerPrintRegistered = isRegisteredFingerprint
                )
            }
        }

    init {
        val installedAppsObservable = appDataProvider.fetchInstalledAppList().toObservable()
        val lockedAppsObservable = lockedAppsDao.getLockedApps().toObservable()

        disposables += IsAllAppsLockedStateCreator.create(
            installedAppsObservable,
            lockedAppsObservable
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isAllAppsLocked ->
                settingsViewStateLiveData.value =
                    SettingsViewState(
                        isAllAppLocked = isAllAppsLocked,
                        isHiddenDrawingMode = appLockerPreferences.getHiddenDrawingMode(),
                        isFingerPrintEnabled = appLockerPreferences.getFingerPrintEnabled(),
                        isIntrudersCatcherEnabled = appLockerPreferences.getIntrudersCatcherEnabled()
                    )
            }
    }

    fun getSettingsViewStateLiveData(): LiveData<SettingsViewState> = settingsViewStateLiveData

    fun getFingerPrintStatusViewStateLiveData(): LiveData<FingerPrintStatusViewState> =
        fingerPrintStatusViewStateLiveData

    fun isAllLocked() = settingsViewStateLiveData.value?.isAllAppLocked ?: false

    fun isIntrudersCatcherEnabled() =
        settingsViewStateLiveData.value?.isIntrudersCatcherEnabled ?: false

    fun lockAll() {
        disposables += appDataProvider
            .fetchInstalledAppList()
            .map {
                val entityList: ArrayList<LockedAppEntity> = arrayListOf()
                it.forEach {
                    entityList.add(it.toEntity())
                }
                lockedAppsDao.lockApps(entityList)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun lockAllImportantApps() {
        disposables += appDataProvider
            .fetchInstalledAppList()
            .map {
                val entityList: ArrayList<LockedAppEntity> = arrayListOf()
                it.forEach {
                    if (it.parsePackageName().contains("gallery")){
                        entityList.add(it.toEntity())
                    }
                    when(it.parsePackageName()) {
                        "com.instagram.android" -> entityList.add(it.toEntity())
                        "com.twitter.android" -> entityList.add(it.toEntity())
                        "com.vkontakte.android" -> entityList.add(it.toEntity())
                        "com.facebook.katana" -> entityList.add(it.toEntity())
                        "com.google.android.youtube" -> entityList.add(it.toEntity())
                        "com.tinder" -> entityList.add(it.toEntity())
                        "com.whatsapp" -> entityList.add(it.toEntity())
                        "com.pinterest" -> entityList.add(it.toEntity())
                        "com.tumblr" -> entityList.add(it.toEntity())
                        "ru.mail.mailapp" -> entityList.add(it.toEntity())
                        "com.google.android.gm" -> entityList.add(it.toEntity())
                        "ru.yandex.mail" -> entityList.add(it.toEntity())
                        "com.yandex.browser" -> entityList.add(it.toEntity())
                        "com.yahoo.mobile.client.android.mail" -> entityList.add(it.toEntity())
                        "com.microsoft.office.outlook" -> entityList.add(it.toEntity())
                        "com.facebook.orca" -> entityList.add(it.toEntity())
                        "org.telegram.messenger" -> entityList.add(it.toEntity())
                        "com.viber.voip" -> entityList.add(it.toEntity())
                    }
                }
                lockedAppsDao.lockApps(entityList)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun lockApp(appLockItemViewState: AppLockItemItemViewState) {
        disposables += doOnBackground {
            lockedAppsDao.lockApp(
                LockedAppEntity(
                    appLockItemViewState.appData.packageName
                )
            )
        }
    }

    fun unlockAll() {
        disposables += doOnBackground {
            lockedAppsDao.unlockAll()
        }
    }

//    fun lockApp(lockedAppEntity: LockedAppEntity) {
//        disposables += doOnBackground {
//            lockedAppsDao.lockApp(
//                lockedAppEntity
//            )
//        }
//    }

//    fun unlockApp(packageName: String) {
//        disposables += doOnBackground {
//            lockedAppsDao.unlockApp(packageName)
//        }
//    }

    fun setHiddenDrawingMode(hiddenDrawingMode: Boolean) {
        appLockerPreferences.setHiddenDrawingMode(hiddenDrawingMode)
        val currentViewState = settingsViewStateLiveData.value
        val updatedViewState = SettingsViewState(
            isAllAppLocked = currentViewState?.isAllAppLocked ?: false,
            isHiddenDrawingMode = hiddenDrawingMode,
            isFingerPrintEnabled = appLockerPreferences.getFingerPrintEnabled(),
            isIntrudersCatcherEnabled = appLockerPreferences.getIntrudersCatcherEnabled()
        )
        settingsViewStateLiveData.value = updatedViewState
    }

    fun setEnableFingerPrint(fingerPrintEnabled: Boolean) {
        appLockerPreferences.setFingerPrintEnable(fingerPrintEnabled)
        val currentViewState = settingsViewStateLiveData.value
        val updatedViewState = SettingsViewState(
            isAllAppLocked = currentViewState?.isAllAppLocked ?: false,
            isHiddenDrawingMode = appLockerPreferences.getHiddenDrawingMode(),
            isFingerPrintEnabled = fingerPrintEnabled,
            isIntrudersCatcherEnabled = appLockerPreferences.getIntrudersCatcherEnabled()
        )
        settingsViewStateLiveData.value = updatedViewState
    }

    fun setEnableIntrudersCatchers(intruderCatcherEnabled: Boolean) {
        appLockerPreferences.setIntrudersCatcherEnable(intruderCatcherEnabled)
        val currentViewState = settingsViewStateLiveData.value
        val updatedViewState = SettingsViewState(
            isAllAppLocked = currentViewState?.isAllAppLocked ?: false,
            isHiddenDrawingMode = appLockerPreferences.getHiddenDrawingMode(),
            isFingerPrintEnabled = appLockerPreferences.getFingerPrintEnabled(),
            isIntrudersCatcherEnabled = appLockerPreferences.getIntrudersCatcherEnabled()
        )
        settingsViewStateLiveData.value = updatedViewState
    }
}