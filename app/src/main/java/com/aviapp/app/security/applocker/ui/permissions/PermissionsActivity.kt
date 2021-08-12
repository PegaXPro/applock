package com.aviapp.app.security.applocker.ui.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.databinding.ActivityPermissionsBinding
import com.aviapp.app.security.applocker.service.notification.ServiceNotificationManager
import com.aviapp.app.security.applocker.ui.BaseActivity
import com.aviapp.app.security.applocker.ui.main.MainActivity
import com.aviapp.app.security.applocker.ui.settings.SettingsActivity
import com.aviapp.app.security.applocker.ui.settings.SettingsViewModel
import com.aviapp.app.security.applocker.util.FirebaseEvents
import com.aviapp.app.security.applocker.util.extensions.toast
import com.aviapp.app.security.applocker.util.setToggleStateAnimated
import kotlinx.android.synthetic.main.activity_permissions.*
import kotlinx.android.synthetic.main.rate_us_dialog.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class PermissionsActivity : BaseActivity<SettingsViewModel>() {

    private lateinit var binding: ActivityPermissionsBinding
//    lateinit var permission1: View
//    lateinit var permission2: View
//    lateinit var permission3: View
    var firsPerm = false

    @Inject
    lateinit var serviceNotificationManager: ServiceNotificationManager
    lateinit var sharedPref: SharedPreferences

    override fun getViewModel(): Class<SettingsViewModel> = SettingsViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_permissions)

        sharedPref = applicationContext.getSharedPreferences("first1", Context.MODE_PRIVATE)

        val start = AutoStartPermissionHelper.getInstance()


        binding.perm1.setOnClickListener {
            lifecycleScope.launch {
                binding.autoStartSw.setToggleStateAnimated(true)
                firsPerm = true
                start.getAutoStartPermission(this@PermissionsActivity)
            }
        }


        if (!start.isAutoStartPermissionAvailable(this)) {
            firsPerm = true
            binding.perm1.visibility = View.INVISIBLE
        }


/*        binding.autoStartSw.setOnCheckedChangeListener { buttonView, isChecked ->

            //sharedPref.edit().putBoolean("check", isChecked).apply()
            if (isChecked && start.isAutoStartPermissionAvailable(this)) {
                start.getAutoStartPermission(this)
            } else {
                binding.runServicePerm.visibility = View.INVISIBLE
            }
        }*/

        binding.goBack.setOnClickListener {
            onBackPressed()
        }

        binding.perm2.setOnClickListener {
            startActivity(IntentHelper.usageAccessIntent())
        }

        binding.perm3.setOnClickListener {
            startActivityForResult(
                IntentHelper.overlayIntent(packageName),
                RC_OVERLAY_PERMISSION
            )
        }

//        permission1 = findViewById(R.id.perm1)
//        permission2 = findViewById(R.id.perm2)
//        permission3 = findViewById(R.id.perm3)

/*        binding.switchUsageAccess.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && PermissionChecker.checkUsageAccessPermission(this).not()) {
                startActivity(IntentHelper.usageAccessIntent())
            }
        }*/

/*        binding.switchOverlay.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked && PermissionChecker.checkOverlayPermission(this).not()) {
                startActivityForResult(
                    IntentHelper.overlayIntent(packageName),
                    RC_OVERLAY_PERMISSION
                )
            }
        }*/


        binding.buttonNext.setOnClickListener {

            val p1 = PermissionChecker.checkUsageAccessPermission(this)
            val p2 = PermissionChecker.checkOverlayPermission(this)

            Log.d("SLDFJSLJF", "!!! p1 = $p1   p2 = $p2")

            if (!firsPerm) {
                binding.perm1.setBackgroundResource(R.drawable.custom_item_buck_select)
            }
            if (!p1) {
                binding.perm2.setBackgroundResource(R.drawable.custom_item_buck_select)
            }
            if (!p2) {
                binding.perm3.setBackgroundResource(R.drawable.custom_item_buck_select)
            }

            if (PermissionChecker.isAllPermissionChecked(this).not()) {
                toast(R.string.permission_toast_enable_permissions)
                return@setOnClickListener
            }

            if (isAllPermissionsPermitted()) {
                FirebaseEvents.get_all_permission_success()
                lockImportantAppsOrAllApps()
            }
        }

        binding.textViewPrivacyPolicy.setOnClickListener {
            startActivity(IntentHelper.privacyPolicyWebIntent("https://docs.google.com/document/d/1iFdc5aWTrKoH0dOlhkgA0aWXbmr_HEP9XyALIzjXnUw/edit"))
        }
    }

    override fun onStop() {
        super.onStop()
        //finish()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (sharedPref.getBoolean("firstL", true)) {
            sharedPref.edit().putBoolean("firstL", false).apply()
        }

        if (isAllPermissionsPermitted()) {
            serviceNotificationManager.hidePermissionNotification()
            setResult(Activity.RESULT_OK)
            FirebaseEvents.get_all_permission_success()
            lockImportantAppsOrAllApps()
        }

    }

    override fun onResume() {
        super.onResume()

        val p1 = PermissionChecker.checkUsageAccessPermission(this)
        val p2 = PermissionChecker.checkOverlayPermission(this)

        if (firsPerm) {
            binding.perm1.setBackgroundResource(R.drawable.custom_item_buck)
        }

        if (p1) {
            binding.perm2.setBackgroundResource(R.drawable.custom_item_buck)
        }

        if (p2) {
            binding.perm1.setBackgroundResource(R.drawable.custom_item_buck)
        }


        binding.switchUsageAccess.setToggleStateAnimated(p1)
        binding.switchOverlay.setToggleStateAnimated(p2)
    }

    private fun lockAllApps() {
        if (viewModel.isAllLocked()) {
            viewModel.unlockAll()
        } else {
            FirebaseEvents.lock_all_app_success()
            viewModel.lockAll()
        }
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("allAppsLocked", 1)
        startActivity(intent)
    }

    private fun lockImportantAppsOrAllApps() {
        if (intent.getIntExtra("lockAppOrApps", 0) == 1) {
            lockAllApps()
        } else if (intent.getIntExtra("lockAppOrApps", 0) == 0){
            viewModel.lockAllImportantApps()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("allAppsLocked", 1)
            startActivity(intent)
        }
    }

    private fun isAllPermissionsPermitted() =
        PermissionChecker.checkUsageAccessPermission(this) &&
                PermissionChecker.checkOverlayPermission(this)

    companion object {
        private const val RC_OVERLAY_PERMISSION = 123
        fun newIntent(context: Context): Intent {
            return Intent(context, PermissionsActivity::class.java)
        }
    }
}