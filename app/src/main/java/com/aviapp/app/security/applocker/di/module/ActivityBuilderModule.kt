package com.aviapp.app.security.applocker.di.module

import com.aviapp.app.security.applocker.di.scope.ActivityScope
import com.aviapp.app.security.applocker.ui.background.BackgroundsActivity
import com.aviapp.app.security.applocker.ui.browser.BrowserActivity
import com.aviapp.app.security.applocker.ui.callblocker.CallBlockerActivity
import com.aviapp.app.security.applocker.ui.icon_changer.IconChangerActivity
import com.aviapp.app.security.applocker.ui.image_viewer.ImageScrActivity
import com.aviapp.app.security.applocker.ui.intruders.IntrudersPhotosActivity
import com.aviapp.app.security.applocker.ui.main.FeedbackActivity
import com.aviapp.app.security.applocker.ui.main.MainActivity
import com.aviapp.app.security.applocker.ui.main.NewMainActivity
import com.aviapp.app.security.applocker.ui.newpattern.CreateNewPatternActivity
import com.aviapp.app.security.applocker.ui.overlay.activity.OverlayValidationActivity
import com.aviapp.app.security.applocker.ui.permissions.PermissionsActivity
import com.aviapp.app.security.applocker.ui.prem.PremActivity
import com.aviapp.app.security.applocker.ui.settings.SettingsActivity
import com.aviapp.app.security.applocker.ui.splash.SplashActivity
import com.aviapp.app.security.applocker.ui.vault.VaultActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class ActivityBuilderModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class, DialogFragmentBuilderModule::class])
    abstract fun mainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class, DialogFragmentBuilderModule::class])
    abstract fun mainIcon(): IconChangerActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class, DialogFragmentBuilderModule::class])
    abstract fun settingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class, DialogFragmentBuilderModule::class])
    abstract fun newMainActivity(): NewMainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class, DialogFragmentBuilderModule::class])
    abstract fun backgroundActivity(): BackgroundsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class, DialogFragmentBuilderModule::class])
    abstract fun browserActivity(): BrowserActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class, DialogFragmentBuilderModule::class])
    abstract fun vaultActivity(): VaultActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class, DialogFragmentBuilderModule::class])
    abstract fun prevActivity(): ImageScrActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class, DialogFragmentBuilderModule::class])
    abstract fun callBlockerActivity(): CallBlockerActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class, DialogFragmentBuilderModule::class])
    abstract fun intrudersPhotosActivity(): IntrudersPhotosActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun permissionsActivity(): PermissionsActivity


    @ActivityScope
    @ContributesAndroidInjector
    abstract fun messageSenderActivity(): FeedbackActivity


    @ActivityScope
    @ContributesAndroidInjector
    abstract fun createNewPatternActivity(): CreateNewPatternActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun fingerPrintOverlayActivity(): OverlayValidationActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun pemOverlayActivity(): PremActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun spActivity(): SplashActivity
}