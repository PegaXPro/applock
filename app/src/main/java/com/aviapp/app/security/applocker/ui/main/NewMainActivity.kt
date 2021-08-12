package com.aviapp.app.security.applocker.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.aviapp.app.security.applocker.BuildConfig
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.database.prem.Prem
import com.aviapp.app.security.applocker.databinding.ActivityNewMainBinding
import com.aviapp.app.security.applocker.ui.BaseActivity
import com.aviapp.app.security.applocker.ui.background.BackgroundsActivity
import com.aviapp.app.security.applocker.ui.browser.BrowserActivity
import com.aviapp.app.security.applocker.ui.callblocker.CallBlockerActivity
import com.aviapp.app.security.applocker.ui.icon_changer.IconChangerActivity
import com.aviapp.app.security.applocker.ui.intruders.IntrudersPhotosActivity
import com.aviapp.app.security.applocker.ui.newpattern.CreateNewPatternActivity
import com.aviapp.app.security.applocker.ui.permissions.IntentHelper
import com.aviapp.app.security.applocker.ui.prem.PremActivity
import com.aviapp.app.security.applocker.ui.settings.SettingsActivity
import com.aviapp.app.security.applocker.ui.vault.VaultActivity
import com.aviapp.app.security.applocker.util.FirebaseEvents
import com.aviapp.app.security.applocker.util.dialogs.ModalBottomSheet
import com.aviapp.app.security.applocker.util.helper.NavigationIntentHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class NewMainActivity : BaseActivity<MainViewModel>() {

    override fun getViewModel(): Class<MainViewModel> = MainViewModel::class.java
    lateinit var drawerLayout: DrawerLayout
    lateinit var binding: ActivityNewMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = findViewById(R.id.drawer_layout)

        adId =
            if (!BuildConfig.DEBUG) "ca-app-pub-4875591253190011/4560402780" else "ca-app-pub-3940256099942544/1044960115"
        initAd()

        adHelper.adEventLiveData.observe(this) {
            if (it) {
                binding.bufferHolder.visibility = View.VISIBLE
            } else {
                binding.bufferHolder.visibility = View.GONE
            }
        }

        val prem = Prem(0,false)

        lifecycleScope.launch {
            database.premDao().updatePrem(prem)
        }

        FirebaseEvents.preland_screen_opened()

        viewModel.getPatternCreationNeedLiveData().observe(this) { isPatternCreateNeed ->
            when {
                isPatternCreateNeed -> {
                    startActivityForResult(
                        CreateNewPatternActivity.newIntent(
                            this,
                            CreateNewPatternActivity.TYPE_PATTERN
                        ),
                        RC_CREATE_PATTERN
                    )
                }
                else -> {
                }
            }
        }


        binding.themes.setOnClickListener {
            val intent = Intent(this, BackgroundsActivity::class.java)
            startActivity(intent)
        }


        binding.lockButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.vault.setOnClickListener {
            val intent = Intent(this, VaultActivity::class.java)
            startActivity(intent)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.privateBrowser.setOnClickListener {
            val intent = Intent(this, BrowserActivity::class.java)
            startActivity(intent)
        }

        adHelper.setOnClickListenerWithInter(binding.menu, lifecycleScope, 3) {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        adHelper.setOnClickListenerWithInter(
            findViewById<TextView>(R.id.btn_call_blocker),
            lifecycleScope
        ) {
            val intent = Intent(this@NewMainActivity, CallBlockerActivity::class.java)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        adHelper.setOnClickListenerWithInter(
            findViewById<TextView>(R.id.btn_browser),
            lifecycleScope
        ) {
            val intent = Intent(this, BrowserActivity::class.java)
            startActivity(intent)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        adHelper.setOnClickListenerWithInter(
            findViewById<TextView>(R.id.btn_theme),
            lifecycleScope
        ) {
            val intent = Intent(this, BackgroundsActivity::class.java)
            startActivity(intent)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        adHelper.setOnClickListenerWithInter(binding.themes, lifecycleScope) {
            val intent = Intent(this, BackgroundsActivity::class.java)
            startActivity(intent)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<TextView>(R.id.btn_removeads).setOnClickListener {
            PremActivity.start(this)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.fakeIcon.setOnClickListener {
            lifecycleScope.launch {
                startActivity(Intent(this@NewMainActivity, IconChangerActivity::class.java))
            }
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<TextView>(R.id.btn_rate).setOnClickListener {
            //RateDialog(this)
            ModalBottomSheet.show(this)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<View>(R.id.callBlockText).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(CallBlockerActivity.newIntent(this))
        }

        binding.selfieIntruder.setOnClickListener {
            startActivity(Intent(this@NewMainActivity, IntrudersPhotosActivity::class.java))
        }

        findViewById<View>(R.id.btn_contact_us).setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.type = "text/plain"
            val email = arrayOf("help.aviapp@gmail.com")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, email)
            emailIntent.setPackage("com.google.android.gm")
            try {
                startActivity(emailIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        findViewById<TextView>(R.id.btn_support).setOnClickListener {
            startActivity(NavigationIntentHelper.getShareAppIntent())
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<TextView>(R.id.menuSettings).setOnClickListener {
            startActivity(NavigationIntentHelper.getShareAppIntent())
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<TextView>(R.id.btn_privacy).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(IntentHelper.privacyPolicyWebIntent("https://docs.google.com/document/d/1iFdc5aWTrKoH0dOlhkgA0aWXbmr_HEP9XyALIzjXnUw/edit"))
        }

        adHelper.setOnClickListenerWithInter(
            findViewById<TextView>(R.id.menuSettings),
            lifecycleScope
        ) {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        val p0 = viewModel.userPreferencesRepository.appLockerPreferences.getLaunchCount()
        if ((p0 + 1) % 5 == 0) {
            if (!viewModel.userPreferencesRepository.appLockerPreferences.isRated()) {
                //RateDialog(this)
                ModalBottomSheet.show(this)
            }
        }

        lifecycleScope.launch {
            val p1 =
                withContext(Dispatchers.IO) { database.premDao().getPrem() ?: Prem() }.prIsNeeded
            if ((p0 + 1) % 3 == 0 && p1) {
                PremActivity.start(this@NewMainActivity)
            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_CREATE_PATTERN -> {
                viewModel.onAppLaunchValidated()
                showPrivacyPolicyIfNeeded()
                if (resultCode != Activity.RESULT_OK) {
                    finish()
                }
            }
            RC_VALIDATE_PATTERN -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.onAppLaunchValidated()
                    showPrivacyPolicyIfNeeded()
                } else {
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            finishAffinity()
        }
    }


    private fun showPrivacyPolicyIfNeeded() {
        /*if (viewModel.isPrivacyPolicyAccepted().not()) {
            val bb = PrivacyPolicyDialog.newInstance()
            bb.show(supportFragmentManager, "")
        }*/
    }

    override fun onResume() {
        super.onResume()
        /*if (PermissionChecker.isAllPermissionChecked(this).not()) {
            val intent = Intent(this, PermissionsActivity::class.java)
            startActivity(intent)
        } else {
            //showDialog()
        }*/
    }

    companion object {
        const val RC_CREATE_PATTERN = 2002
        const val RC_VALIDATE_PATTERN = 2003

        fun start(context: Context) {
            val intent = Intent(context, NewMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }


/*    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_share -> startActivity(NavigationIntentHelper.getShareAppIntent())
            R.id.nav_rate_us -> startActivity(NavigationIntentHelper.getRateAppIntent())
            R.id.nav_feedback -> startActivity(NavigationIntentHelper.getFeedbackIntent())
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }*/
}