package com.aviapp.app.security.applocker.ui.background

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

data class GradientItemViewState(
    val id: Int, @DrawableRes val gradientBackgroundRes: Int,
    var isChecked: Boolean = false,
    var prem: Boolean = false,
    val firebaseRef: String? = null
) : BackgroundItem {

    fun getGradiendDrawable(context: Context): Drawable? {
        return ContextCompat.getDrawable(context, gradientBackgroundRes)
    }
    fun isCheckedVisible(): Int = if (isChecked) View.VISIBLE else View.INVISIBLE
    fun showPrem(): Int = View.INVISIBLE
}