package com.aviapp.app.security.applocker.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.aviapp.ads.AdHelper
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.AppLockerPreferences
import com.aviapp.app.security.applocker.data.database.prem.Prem
import com.aviapp.app.security.applocker.databinding.FragmentSettingsBinding
import com.aviapp.app.security.applocker.ui.BaseFragment
import com.aviapp.app.security.applocker.ui.callblocker.CallBlockerActivity
import com.aviapp.app.security.applocker.ui.icon_changer.IconChangerActivity
import com.aviapp.app.security.applocker.ui.intruders.IntrudersPhotosActivity
import com.aviapp.app.security.applocker.ui.newpattern.CreateNewPatternActivity
import com.aviapp.app.security.applocker.ui.permissiondialog.UsageAccessPermissionDialog
import com.aviapp.app.security.applocker.ui.permissions.PermissionChecker
import com.aviapp.app.security.applocker.ui.permissions.PermissionsActivity
import com.aviapp.app.security.applocker.ui.prem.PremActivity
import com.aviapp.app.security.applocker.ui.security.analytics.SecurityFragmentAnalytics
import com.aviapp.app.security.applocker.ui.settings.analytics.SettingsAnalytics
import com.aviapp.app.security.applocker.util.FirebaseEvents
import com.aviapp.app.security.applocker.util.delegate.inflate
import com.aviapp.app.security.applocker.util.extensions.toast
import com.aviapp.app.security.applocker.util.getToggleState
import com.aviapp.app.security.applocker.util.helper.DpmHelper
import com.aviapp.app.security.applocker.util.setToggleState
import com.aviapp.app.security.applocker.util.setToggleStateAnimated
import com.tbruyelle.rxpermissions2.RxPermissions
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsFragment : BaseFragment<SettingsViewModel>() {

    private val binding: FragmentSettingsBinding by inflate(R.layout.fragment_settings)
    private lateinit var appLockerPreferences: AppLockerPreferences
    private val TAG: String = "SettingsFragment"

    @Inject
    lateinit var fingerprintIdentify: FingerprintIdentify

    @Inject
    lateinit var adHandler: AdHelper

    override fun getViewModel(): Class<SettingsViewModel> = SettingsViewModel::class.java


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        appLockerPreferences = AppLockerPreferences(requireContext())


        binding.goBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.imageViewLockAll.setOnClickListener {
            if (PermissionChecker.isAllPermissionChecked(requireContext()).not()) {
                val intent = Intent(context, PermissionsActivity::class.java)
                intent.putExtra("lockAppOrApps", 1)
                requireContext().startActivity(intent)
            } else {
                lifecycleScope.launch {
                    adHandler.showInterstitial(requireActivity()) {
                        if (PermissionChecker.checkUsageAccessPermission(requireActivity()).not()) {
                            UsageAccessPermissionDialog.newInstance()
                                .show(requireActivity().supportFragmentManager, "")
                        } else {
                            if (viewModel.isAllLocked()) {
                                viewModel.unlockAll()
                            } else {
                                FirebaseEvents.lock_all_app_success()
                                viewModel.lockAll()
                            }
                        }
                    }
                }
            }
        }

//        binding.layoutIcon.setOnClickListener {
//            lifecycleScope.launch {
//                startActivity(Intent(requireContext(), IconChangerActivity::class.java))
//            }
//        }

        binding.layoutChangePattern.setOnClickListener {
            activity?.let {
                startActivityForResult(
                    CreateNewPatternActivity.newIntent(
                        it,
                        CreateNewPatternActivity.TYPE_PATTERN
                    ),
                    RC_CHANGE_PATTERN
                )
            }
        }

        binding.layoutChangePin.setOnClickListener {
            activity?.let {
                startActivityForResult(
                    CreateNewPatternActivity.newIntent(
                        it,
                        CreateNewPatternActivity.TYPE_PIN
                    ),
                    RC_CHANGE_PIN
                )
            }
        }


//        binding.callBlockSettings.setOnClickListener {
//            startActivity(CallBlockerActivity.newIntent(requireContext()))
//            SecurityFragmentAnalytics.onCallBlockerClicked(requireContext())
//        }

        /*      binding.switchAdvanceProtection.setOnClickListener {
                  activity?.let {
                      if (DpmHelper.isDeviceAdminActive(activity as Context)) {
                          DpmHelper.removeDeviceAdmin(activity as Context)
      //                    viewModel.unlockApp("com.android.settings")
                      } else {
      //                    viewModel.lockApp(
      //                        LockedAppEntity("com.android.settings")
      //                    )
                          DpmHelper.activateDeviceAdmin(
                              activity!!,
                              RC_ADVANCE_PROTECTION
                          )
                      }
                  }
              }
      */




        viewModel.settingsViewStateLiveData.observe(this.viewLifecycleOwner) {
            binding.switchAdvanceProtection.setToggleStateAnimated(it.isAdvanceProtectionEnabled)
            binding.switchStealth.setToggleStateAnimated(it.isHiddenDrawingMode)
            binding.switchFingerPrint.setToggleStateAnimated(it.isFingerPrintEnabled)
            /*binding.switchStealth*/
        }


/*        binding.switchAdvanceProtection.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked && !DpmHelper.isDeviceAdminActive(activity as Context)) {
                DpmHelper.activateDeviceAdmin(
                    requireActivity(),
                    RC_ADVANCE_PROTECTION
                )
            } else if (!isChecked && DpmHelper.isDeviceAdminActive(activity as Context)) {
                DpmHelper.removeDeviceAdmin(activity as Context)
            }
        }*/

        binding.switchAdvanceProtection.setOnClickListener {

            val isChecked = binding.switchAdvanceProtection.getToggleState()

            if (!isChecked && !DpmHelper.isDeviceAdminActive(activity as Context)) {
                DpmHelper.activateDeviceAdmin(
                    requireActivity(),
                    RC_ADVANCE_PROTECTION
                )
            } else if (isChecked && DpmHelper.isDeviceAdminActive(activity as Context)) {
                DpmHelper.removeDeviceAdmin(activity as Context)
            }

            binding.switchAdvanceProtection.setToggleStateAnimated(!isChecked)
        }



/*        binding.switchStealth.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setHiddenDrawingMode(isChecked)
        }*/

        binding.switchStealth.setOnClickListener {
            val state = binding.switchStealth.getToggleState()
            binding.switchStealth.setToggleStateAnimated(!state)
            viewModel.setHiddenDrawingMode(!state)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val fingerprintManager =
                requireActivity().getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager?

            when {

                fingerprintManager == null -> {
                    binding.fgHolder.visibility = View.GONE
                }
                !fingerprintManager.isHardwareDetected -> {
                    binding.fgHolder.visibility = View.GONE
                }
                !fingerprintManager.hasEnrolledFingerprints()-> {
                binding.layoutFingerPrint.setOnClickListener {
                    binding.switchFingerPrint.setToggleStateAnimated(false)
                    val intent =
                        when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> Intent(Settings.ACTION_FINGERPRINT_ENROLL)
                            else -> {Intent(Settings.ACTION_SECURITY_SETTINGS);}
                        }
                    startActivity(intent);
                }
            }
                else -> {
/*                    binding.switchFingerPrint.setOnCheckedChangeListener { buttonView, isChecked ->
                        SettingsAnalytics.fingerPrintEnabled(requireContext())
                        viewModel.setEnableFingerPrint(isChecked)
                    }*/

                    binding.switchFingerPrint.setOnClickListener {
                        SettingsAnalytics.fingerPrintEnabled(requireContext())
                        val state = binding.switchFingerPrint.getToggleState()
                        viewModel.setEnableFingerPrint(!state)
                        binding.switchFingerPrint.setToggleStateAnimated(!state)
                    }

                }
            }
        } else {
            binding.fgHolder.visibility = View.GONE
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.switchEnableIntrudersCatcher.setOnCheckedChangeListener { buttonView, isChecked ->
//            activity?.let { SettingsAnalytics.intrudersEnabled(it) }
//            enableIntrudersCatcher(isChecked)
//        }


//        binding.layoutIntrudersFolder.setOnClickListener {
//            /*activity?.let {
//                if (viewModel.isIntrudersCatcherEnabled().not()) {
//                    activity?.let { SettingsAnalytics.intrudersEnabled(it) }
//                    enableIntrudersCatcher(true)
//                } else {
//                    SettingsAnalytics.intrudersFolderClicked(it)
//                    startActivity(IntrudersPhotosActivity.newIntent(it))
//                }
//            }*/
//            SettingsAnalytics.intrudersFolderClicked(requireContext())
//            startActivity(IntrudersPhotosActivity.newIntent(requireActivity()))
//        }

    }


    override fun onResume() {
        super.onResume()
        binding.switchAdvanceProtection.setToggleState(DpmHelper.isDeviceAdminActive(activity as Context))

        if (appLockerPreferences.isPinEnabled()) {
            textViewPinTitle.text = getString(R.string.change_pin)
            textViewPinDescription.text = getString(R.string.settings_change_pin_description)
        }


    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getSettingsViewStateLiveData().observe(this.viewLifecycleOwner) {
            binding.viewState = it
        }

        viewModel.getFingerPrintStatusViewStateLiveData().observe(this.viewLifecycleOwner) {
            binding.fingerPrintStatus = it
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_CHANGE_PATTERN -> {
                if (resultCode == Activity.RESULT_OK) {
                    activity?.let { it.toast(R.string.message_pattern_changed) }
                }
            }

            RC_CHANGE_PIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    activity?.let { it.toast(R.string.message_pin_changed) }
                }
            }

            RC_ADVANCE_PROTECTION -> {
                if (resultCode == Activity.RESULT_OK) {
                }
            }

        }
    }

    @SuppressLint("CheckResult")
    private fun enableIntrudersCatcher(isChecked: Boolean) {
        if (isChecked) {

            try {

                when {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_CONTACTS
                    ) != PackageManager.PERMISSION_GRANTED -> {
                        RxPermissions(this)
                            .request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe { granted ->
                                viewModel.setEnableIntrudersCatchers(granted)
                            }
                    } else -> {  viewModel.setEnableIntrudersCatchers(true) }
                }

            } catch (e: Throwable) {

            }

        } else {
            viewModel.setEnableIntrudersCatchers(false)
        }
    }


    companion object {
        private const val RC_CHANGE_PATTERN = 101
        private const val RC_CHANGE_PIN = 103
        private const val RC_ADVANCE_PROTECTION = 102
        fun newInstance() = SettingsFragment()
    }
}