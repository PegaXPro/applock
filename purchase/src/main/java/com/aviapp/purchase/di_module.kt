package com.aviapp.purchase

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module


val purchaseModule = module {

    factory { UpdatePurchaseStatusUseCase(get()) }
    single { PurchaseService(androidApplication(), get(), get()) }
}