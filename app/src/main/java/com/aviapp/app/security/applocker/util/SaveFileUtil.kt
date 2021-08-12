package com.aviapp.app.security.applocker.util

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.aviapp.app.security.applocker.util.permissions.CheckWriteStorage
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File


object SaveFileUtil {

    val storage = Firebase.storage

    private fun getFileUri(context: Context, fileName: String): CompletableDeferred<Uri?> {

        val completeD = CompletableDeferred<Uri?>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){

            CheckWriteStorage.check(context as Activity) {

                if (it) {
                    val resolver = context.contentResolver
                    val values = ContentValues()
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    values.put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + File.separator + "theme"
                    )

                    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    completeD.complete(imageUri)
                } else {
                    completeD.complete(null)
                }

            }
        } else {
            val completeD = CompletableDeferred<Uri?>()

            CheckWriteStorage.check(context as Activity) {
                    if (it) {
                        val dirPath =
                            Environment.getExternalStorageDirectory().toString() + "/theme"
                        if (!dirExists(dirPath)) {
                            val directory = File(dirPath)
                            directory.mkdirs()
                            directory.mkdir()
                        }
                        val filePath = dirPath + fileName
                        completeD.complete(Uri.parse(filePath))
                    } else {
                        completeD.complete(null)
                    }
            }
        }
        return completeD;
    }


    private fun getFile(c: Context, fileName: String): File? {
        val resolver = c.contentResolver ?: return null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, null, null, null)?: return null
        val count: Int = cursor.count
        for (i: Int in (0..count)) {
            if (!cursor.moveToPosition(i)) {
                cursor.close()
                return null
            }
            val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val path: String = cursor.getString(column_index)
            if (path.endsWith(fileName)) {
                return File(path);
            }
        }
        cursor.close()
        return null;
    }


    private fun ContentResolver.getBitmap(selectedPhotoUri: Uri): Bitmap? {


        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(this, selectedPhotoUri)
            ImageDecoder.decodeBitmap(source)
        } else {

            MediaStore.Images.Media.getBitmap(
                this,
                selectedPhotoUri
            )

        }
    }

    fun getBitmap(filename: String, context: Context): Bitmap? {
        val p0 = getFile(context, fileName = filename) ?: return null
        val resolver = context.contentResolver
        return resolver.getBitmap(Uri.fromFile(p0))
    }


    suspend fun saveFileWithRequest(activity: Activity, fileName: String, scope: CoroutineScope): Flow<StateBack> {

        val p1 = CompletableDeferred<Flow<StateBack>>()
        CheckWriteStorage.check(activity) {
            val p0 = if (it) {
                saveImage(activity, fileName, scope)
            } else {
                flow<StateBack> { emit(StateBack.Error("request")) }
            }
            p1.complete(p0)
        }
        return p1.await();
    }


     fun saveImage(context: Context,  fileName: String, scope: CoroutineScope) =  flow {
         val storageRef = storage.reference
         val pathReference = storageRef.child(fileName)
         emit(StateBack.Downloading)
         val file = getFile(context, fileName)
         if (file == null) {
             val localFile = File.createTempFile("temp_image", "jpg")
             val rez = CompletableDeferred<StateBack>()
             pathReference.getFile(localFile).addOnSuccessListener {

                 scope.launch(Dispatchers.IO) {
                     val fileUri = getFileUri(context, fileName).await()
                     if (fileUri == null) {
                         rez.complete(StateBack.Error("fileUri == null"))
                     } else {
                         val resolver = context.contentResolver
                         val output = resolver.openOutputStream(fileUri)

                         val input = localFile.inputStream()

                         val data = ByteArray(1024)
                         var total: Long = 0
                         var count = 0

                         while (input.read(data).also { count = it } != -1) {
                             total += count.toLong()
                             output?.write(data, 0, count)
                         }

                         output?.flush()
                         output?.close()
                         input.close()

                         val bitmap = resolver.getBitmap(Uri.fromFile(localFile))
                         if (bitmap == null) {
                             rez.complete(StateBack.Error("bitmap == null"))
                         } else {
                             rez.complete(StateBack.Success(bitmap))
                         }
                     }
                 }

             }.addOnFailureListener {
                 rez.complete(StateBack.Error(it.message?:""))
             }
             emit(rez.await())
         } else {
             val resolver = context.contentResolver
             val bitmap = resolver.getBitmap(Uri.fromFile(file))

             if (bitmap == null) {
                 emit(StateBack.Error("bitmap == null"))
             } else {
                 emit(StateBack.Success(bitmap))
             }
         }
    }


    private fun getBitmapFormUri(file: File, mContext: Context): Bitmap? {
        return try {
            MediaStore.Images.Media.getBitmap(mContext.contentResolver, Uri.fromFile(file))
        } catch (e: Throwable) {
            null
        }
    }

    sealed class StateBack {
        object Downloading : StateBack()
        object PermD:StateBack()
        data class Error (val message: String): StateBack()
        data class Success(val bitmap: Bitmap): StateBack()
    }


    private fun dirExists(dir_path: String?): Boolean {
        var ret = false
        val dir = File(dir_path)
        if (dir.exists() && dir.isDirectory) ret = true
        return ret
    }

}