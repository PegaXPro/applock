package com.aviapp.app.security.applocker.di.module

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aviapp.ads.AdHelper
import com.aviapp.ads.AdHelperImpl
import com.aviapp.app.security.applocker.AppLockerApplication
import com.aviapp.app.security.applocker.data.database.prem.Prem
import com.aviapp.app.security.applocker.data.database.prem.PremDao
import com.aviapp.app.security.applocker.ui.overlay.activity.FingerPrintLiveData
import com.aviapp.app.security.applocker.util.GMailSender
import com.aviapp.purchase.PremEvents
import com.aviapp.purchase.PurchaseService
import com.aviapp.purchase.UpdatePurchaseStatusUseCase
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {

    @Provides
    @Singleton
    fun provideContext(appLockerApplication: AppLockerApplication): Context = appLockerApplication.applicationContext

    @Provides
    @Singleton
    fun provideApplication(appLockerApplication: AppLockerApplication): Application = appLockerApplication


    @Provides
    fun provideFingerPrintInstance(context: Context): FingerprintIdentify {

        val fingerprintIdentify = FingerprintIdentify(context)
        try {
            fingerprintIdentify.setSupportAndroidL(true)
            fingerprintIdentify.init()
        } catch (e: Throwable) {
        }
        return fingerprintIdentify
    }


    @Provides
    @Singleton
    fun getPrem(premDatabaseDao: PremDao, scope: CoroutineScope) = object: PremEvents {

        override suspend fun prIsNeeded() = withContext(Dispatchers.IO) {(premDatabaseDao.getPrem()?: Prem()).prIsNeeded}

        override fun prIsNeededFlow() = premDatabaseDao.getPremFlow().flowOn(Dispatchers.IO).map { (it?: Prem()).prIsNeeded }

        override fun prIsNeededLiveData(): LiveData<Boolean> {
            return MutableLiveData<Boolean>()
        }

        override fun updatePrem(prem: Boolean) {
            scope.launch(Dispatchers.IO) {
                premDatabaseDao.updatePrem(Prem(0, prem))
            }
        }
    }


    @Singleton
    @Provides
    fun provideGmailSender(context: Context) : GMailSender = GMailSender(context)

    @Singleton
    @Provides
    fun provideAds(context: Context, premEvents: PremEvents) : AdHelper = AdHelperImpl(context, premEvents)


    @Provides
    fun provideFingerPrintLiveData(fingerprintIdentify: FingerprintIdentify, context: Context): FingerPrintLiveData
            = FingerPrintLiveData(fingerprintIdentify, context)


    @Provides
    fun provideUpdatePr(premEvents: PremEvents): UpdatePurchaseStatusUseCase = UpdatePurchaseStatusUseCase(premEvents)

    @Singleton
    @Provides
    fun providePrHelper(context: Context, premEvents: PremEvents, updatePurchaseStatusUseCase: UpdatePurchaseStatusUseCase): PurchaseService = PurchaseService(context, premEvents, updatePurchaseStatusUseCase)

    @Provides
    @Singleton
    fun provideGlobalScope(): CoroutineScope = CoroutineScope(Dispatchers.Main)

}