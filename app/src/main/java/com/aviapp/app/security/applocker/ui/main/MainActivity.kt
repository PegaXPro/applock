package com.aviapp.app.security.applocker.ui.main

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.aviapp.app.security.applocker.BuildConfig
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.databinding.ActivityMainBinding
import com.aviapp.app.security.applocker.ui.BaseActivity
import com.aviapp.app.security.applocker.ui.security.SecurityFragment
import com.aviapp.app.security.applocker.ui.security.SecurityFragmentProtected
import com.aviapp.app.security.applocker.ui.settings.SettingsActivity
import com.aviapp.app.security.applocker.util.FirebaseEvents
import org.koin.android.ext.android.bind


class MainActivity : BaseActivity<MainViewModel>() {

    private lateinit var binding: ActivityMainBinding
    override fun getViewModel(): Class<MainViewModel> = MainViewModel::class.java
    var start = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        //adId = if (!BuildConfig.DEBUG) "ca-app-pub-4875591253190011/4560402780" else "ca-app-pub-3940256099942544/1044960115"
        initAd()

        FirebaseEvents.lock_apps_opened()

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> {
                        SecurityFragment.newInstance()
                    }
                    else -> {
                        SecurityFragmentProtected.newInstance()
                    }
                }
            }
        }

        binding.goBack.setOnClickListener {
            onBackPressed()
        }

        binding.goToSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.showAllApps.setOnClickListener { handleSelect(0) }
        binding.showLockApps.setOnClickListener { handleSelect(1) }
        binding.viewPagger.adapter = adapter

        binding.viewPagger.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (start) {
                    start = false
                } else {
                    handleScrollChangeListener(position, false)
                }
            }
        })

        // Permissions permitted for lock all apps
        if (intent.getIntExtra("allAppsLocked", 0) == 1) {
            binding.viewPagger.setCurrentItem(1, false)
            handleScrollChangeListener(1, false)
            showPopup()
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, NewMainActivity::class.java))
    }

    private fun showPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_all_apps_locked)

        val button = dialog.findViewById<Button>(R.id.okButtonWhenLockedApps)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        button.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun handleSelect(position: Int) {
        handleScrollChangeListener(position, true)
        binding.viewPagger.setCurrentItem(position, true)
    }

    fun handleScrollChangeListener(position: Int, click: Boolean = true) {


        if (binding.viewPagger.currentItem == position && click) return

        if (position == 1) {
            binding.showAllApps.alpha = 1f
            binding.showAllApps.scaleX = 1f
            binding.showAllApps.scaleY = 1f

            binding.showAllApps
                .animate()
                .setInterpolator(AccelerateInterpolator())
                .setDuration(100)
                .scaleY(0.7f)
                .scaleX(0.7f)
                .alpha(0f)
                .withEndAction {
                    binding.text1.setTextColor(Color.parseColor("#959595"))
                    binding.text2.setTextColor(Color.parseColor("#ffffff"))
                    binding.showLockApps.alpha = 0f
                    binding.showLockApps.scaleY = 0.7f
                    binding.showLockApps.scaleX = 0.7f
                    binding.showLockApps.setBackgroundResource(R.drawable.block_select_back)
                    binding.showLockApps.animate().setDuration(180)
                        .setInterpolator(DecelerateInterpolator()).scaleY(1f).scaleX(1f).alpha(1f)
                        .start()
                }.start()
        } else {

            binding.showLockApps.alpha = 1f
            binding.showLockApps.scaleX = 1f
            binding.showLockApps.scaleY = 1f

            binding.showLockApps
                .animate()
                .setInterpolator(AccelerateInterpolator())
                .setDuration(100)
                .scaleY(0.7f)
                .scaleX(0.7f)
                .alpha(0f)
                .withEndAction {
                    binding.text2.setTextColor(Color.parseColor("#959595"))
                    binding.text1.setTextColor(Color.parseColor("#ffffff"))
                    binding.showAllApps.alpha = 0f
                    binding.showAllApps.scaleY = 0.7f
                    binding.showAllApps.scaleX = 0.7f
                    binding.showAllApps.setBackgroundResource(R.drawable.block_select_back)
                    binding.showAllApps.animate().setDuration(180)
                        .setInterpolator(DecelerateInterpolator()).scaleY(1f).scaleX(1f).alpha(1f)
                        .start()
                }.start()
        }
    }

    companion object {
        private const val RC_CREATE_PATTERN = 2002
        private const val RC_VALIDATE_PATTERN = 2003
    }
}
