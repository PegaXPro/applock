package com.aviapp.app.security.applocker.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.aviapp.ads.AdHelper
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.database.AppLockerDatabase
import com.aviapp.app.security.applocker.data.database.prem.Prem
import com.aviapp.app.security.applocker.ui.prem.PremActivity
import com.aviapp.purchase.PremEvents
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseFragment<VM : ViewModel> : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var database: AppLockerDatabase

    var adId: String? = null

    @Inject
    lateinit var adHelper: AdHelper

    lateinit var viewModel: VM

    abstract fun getViewModel(): Class<VM>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(getViewModel())
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adIcon: View? = view.findViewById(R.id.noAd)

        adIcon?.setOnClickListener {
            startActivity(Intent(requireContext(), PremActivity::class.java))
        }

        lifecycleScope.launch {
            database.premDao().getPremFlow().flowOn(Dispatchers.IO).collect {
                val p0 = it?:Prem()
                adIcon?.visibility = if (p0.prIsNeeded) View.VISIBLE else View.INVISIBLE
            }
        }

        adHelper.adEventLiveData.observe(this.viewLifecycleOwner) {
            lifecycleScope.launch {
                adId?.let { id ->
                    adHelper.showNative(id, requireActivity(), view, it, false)
                }
                adHelper.showBanner(requireActivity(), view, it)
            }
        }
    }
}