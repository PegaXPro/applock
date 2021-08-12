package com.aviapp.app.security.applocker.ui.intruders

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aviapp.app.security.applocker.data.AppDataProvider
import com.aviapp.app.security.applocker.data.AppLockerPreferences
import com.aviapp.app.security.applocker.data.database.lockedapps.LockedAppsDao
import com.aviapp.app.security.applocker.ui.RxAwareViewModel
import com.aviapp.app.security.applocker.ui.settings.IsAllAppsLockedStateCreator
import com.aviapp.app.security.applocker.ui.settings.SettingsViewState
import com.aviapp.app.security.applocker.util.extensions.plusAssign
import com.aviapp.app.security.applocker.util.helper.file.FileExtension
import com.aviapp.app.security.applocker.util.helper.file.FileManager
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

class IntrudersPhotosViewModel @Inject constructor(
    val app: Application,
    val fileManager: FileManager,
    val appLockerPreferences: AppLockerPreferences,
    val appDataProvider: AppDataProvider,
    val lockedAppsDao: LockedAppsDao,
    ) :
    RxAwareViewModel() {

    private val intruderListViewState = MutableLiveData<IntrudersViewState>()


    val settingsViewStateLiveData = MutableLiveData<SettingsViewState>()
        .apply {
            value = SettingsViewState(
                isHiddenDrawingMode = appLockerPreferences.getHiddenDrawingMode(),
                isFingerPrintEnabled = appLockerPreferences.getFingerPrintEnabled()
            )
        }



    init {
        loadIntruderPhotos()

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



    fun getIntruderListViewState(): LiveData<IntrudersViewState> =
        intruderListViewState

    fun loadIntruderPhotos() {
        val subFilesObservable = Single.create<List<File>> {
            val subFiles = fileManager.getSubFiles(
                fileManager.getExternalDirectory(FileManager.SubFolder.INTRUDERS),
                FileExtension.JPEG
            )
            it.onSuccess(subFiles)
        }

        disposables += subFilesObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { files ->
                    intruderListViewState.value = IntrudersViewState(mapToViewState(files))
                },
                { error -> Log.v("TEST", "error : ${error.message}") })
    }

    private fun mapToViewState(files: List<File>): List<IntruderPhotoItemViewState> {
        val viewStateList = arrayListOf<IntruderPhotoItemViewState>()
        files.forEach { viewStateList.add((IntruderPhotoItemViewState(it))) }
        return viewStateList
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