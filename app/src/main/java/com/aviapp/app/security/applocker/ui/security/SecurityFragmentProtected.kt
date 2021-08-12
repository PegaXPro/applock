package com.aviapp.app.security.applocker.ui.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.aviapp.ads.AdHelper
import com.google.android.gms.ads.*
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.databinding.FragmentSecurityBinding
import com.aviapp.app.security.applocker.ui.BaseFragment
import com.aviapp.app.security.applocker.ui.background.BackgroundsActivity
import com.aviapp.app.security.applocker.ui.browser.BrowserActivity
import com.aviapp.app.security.applocker.ui.callblocker.CallBlockerActivity
import com.aviapp.app.security.applocker.ui.permissiondialog.UsageAccessPermissionDialog
import com.aviapp.app.security.applocker.ui.permissions.PermissionChecker
import com.aviapp.app.security.applocker.ui.security.analytics.SecurityFragmentAnalytics
import com.aviapp.app.security.applocker.ui.vault.VaultActivity
import com.aviapp.app.security.applocker.ui.vault.analytics.VaultAdAnalytics
import com.aviapp.app.security.applocker.util.FirebaseEvents
import com.aviapp.app.security.applocker.util.delegate.inflate
import javax.inject.Inject

class SecurityFragmentProtected : BaseFragment<SecurityViewModel>() {

    private val binding: FragmentSecurityBinding by inflate(R.layout.fragment_security)

    private lateinit var adapter: AppLockListAdapter

    override fun getViewModel(): Class<SecurityViewModel> = SecurityViewModel::class.java

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AppLockListAdapter(adHelper, lifecycleScope)
        binding.recyclerViewAppLockList.adapter = adapter


        viewModel.getProtectedApp().observe(this.viewLifecycleOwner) {

            try {
                binding.noItemsFrame.visibility = if (it.isEmpty()) View.VISIBLE else View.INVISIBLE
                binding.progressBar2.visibility = View.INVISIBLE
                adapter.setAppDataList(it)
            } catch (e: Throwable) { }
        }
        adapter.appItemClicked = this@SecurityFragmentProtected::onAppSelected

    }


    private fun onAppSelected(selectedApp: AppLockItemItemViewState) {
        activity?.let {
            if (PermissionChecker.checkUsageAccessPermission(it).not()) {
                UsageAccessPermissionDialog.newInstance().show(it.supportFragmentManager, "")
            } else {
                if (selectedApp.isLocked) {
                    activity?.let { SecurityFragmentAnalytics.onAppUnlocked(it) }
                    viewModel.unlockApp(selectedApp)
                } else {

                    FirebaseEvents.lock_screen_success()
                    activity?.let { SecurityFragmentAnalytics.onAppLocked(it) }
                    viewModel.lockApp(selectedApp)
                }
            }
        }
    }

    companion object {
        fun newInstance() = SecurityFragmentProtected()
    }
}