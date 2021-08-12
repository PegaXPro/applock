package com.aviapp.ads

import com.aviapp.ads.AdHelper
import com.aviapp.ads.AdHelperImpl
import com.aviapp.ads.AppOpenAds
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val adModule = module {

    single<AdHelper> { AdHelperImpl(androidContext(), get()) }
    single { AppOpenAds(androidApplication(), get()) }
}