package com.aviapp.app.security.applocker.ui.intruders

import com.aviapp.app.security.applocker.util.binding.ImageSize
import java.io.File

data class IntruderPhotoItemViewState(val file: File) {

    fun getFilePath() = file.absolutePath

    fun getImageSize(): ImageSize = ImageSize.MEDIUM
}