package com.aviapp.app.security.applocker.service.stateprovider

import android.content.Context
import com.aviapp.app.security.applocker.ui.permissions.PermissionChecker
import io.reactivex.Flowable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PermissionCheckerObservable @Inject constructor(val context: Context) {

    fun get(): Flowable<Boolean> {
        return Flowable.interval(1, TimeUnit.MINUTES)
            .map { PermissionChecker.isAllPermissionChecked(context).not() }
    }
}