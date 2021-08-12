package com.aviapp.app.security.applocker.service.stateprovider

import android.app.ActivityManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.aviapp.app.security.applocker.data.SystemPackages
import com.aviapp.app.security.applocker.data.database.lockedapps.LockedAppsDao
import com.aviapp.app.security.applocker.ui.overlay.activity.OverlayValidationActivity
import com.aviapp.app.security.applocker.ui.permissions.PermissionChecker
import com.aviapp.app.security.applocker.util.extensions.plusAssign
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AppForegroundObservable @Inject constructor(val context: Context, val lockedAppsDao: LockedAppsDao) {

    private var foregroundFlowable: Flowable<String>? = null
    private val restrictedPackages: HashSet<String> = HashSet()
    private var currentActivity: String = "android"
    private val TAG: String = "AppForegroundObservable"
    private val allDisposables: CompositeDisposable = CompositeDisposable()
    private val lockedAppPackageSet: HashSet<String> = HashSet()

    private fun observeLockedApps() {

        allDisposables += lockedAppsDao.getLockedApps()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { lockedAppList ->
                            lockedAppPackageSet.clear()
                            Log.d(TAG+"##", "observeLockedApps()")
                            lockedAppList.forEach { lockedAppPackageSet.add(it.parsePackageName()) }
                            SystemPackages.getSystemPackages().forEach { lockedAppPackageSet.add(it) }
                        },
                        { error -> })
    }

    fun get(): Flowable<String> {
        foregroundFlowable = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> getForegroundObservableHigherLollipop()
            else -> getForegroundObservableLowerLollipop()
        }
        return foregroundFlowable!!
    }

    init {
        observeLockedApps()
        restrictedPackages.add("com.android.packageinstaller")
        restrictedPackages.add("com.google.android.packageinstaller")
    }

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager?

    var bloakApplastTimeUpdate = 0L
    var blocAppName: String = ""

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getForegroundObservableHigherLollipop(): Flowable<String> {
        return Flowable.interval(100, TimeUnit.MILLISECONDS)
            .filter { PermissionChecker.checkUsageAccessPermission(context) }
            .map {
                val time = System.currentTimeMillis()
                val appList = usageStatsManager?.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    time - 1000 * 1000, time
                )

                val newMap = appList?.sortedBy {
                    it.lastTimeUsed
                }
                var currentApp = newMap?.lastOrNull()

                val appName = currentApp?.packageName?: "android"
                val appTime = currentApp?.lastTimeUsed?: 0L


                if (lockedAppPackageSet.contains(appName)) {
                    bloakApplastTimeUpdate = time
                    blocAppName = appName
                }
                UsageEventWrapper(appName, appTime)
            }
            //.filter { it.usageEvent != context.packageName }
            .filter { cheAppStatus(context, it.usageEvent)}
                .filter {
                    !((it.usageEvent != blocAppName) && (it.lastTimeUpdate - bloakApplastTimeUpdate) < 500)
                }
            .map {
                var currentPackage: String = it.usageEvent

                Log.d(TAG, "Current App: $currentPackage")
                if (restrictedPackages.contains(currentPackage)) {
                    Log.d(TAG, "Current App is in restricted packages")
                    currentPackage = currentActivity(context, currentPackage)
                    Log.d(TAG, "Current Activity $currentPackage")
                    if (restrictedPackages.contains(currentPackage)) {
                        Log.d(TAG, "Nazaaaam.........>>>")
                    } else {
                        currentActivity = currentPackage
                        Log.d(TAG, "current activity cashed $currentActivity")
                    }

                } else {
                    currentActivity = currentPackage
                    Log.d(TAG, "else part current activity $currentActivity")
                }
                currentActivity
            }
            .distinctUntilChanged()
    }

    private fun currentActivity(context: Context, currentPackage: String): String {

        var currentActivity: String = currentPackage
        var usageEvent: UsageEvents.Event? = null

        val mUsageStatsManager =
            context.getSystemService(Service.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()

        val usageEvents = mUsageStatsManager.queryEvents(time - 1000 * 3600, time)
        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                usageEvent = event
            }
        }
        if (usageEvent != null && usageEvent.className != null) {
            currentActivity = usageEvent.className
        }
        return currentActivity
    }

    private fun cheAppStatus(context: Context, str: String): Boolean {
        if (str == context.packageName) {
            val mActivityManager =
                context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val cmpName: String = mActivityManager.getRunningTasks(1)[0].topActivity!!.className
            if (cmpName.contains(OverlayValidationActivity::class.java.simpleName)) {
                Log.d(TAG, "in condition $cmpName")
                return false
            }
        }
        return true
    }

    private fun getForegroundObservableLowerLollipop(): Flowable<String> {
        return Flowable.interval(100, TimeUnit.MILLISECONDS)
            .map {
                val mActivityManager =
                    context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
                mActivityManager.getRunningTasks(1)[0].topActivity
            }
            .filter {
                it.className.contains(OverlayValidationActivity::class.java.simpleName).not()
            }
            .map { it.packageName }
            .distinctUntilChanged()
    }
}