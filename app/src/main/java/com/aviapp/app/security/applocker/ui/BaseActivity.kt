package com.aviapp.app.security.applocker.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.aviapp.ads.AdHelper
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.database.AppLockerDatabase
import com.aviapp.app.security.applocker.ui.prem.PremActivity
import com.aviapp.app.security.applocker.util.setAnimatedClick
import com.aviapp.purchase.PurchaseService
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseActivity<VM : ViewModel> : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prService: PurchaseService

    @Inject
    lateinit var adHelper: AdHelper

    var adId: String? = null

    @Inject
    lateinit var database: AppLockerDatabase

    protected lateinit var viewModel: VM

    abstract fun getViewModel(): Class<VM>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(getViewModel())
    }

    fun initAd(small: Boolean? = null) {

        val adIcon: ImageView? = findViewById(R.id.noAdv)
        adIcon?.setOnClickListener {
            startActivity(Intent(this, PremActivity::class.java))
        }

        adHelper.adEventLiveData.observe(this) {
            lifecycleScope.launch {

                adId?.let { id ->
                    adHelper.showNative(id, this@BaseActivity, null, it, small)
                }

                adHelper.showBanner(this@BaseActivity, null, it)
                if (it) {
                    adIcon?.visibility = View.VISIBLE
                } else {
                    adIcon?.visibility = View.INVISIBLE
                }

            }
        }
    }
    
}