package com.aviapp.app.security.applocker.ui.intruders.camera

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.aviapp.app.security.applocker.util.helper.file.MediaScannerConnector
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream


class FrontPictureLiveData(val app: Application, private val destinationImageFile: File) :
    MutableLiveData<FrontPictureState>() {

    private enum class State {
        IDLE, TAKING, TAKEN, ERROR
    }

    private var camera: Camera? = null

    private var state: State = State.IDLE


    override fun onInactive() {
        super.onInactive()
        stopCamera()
    }

    fun takePicture() {
        try {
            if (state == State.TAKEN || state == State.TAKING) {
                return
            }

            state = State.TAKING
            startCamera()
            camera?.takePicture(
                null,
                null,
                Camera.PictureCallback { data, camera -> savePicture(data) })
        } catch (e: Exception) {}

    }

    private fun startCamera() {
        val dummy = SurfaceTexture(0)

        try {
            val cameraId = getFrontCameraId()
            if (cameraId == NO_CAMERA_ID) {
                value = FrontPictureState.Error(IllegalStateException("No front camera found"))
                state = State.ERROR
                return
            }
            camera = Camera.open(cameraId).also {
                it.setPreviewTexture(dummy)
                it.startPreview()
            }
            value = FrontPictureState.Started()
        } catch (e: RuntimeException) {
            value = FrontPictureState.Error(e)
            state = State.ERROR
        }
    }


/*    private fun startCamera1():String {

        try {
            manager?:
                (app.getSystemService(Context.CAMERA_SERVICE) as CameraManager).also { manager = it }
            for (id in manager!!.cameraIdList) {
                val cameraCharacteristics =
                    manager?.getCameraCharacteristics(id)
                //Seek frontal camera.
                if (cameraCharacteristics?.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    Log.i("LKSJDFLKS", "Camara frontal id $id")
                    return id
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return "0"
    }*/

    private fun stopCamera() {
        camera?.stopPreview()
        camera?.release()
        camera = null
        value = FrontPictureState.Destroyed()
    }

    private fun getFrontCameraId(): Int {
        var camId =
            NO_CAMERA_ID
        val numberOfCameras = Camera.getNumberOfCameras()
        val ci = Camera.CameraInfo()

        for (i in 0 until numberOfCameras) {
            Camera.getCameraInfo(i, ci)
            if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                camId = i
            }
        }

        return camId
    }

    @SuppressLint("CheckResult")
    private fun savePicture(data: ByteArray) {

        Log.d("lsJDNRJRVNLSkjsldk", "!!!!!!!!!")

        Single
            .create<String> { emitter ->
                try {
                    val pictureFile = destinationImageFile
                    pictureFile?.let {
                        val fos = FileOutputStream(pictureFile)
                        fos.write(data)
                        fos.close()
                        emitter.onSuccess(it.absolutePath)
                    }
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(
                { filePath ->
                    state = State.TAKEN
                    value = FrontPictureState.Taken(filePath)
                    refreshFileSystem(filePath)
                },
                {
                    state = State.ERROR
                    value = FrontPictureState.Error(it)
                })
    }

    private fun refreshFileSystem(originalPath: String) {

        MediaScannerConnector.scan(app, originalPath)
    }

    companion object {
        private const val NO_CAMERA_ID = -1
    }

}