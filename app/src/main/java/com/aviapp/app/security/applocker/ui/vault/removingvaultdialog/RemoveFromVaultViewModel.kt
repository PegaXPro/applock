package com.aviapp.app.security.applocker.ui.vault.removingvaultdialog

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.aviapp.app.security.applocker.data.database.vault.VaultMediaEntity
import com.aviapp.app.security.applocker.repository.VaultRepository
import com.aviapp.app.security.applocker.ui.RxAwareAndroidViewModel
import com.aviapp.app.security.applocker.ui.vault.addingvaultdialog.ProcessState
import com.aviapp.app.security.applocker.util.encryptor.CryptoProcess
import com.aviapp.app.security.applocker.util.extensions.plusAssign
import com.aviapp.app.security.applocker.util.helper.file.MediaScannerConnector
import com.aviapp.app.security.applocker.util.helper.progress.FakeProgress
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class RemoveFromVaultViewModel @Inject constructor(
    private val app: Application,
    private val vaultRepository: VaultRepository
) : RxAwareAndroidViewModel(app) {

    private val removeFromVaultProcessLiveData = MutableLiveData<CryptoProcess>()

    private val fakeProgress = FakeProgress()

    private val removeFromVaultViewStateLiveData =
        MutableLiveData<RemoveFromVaultViewState>()
            .apply {
                value =
                    RemoveFromVaultViewState(progress = 0, processState = ProcessState.PROCESSING)
            }


    init {
        with(fakeProgress) {
            setOnProgressListener {
                removeFromVaultViewStateLiveData.value =
                    RemoveFromVaultViewState(it, ProcessState.PROCESSING)
            }

            setOnCompletedListener {
                removeFromVaultViewStateLiveData.value =
                    RemoveFromVaultViewState(100, ProcessState.COMPLETE)
            }
        }
    }


    fun getMediaFromVault(vaultMediaEntity: VaultMediaEntity) {
        val decryptionObservable = vaultRepository
            .getMediaFileFromVault(vaultMediaEntity)
            .doOnComplete { refreshFileSystem(vaultMediaEntity.originalPath) }

        disposables += decryptionObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { fakeProgress.start() }
            .subscribe({
                    when (it) {
                        is CryptoProcess.Complete -> {
                            fakeProgress.complete()
                        }
                    }
                },
                {

                })
    }


    fun removeMediaFromVault(vaultMediaEntity: VaultMediaEntity) {
        val decryptionObservable = vaultRepository
            .removeMediaFromVault(vaultMediaEntity)
            .doOnComplete { refreshFileSystem(vaultMediaEntity.originalPath) }

        disposables += decryptionObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { fakeProgress.start() }
            .subscribe(
                {
                    when (it) {
                        is CryptoProcess.Complete -> fakeProgress.complete()
                    }
                },
                {  })
    }

    fun getRemoveFromVaultViewStateLiveData() = removeFromVaultViewStateLiveData

    private fun refreshFileSystem(originalPath: String) {
        MediaScannerConnector.scan(app, originalPath)
    }
}