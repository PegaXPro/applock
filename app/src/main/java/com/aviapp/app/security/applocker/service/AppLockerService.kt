package com.aviapp.app.security.applocker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationManagerCompat
import com.andrognito.patternlockview.PatternLockView
import com.aviapp.ads.AdHelper
import com.aviapp.app.security.applocker.data.AppLockerPreferences
import com.aviapp.app.security.applocker.data.SystemPackages
import com.aviapp.app.security.applocker.data.database.lockedapps.LockedAppsDao
import com.aviapp.app.security.applocker.data.database.pattern.PatternDao
import com.aviapp.app.security.applocker.data.database.pattern.PatternDot
import com.aviapp.app.security.applocker.service.notification.ServiceNotificationManager
import com.aviapp.app.security.applocker.service.stateprovider.AppForegroundObservable
import com.aviapp.app.security.applocker.service.stateprovider.PermissionCheckerObservable
import com.aviapp.app.security.applocker.ui.overlay.activity.OverlayValidationActivity
import com.aviapp.app.security.applocker.ui.overlay.view.OverlayViewLayoutParams
import com.aviapp.app.security.applocker.ui.overlay.view.PatternOverlayView
import com.aviapp.app.security.applocker.ui.permissions.PermissionChecker
import com.aviapp.app.security.applocker.util.extensions.convertToPatternDot
import com.aviapp.app.security.applocker.util.extensions.plusAssign
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint
import dagger.android.DaggerService
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject


class AppLockerService : DaggerService() {

    @Inject
    lateinit var serviceNotificationManager: ServiceNotificationManager

    @Inject
    lateinit var appForegroundObservable: AppForegroundObservable

    @Inject
    lateinit var permissionCheckerObservable: PermissionCheckerObservable

    @Inject
    lateinit var lockedAppsDao: LockedAppsDao

    @Inject
    lateinit var patternDao: PatternDao

    @Inject
    lateinit var appLockerPreferences: AppLockerPreferences

    @Inject
    lateinit var fingerprintIdentify: FingerprintIdentify

    @Inject
    lateinit var adHelper: AdHelper


    private val validatedPatternObservable = PublishSubject.create<List<PatternDot>>()

    private val allDisposables: CompositeDisposable = CompositeDisposable()

    private var foregroundAppDisposable: Disposable? = null

    private val lockedAppPackageSet: HashSet<String> = HashSet()
    private val blockedActivities: HashSet<String> = HashSet()

    private lateinit var windowManager: WindowManager

    private lateinit var overlayParams: WindowManager.LayoutParams

    private lateinit var overlayView: PatternOverlayView

    private var isOverlayShowing = false
    private var stPack: String? = null

    private var lastForegroundAppPackage: String? = null
    private var TAG: String? = "AppLockerService"
    private var wakeLock: PowerManager.WakeLock? = null

