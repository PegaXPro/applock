package com.aviapp.app.security.applocker

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.multidex.MultiDex
import androidx.work.Configuration
import androidx.work.WorkManager
import com.aviapp.app.security.applocker.di.component.DaggerAppComponent
import com.aviapp.app.security.applocker.repository.CallBlockerRepository
import com.aviapp.app.security.applocker.service.ServiceStarter
import com.aviapp.app.security.applocker.service.worker.WorkerBlocker
import com.aviapp.app.security.applocker.service.worker.WorkerStarter
import com.aviapp.app.security.applocker.util.OpenAppBlock
import com.facebook.soloader.SoLoader
import com.facebook.stetho.Stetho
import com.raqun.beaverlib.Beaver
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import io.paperdb.Paper
import javax.inject.Inject

class AppLockerApplication : DaggerApplication() , LifecycleObserver , Application.ActivityLifecycleCallbacks {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerAppComponent.builder().create(this)

    var wasInBackground = false

    @Inject
    lateinit var callBlockerRepository: CallBlockerRepository

    override fun onCreate() {
        super.onCreate()

        OpenAppBlock(this)
        Stetho.initializeWithDefaults(this)
        Beaver.build(this)
        ServiceStarter.startService(this)
        SoLoader.init(this, false)

        try {
            WorkerStarter.startServiceCheckerWorker(this)
            WorkerBlocker.startCallBlockerWorker(this)
        } catch (e: Throwable) {
            Log.d("WORK_MANAGER_ERROR", "error: $e")
        }

        Paper.init(this)
        registerActivityLifecycleCallbacks(this)
    }


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }



    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        wasInBackground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        wasInBackground = false
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
    override fun onActivityStarted(p0: Activity) {
        Log.d("CurrentActivity", "!!!!! ${p0::class.java.simpleName}")
    }
    override fun onActivityResumed(p0: Activity) {}
    override fun onActivityPaused(p0: Activity) {}
    override fun onActivityStopped(p0: Activity) {}
    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
    override fun onActivityDestroyed(p0: Activity) {}
}