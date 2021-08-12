package com.aviapp.app.security.applocker.ui.security

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.AppData

data class AppLockItemItemViewState(val appData: AppData, var isLocked: Boolean = false) : AppLockItemBaseViewState() {

    fun appName() = appData.appName

    fun getLockIcon(context: Context): Drawable? {
        return if (isLocked) {
            ContextCompat.getDrawable(context, R.drawable.ic_locked_24px)
        } else {
            ContextCompat.getDrawable(context, R.drawable.ic_lock_open_24px)
        }
    }

    fun getAppStatus(context: Context): String? {
        return if (isLocked) {
            context.getString(R.string.status_protected)
        } else {
            context.getString(R.string.non_protected)
        }
    }

    fun getAppIcon(): Drawable? = appData.appIconDrawable

}