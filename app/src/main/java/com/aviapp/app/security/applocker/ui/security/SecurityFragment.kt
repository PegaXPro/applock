package com.aviapp.app.security.applocker.ui.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.databinding.FragmentSecurityBinding
import com.aviapp.app.security.applocker.ui.BaseFragment
import com.aviapp.app.security.applocker.ui.permissiondialog.UsageAccessPermissionDialog
import com.aviapp.app.security.applocker.ui.permissions.PermissionChecker
import com.aviapp.app.security.applocker.ui.security.analytics.SecurityFragmentAnalytics
import com.aviapp.app.security.applocker.util.FirebaseEvents
import com.aviapp.app.security.applocker.util.delegate.inflate
import com.google.android.gms.ads.*

class SecurityFragment : BaseFragment<SecurityViewModel>() {


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

        viewModel.getAppDataListLiveData().observe(this.viewLifecycleOwner) {
            try {
                binding.progressBar2.visibility = View.INVISIBLE
                adapter.setAppDataList(it)
            } catch (e: Throwable) {
            }
        }



        adapter.appItemClicked = this@SecurityFragment::onAppSelected
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
        fun newInstance() = SecurityFragment()
    }
}