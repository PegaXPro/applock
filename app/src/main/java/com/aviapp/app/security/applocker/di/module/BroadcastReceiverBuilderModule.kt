package com.aviapp.app.security.applocker.di.module

import com.aviapp.app.security.applocker.ui.callblocker.service.CallReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BroadcastReceiverBuilderModule {

    @ContributesAndroidInjector
    abstract fun callBroadcastReceiver(): CallReceiver

}