    private var screenOnOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> observeForegroundApplication()
                Intent.ACTION_SCREEN_OFF -> stopForegroundApplicationObserver()
            }

            Log.d(TAG, "screenOnOffReceiver${intent?.action}")
        }
    }

    private var installUninstallReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.e("onReceive", "Call")
        }
    }

    init {
        SystemPackages.getSystemPackages().forEach {
            lockedAppPackageSet.add(it)
        }
        blockedActivities.add("com.android.packageinstaller.UninstallerActivity")
        blockedActivities.add("ccom.google.android.packageinstaller.UninstallerActivity")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d(TAG, "onStartCommand")

        if (allDisposables.isDisposed) {

            Log.d(TAG, "disposed")
            initializeOverlayView()
            observeLockedApps()
            observeOverlayView()
            observeForegroundApplication()
            observePermissionChecker()
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        initializeAppLockerNotification()
        acquireWakeLock()
        registerScreenReceiver()
        registerInstallUninstallReceiver()
        initializeOverlayView()
        observeLockedApps()
        observeOverlayView()
        observeForegroundApplication()
        observePermissionChecker()
    }

    override fun onDestroy() {
        ServiceStarter.startService(applicationContext)
        unregisterScreenReceiver()
        unregisterInstallUninstallReceiver()

        if (allDisposables.isDisposed.not()) {
            allDisposables.dispose()
        }

        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }

        } catch (e: Exception) {
            Log.d(TAG, "Service stopped without being started: ${e.message}")
        }

        super.onDestroy()
    }


    private fun registerInstallUninstallReceiver() {
        var installUninstallFilter = IntentFilter()
            .apply {
                addAction(Intent.ACTION_PACKAGE_INSTALL)
                addDataScheme("package")
            }

        registerReceiver(installUninstallReceiver, installUninstallFilter)
    }

    private fun unregisterInstallUninstallReceiver() {
        unregisterReceiver(installUninstallReceiver)
    }

    private fun registerScreenReceiver() {
        val screenFilter = IntentFilter()
        screenFilter.addAction(Intent.ACTION_SCREEN_ON)
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOnOffReceiver, screenFilter)
    }

    private fun acquireWakeLock() {

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }
    }

    private fun unregisterScreenReceiver() {
        unregisterReceiver(screenOnOffReceiver)
    }

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
                { error ->  })
    }

    private fun observeOverlayView() {
        allDisposables += Flowable
            .combineLatest(
                patternDao.getPattern().map { it.patternMetadata.pattern },
                validatedPatternObservable.toFlowable(BackpressureStrategy.BUFFER),
                PatternValidatorFunction()
            )
            .subscribe(this@AppLockerService::onPatternValidated)
    }

    private fun initializeOverlayView() {

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayParams = OverlayViewLayoutParams.get()
        overlayView =
            PatternOverlayView(applicationContext, object : PatternOverlayView.PinMatchedListener {
                override fun onPinMatched() {
                    hideOverlay()
                }
            }, fingerprintIdentify = fingerprintIdentify, adHelper = adHelper).apply {
                observePattern(this@AppLockerService::onDrawPattern)
            }
    }

    private fun observeForegroundApplication() {

        if (foregroundAppDisposable != null && foregroundAppDisposable?.isDisposed?.not() == true) {
            return
        }

        foregroundAppDisposable = appForegroundObservable
            .get()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { foregroundAppPackage -> onAppForeground(foregroundAppPackage) },
                { error ->  }
            )

        allDisposables.add(foregroundAppDisposable!!)
    }

    private fun stopForegroundApplicationObserver() {
        if (foregroundAppDisposable != null && foregroundAppDisposable?.isDisposed?.not() == true) {
            foregroundAppDisposable?.dispose()
        }
    }

    private fun observePermissionChecker() {

        allDisposables += permissionCheckerObservable
            .get()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isPermissionNeed ->
                if (isPermissionNeed) {
                    showPermissionNeedNotification()
                } else {
                    serviceNotificationManager.hidePermissionNotification()
                }
            }
    }

    private fun onAppForeground(foregroundAppPackage: String) {


        if (foregroundAppPackage != "android") {
            //hideOverlay()
            Log.d(TAG, "Current App or Activity $foregroundAppPackage")

            if (blockedActivities.contains(foregroundAppPackage) || lockedAppPackageSet.contains(foregroundAppPackage)) {
                if (/*appLockerPreferences.getFingerPrintEnabled() || */PermissionChecker.checkOverlayPermission(applicationContext).not()) {

                    val intent = OverlayValidationActivity.newIntent(
                        applicationContext,
                        foregroundAppPackage
                    )
                    if (lastForegroundAppPackage == applicationContext.packageName) {
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    } else {
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)

                } else {

                    val intent = OverlayValidationActivity.newIntent(
                        applicationContext,
                        foregroundAppPackage
                    )

                    if (foregroundAppPackage == "com.android.packageinstaller.UninstallerActivity" || foregroundAppPackage == "ccom.google.android.packageinstaller.UninstallerActivity") {
                        showOverlay(foregroundAppPackage)
                    } else if (lastForegroundAppPackage == applicationContext.packageName) {
//                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                        startActivity(intent)
                        showOverlay(foregroundAppPackage)
                    } else {
//                        intent.flags =
//                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                        startActivity(intent)
                        showOverlay(foregroundAppPackage)
                    }



//
//                    Log.d(TAG, "show Overlay")
//                    showOverlay(foregroundAppPackage)
                }
            }
            lastForegroundAppPackage = foregroundAppPackage
        }
    }

    private fun onDrawPattern(pattern: List<PatternLockView.Dot>) {
        validatedPatternObservable.onNext(pattern.convertToPatternDot())
    }

    private fun onPatternValidated(isDrawedPatternCorrect: Boolean) {
        if (isDrawedPatternCorrect) {
            overlayView.notifyDrawnCorrect()
            hideOverlay()
        } else {
            overlayView.notifyDrawnWrong()
        }
    }

    private fun initializeAppLockerNotification() {
        val notification = serviceNotificationManager.createNotification()
        NotificationManagerCompat.from(applicationContext)
            .notify(NOTIFICATION_ID_APPLOCKER_SERVICE, notification)
        startForeground(NOTIFICATION_ID_APPLOCKER_SERVICE, notification)
    }

    private fun showPermissionNeedNotification() {
        val notification = serviceNotificationManager.createPermissionNeedNotification()
        NotificationManagerCompat.from(applicationContext)
            .notify(NOTIFICATION_ID_APPLOCKER_PERMISSION_NEED, notification)
    }

    private fun showOverlay(lockedAppPackageName: String) {
        initFingerPr()
        if (isOverlayShowing.not()) {
            isOverlayShowing = true
            overlayView.setHiddenDrawingMode(appLockerPreferences.getHiddenDrawingMode())
            overlayView.setAppPackageName(lockedAppPackageName)
            windowManager.addView(overlayView, overlayParams)
        }
    }

    private fun hideOverlay() {
        if (isOverlayShowing) {
            isOverlayShowing = false
            fingerprintIdentify.cancelIdentify()
            windowManager.removeViewImmediate(overlayView)
        }
    }

    private fun initFingerPr() {

        if(appLockerPreferences.getFingerPrintEnabled()){

            fingerprintIdentify.cancelIdentify()
            fingerprintIdentify = provideFingerPrintInstance(applicationContext)
            fingerprintIdentify.startIdentify(3, object : BaseFingerprint.IdentifyListener {
                override fun onSucceed() {
                    hideOverlay()
                }

                override fun onNotMatch(availableTimes: Int) {
                }

                override fun onFailed(isDeviceLocked: Boolean) {
                }

                override fun onStartFailedByDeviceLocked() {
                }
            })
        }

    }

    fun provideFingerPrintInstance(context: Context): FingerprintIdentify {

        val fingerprintIdentify = FingerprintIdentify(context)
        try {
            fingerprintIdentify.setSupportAndroidL(true)
            fingerprintIdentify.init()
        } catch (e: Throwable) {
        }
        return fingerprintIdentify
    }

    companion object {
        private const val NOTIFICATION_ID_APPLOCKER_SERVICE = 1
        private const val NOTIFICATION_ID_APPLOCKER_PERMISSION_NEED = 2
    }
}