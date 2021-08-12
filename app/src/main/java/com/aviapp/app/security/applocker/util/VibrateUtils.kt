package com.aviapp.app.security.applocker.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object VibrateUtils {

    fun vibrate(context: Context) {
        val v = (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(
                VibrationEffect.createOneShot(200,
                VibrationEffect.DEFAULT_AMPLITUDE))
        }
        else {
            v.vibrate(200)
        }
    }

}