package com.aviapp.app.security.applocker.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.aviapp.app.security.applocker.data.database.vault.VaultMediaDao
import com.aviapp.app.security.applocker.data.database.vault.VaultMediaEntity
import com.aviapp.app.security.applocker.data.database.vault.VaultMediaType
import com.aviapp.app.security.applocker.data.database.vault.VaultMediaType.TYPE_IMAGE
import com.aviapp.app.security.applocker.data.database.vault.VaultMediaType.TYPE_VIDEO
import com.aviapp.app.security.applocker.util.encryptor.CryptoProcess
import com.aviapp.app.security.applocker.util.encryptor.EncryptFileOperationRequest
import com.aviapp.app.security.applocker.util.encryptor.FileEncryptor
import com.aviapp.app.security.applocker.util.extensions.doOnBackground
import com.aviapp.app.security.applocker.util.helper.file.*
import com.facebook.crypto.Entity
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.io.File
import javax.inject.Inject

class VaultRepository @Inject constructor(
    val context: Context,
    private val vaultMediaDao: VaultMediaDao,
    private val fileEncryptor: FileEncryptor,
    private val fileManager: FileManager,
    private val previewCreator: PreviewCreator
) {

    fun getVaultImages(): Flowable<List<VaultMediaEntity>> {
        return vaultMediaDao.getVaultImages()
            .flatMap { t: List<VaultMediaEntity> ->
                Flowable.fromIterable(t)
                    .flatMap { vaultMediaEntity: VaultMediaEntity -> Flowable.just(vaultMediaEntity) }
                    .flatMap {
                        Log.d("SDFGSGESG", "!!!!! $it")
                        getPreviewFile(it)
                    }
                    .toList()
                    .toFlowable()
            }
    }

    fun getVaultVideos(): Flowable<List<VaultMediaEntity>> {
        return vaultMediaDao.getVaultVideos()
            .flatMap { t: List<VaultMediaEntity> ->
                Flowable.fromIterable(t)
                    .flatMap { vaultMediaEntity: VaultMediaEntity -> Flowable.just(vaultMediaEntity) }
                    .doOnNext { getPreviewFile(it) }
                    .toList()
                    .toFlowable()
            }
    }

    fun addMediaToVault(
        imageFilePath: String,
        mediaType: VaultMediaType
    ): Observable<CryptoProcess> {

        return Observable.create { emitter ->

            val originalFile = File(imageFilePath)

            val encryptedFileName = createFileName(mediaType)

            val encryptFileOperationRequest = EncryptFileOperationRequest(
                fileName = encryptedFileName,
                fileExtension = FileExtension.NONE,
                directoryType = DirectoryType.EXTERNAL
            )

            val originalEncryptionObservable =
                fileEncryptor.encrypt(originalFile, encryptFileOperationRequest)

            val previewEncryptionObservable =
                getPreviewEncryptionObservable(originalFile, encryptedFileName, mediaType)

            Observable.combineLatest(originalEncryptionObservable,
                previewEncryptionObservable
            ) { originalProcess, previewProcess ->
                if (originalProcess is CryptoProcess.Complete && previewProcess is CryptoProcess.Complete) {
                    val vaultMediaEntity = VaultMediaEntity(
                        originalPath = imageFilePath,
                        originalFileName = originalFile.name,
                        encryptedPath = originalProcess.file.absolutePath,
                        encryptedPreviewPath = previewProcess.file.absolutePath,
                        mediaType = mediaType.mediaType
                    )


                    Log.d("LKJNRRIOVN", " $vaultMediaEntity")

                    doOnBackground { vaultMediaDao.addToVault(vaultMediaEntity) }

                }

                var totalProgress = 0

                totalProgress += when (originalProcess) {
                    is CryptoProcess.Processing -> originalProcess.percentage
                    else -> 100
                }

                totalProgress += when (previewProcess) {
                    is CryptoProcess.Processing -> previewProcess.percentage
                    else -> 100
                }

                when {
                    originalProcess is CryptoProcess.Complete && previewProcess is CryptoProcess.Complete -> originalProcess
                    else -> {
                        CryptoProcess.processing(totalProgress / 2)
                    }
                }
            }
                .subscribe(
                    {
                        when (it) {
                            is CryptoProcess.Processing -> emitter.onNext(it)
                            is CryptoProcess.Complete -> {
                                emitter.onNext(it)
                                emitter.onComplete()
                            }
                        }
                    },
                    {
                        emitter.onComplete()
                    })

        }
    }

    fun getOrigFile(path: String): Bitmap {

        val encryptFile = File(path)
        val fileCryptoInputStream = fileEncryptor.getDecrStr(encryptFile)

        val bitmap = BitmapFactory.decodeStream(fileCryptoInputStream)
        return bitmap

        /*val available = fileCryptoInputStream.available()
        val buffer = ByteArray(1024)
        var read: Int = fileCryptoInputStream.read(buffer)
        var totalRead: Long = read.toLong()



        while (read != -1) {
            fileOutputStream.write(buffer, 0, read)
            read = fileCryptoInputStream.read(buffer)

            totalRead += read
            val percent = (totalRead * 100 / available).toInt()
            it.onNext(CryptoProcess.processing(percent))
        }*/

    }

    fun getMediaFileFromVault(vaultMediaEntity: VaultMediaEntity): Observable<CryptoProcess> {
        val encryptFile = File(vaultMediaEntity.encryptedPath)
        val decryptFile = File(vaultMediaEntity.originalPath)
        return fileEncryptor.decryptFileWithFile(encryptFile, decryptFile).doOnComplete {
            doOnBackground {

                //vaultMediaDao.removeFromVault(vaultMediaEntity.originalPath)
            }
        }
    }


    fun removeMediaFromVault(vaultMediaEntity: VaultMediaEntity): Observable<CryptoProcess> {
        val encryptFile = File(vaultMediaEntity.encryptedPath)
        val decryptFile = File(vaultMediaEntity.originalPath)
        return fileEncryptor.decryptFile(encryptFile, decryptFile).doOnComplete {
            doOnBackground { vaultMediaDao.removeFromVault(vaultMediaEntity.originalPath) }
        }
    }

    private fun getPreviewEncryptionObservable(originalFile: File, fileName: String, mediaType: VaultMediaType): Observable<CryptoProcess> {

        val previewFileName = createPreviewFileName(fileName)

        val previewCacheFileRequest = FileOperationRequest(
            fileName = previewFileName,
            directoryType = DirectoryType.CACHE,
            fileExtension = FileExtension.JPEG
        )

        val previewFile = when (mediaType) {
            TYPE_IMAGE -> previewCreator.createPreviewImage(originalFile, previewCacheFileRequest)
            TYPE_VIDEO -> previewCreator.createPreviewVideo(originalFile, previewCacheFileRequest)
        }

        val encryptFileOperationRequest = EncryptFileOperationRequest(
            fileName = previewFileName,
            directoryType = DirectoryType.EXTERNAL,
            fileExtension = FileExtension.NONE
        )

        return fileEncryptor.encrypt(previewFile, encryptFileOperationRequest)
    }

    private fun createFileName(mediaType: VaultMediaType): String {
        return if (mediaType == TYPE_IMAGE) createEncryptedImageFileName() else createEncryptedVideoFileName()
    }

    private fun createEncryptedImageFileName(): String {
        return PREFIX_ENCRYPT_IMAGE + System.currentTimeMillis()
    }

    private fun createEncryptedVideoFileName(): String {
        return PREFIX_ENCRYPT_VIDEO + System.currentTimeMillis()
    }

    private fun createPreviewFileName(encryptedFileName: String): String {
        return "${PREFIX_PREVIEW}_$encryptedFileName"
    }


    fun createPreviewFile1(vaultMediaEntity: VaultMediaEntity): Flowable<VaultMediaEntity> {

        val encryptFileOperationRequest = EncryptFileOperationRequest(
            vaultMediaEntity.getEncryptedPreviewFileNameBig(),
            FileExtension.JPEG,
            DirectoryType.CACHE
        )
        Log.d("SDFSGSEGG", "!!!!!! ${vaultMediaEntity.getEncryptedPreviewFileNameBig()}")

        return Flowable.create({ subscriber ->
            fileEncryptor
                .decryptFile(
                    File(vaultMediaEntity.encryptedPath),
                    encryptFileOperationRequest
                )
                .subscribe { cryptoProcess ->
                    when (cryptoProcess) {
                        is CryptoProcess.Complete -> {

                            Log.d("SDFSGSEGG", "!!!!!! ${ cryptoProcess.file.absolutePath}")

                            vaultMediaEntity.decryptedPreviewCachePath =
                                cryptoProcess.file.absolutePath
                            subscriber.onNext(vaultMediaEntity)
                            subscriber.onComplete()
                        }
                    }
                }
        }, BackpressureStrategy.DROP)
    }



    private fun createPreviewFile(vaultMediaEntity: VaultMediaEntity): Flowable<VaultMediaEntity> {
        val encryptFileOperationRequest = EncryptFileOperationRequest(
            vaultMediaEntity.getEncryptedPreviewFileName(),
            FileExtension.JPEG,
            DirectoryType.CACHE
        )

        return Flowable.create({ subscriber ->
            fileEncryptor
                .decryptFile(
                    File(vaultMediaEntity.encryptedPreviewPath),
                    encryptFileOperationRequest
                )
                .subscribe { cryptoProcess ->
                    when (cryptoProcess) {
                        is CryptoProcess.Complete -> {
                            vaultMediaEntity.decryptedPreviewCachePath =
                                cryptoProcess.file.absolutePath
                            subscriber.onNext(vaultMediaEntity)
                            subscriber.onComplete()
                        }
                    }
                }
        }, BackpressureStrategy.DROP)
    }

    private fun getPreviewFile(vaultMediaEntity: VaultMediaEntity): Flowable<VaultMediaEntity> {
        val previewCacheFile = fileManager.getFile(
            FileOperationRequest(
                vaultMediaEntity.getEncryptedPreviewFileName(),
                FileExtension.JPEG,
                DirectoryType.CACHE
            ), FileManager.SubFolder.VAULT
        )

        return when {
            previewCacheFile?.exists() == true -> {
                vaultMediaEntity.decryptedPreviewCachePath = previewCacheFile.absolutePath
                Flowable.just(vaultMediaEntity)
            }
            else -> {
                createPreviewFile(vaultMediaEntity)
            }
        }
    }

    companion object {

        private const val PREFIX_ENCRYPT_IMAGE = "EIF"
        private const val PREFIX_ENCRYPT_VIDEO = "EVF"
        private const val PREFIX_PREVIEW = "PREVIEW"
    }
}