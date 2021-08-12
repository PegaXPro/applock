package com.aviapp.app.security.applocker.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.aviapp.app.security.applocker.AppLockerApplication
import com.aviapp.app.security.applocker.ui.newpattern.CreateNewPatternActivity
import com.aviapp.app.security.applocker.ui.overlay.activity.OverlayValidationActivity
import com.aviapp.app.security.applocker.ui.permissions.PermissionsActivity
import com.aviapp.app.security.applocker.ui.splash.SplashActivity
import com.aviapp.app.security.applocker.ui.vault.VaultActivity


class OpenAppBlock(private val myApplication: AppLockerApplication) : Application.ActivityLifecycleCallbacks,
    LifecycleObserver {

    private var currentActivity: Activity? = null


    init {
        myApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this);
    }

    fun logPr(message: String) {
        Log.d("SDFESFSER", message)
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        logPr("onActivityCreated ${p0.localClassName}")
    }

    override fun onActivityStarted(p0: Activity) {
        logPr("onActivityStarted ${p0.localClassName}")
        currentActivity = p0;
    }

    override fun onActivityResumed(p0: Activity) {
        logPr("onActivityResumed ${p0.localClassName}")
        currentActivity = p0;
    }

    override fun onActivityPaused(p0: Activity) {
        logPr("onActivityPaused ${p0.localClassName}")
    }

    override fun onActivityStopped(p0: Activity) {
        logPr("onActivityStopped ${p0.localClassName}")
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        logPr("onActivitySaveInstanceState ${p0.localClassName}")
    }

    override fun onActivityDestroyed(p0: Activity) {
        logPr("onActivityDestroyed ${p0.localClassName}")
        currentActivity = null;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {

        currentActivity?: return

        if (currentActivity !is SplashActivity && currentActivity !is PermissionsActivity && currentActivity !is CreateNewPatternActivity && currentActivity !is VaultActivity) {
            val intent = OverlayValidationActivity.newIntent(currentActivity!!, "com.app.lock.password.applocker")
            currentActivity?.startActivity(intent)
        }
        logPr("OnLifecycleEvent(Lifecycle.Event.ON_START)")
    }

}