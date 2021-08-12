package com.aviapp.app.security.applocker.ui.vault.addingvaultdialog

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aviapp.app.security.applocker.data.database.vault.VaultMediaType
import com.aviapp.app.security.applocker.repository.VaultRepository
import com.aviapp.app.security.applocker.ui.RxAwareAndroidViewModel
import com.aviapp.app.security.applocker.util.encryptor.CryptoProcess
import com.aviapp.app.security.applocker.util.extensions.plusAssign
import com.aviapp.app.security.applocker.util.helper.file.FileManager
import com.aviapp.app.security.applocker.util.helper.file.MediaScannerConnector
import com.aviapp.app.security.applocker.util.helper.progress.FakeProgress
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import kotlin.collections.ArrayList

class AddToVaultViewModel @Inject constructor(
    private val app: Application,
    private val vaultRepository: VaultRepository,
    private val fileManager: FileManager
) : RxAwareAndroidViewModel(app) {

    private val fakeProgress = FakeProgress()

    private val addToVaultViewStateLiveData = MutableLiveData<AddToVaultViewState>()
        .apply {
            value = AddToVaultViewState(progress = 0, processState = ProcessState.PROCESSING)
        }

    init {
        with(fakeProgress) {
            setOnProgressListener {
                addToVaultViewStateLiveData.value = AddToVaultViewState(it, ProcessState.PROCESSING)
            }

            setOnCompletedListener {
                addToVaultViewStateLiveData.value = AddToVaultViewState(100, ProcessState.COMPLETE)
            }
        }
    }

    fun setSelectedFilePath(selectedFilePath: ArrayList<String>, mediaType: VaultMediaType) {

        val list = arrayListOf<Observable<CryptoProcess>>()

        for (selected in selectedFilePath) {
            val encryptionObservable = vaultRepository
                .addMediaToVault(selected, mediaType)
                .doOnNext {
                    if (it is CryptoProcess.Complete) {
                        deleteAndRefreshFileSystem(selected)
                    }
                }

            list.add(encryptionObservable)
        }
        disposables += Observable.merge(list)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { fakeProgress.start() }
            .doOnComplete { fakeProgress.complete() }
            .subscribe({}, {  })
    }

    fun getAddToVaultViewStateLiveData(): LiveData<AddToVaultViewState> =
        addToVaultViewStateLiveData

    private fun deleteAndRefreshFileSystem(filePath: String) {
        disposables += fileManager.deleteFile(filePath)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    try {
                        MediaScannerConnector.scan(context = app, filePath = filePath)
                    } catch (e: Throwable) { }

                },
                {  })
    }
}