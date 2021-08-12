package com.aviapp.app.security.applocker.ui.icon_changer

import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.aviapp.app.security.applocker.BuildConfig
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.database.prem.Prem
import com.aviapp.app.security.applocker.ui.BaseActivity
import com.aviapp.app.security.applocker.ui.main.MainViewModel
import com.aviapp.app.security.applocker.ui.prem.PremActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.relex.circleindicator.CircleIndicator3

class IconChangerActivity : BaseActivity<MainViewModel>(),
    IconPagerAdapter.OnCheckIconClickListener {
    lateinit var back: ImageView
    lateinit var sharedPref: SharedPreferences
    lateinit var iconsViewPager: ViewPager2
    lateinit var icons: List<Int>
    lateinit var titles: List<Int>
    private lateinit var appLocks: List<String>
    lateinit var checkedBox: MutableList<Boolean>
    private lateinit var iconPageIndicator: CircleIndicator3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_icon_changer)
        initViews()
        initAd()

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        icons = listOf(
            R.drawable.ic_main_logo8,
            R.drawable.ic_main_logo1,
            R.drawable.ic_main_logo2,
            R.drawable.ic_main_logo3,
            R.drawable.ic_main_logo4,
            R.drawable.ic_main_logo5
        )

        titles = listOf(
            R.string.app_name,
            R.string.app_name1,
            R.string.app_name2,
            R.string.app_name3,
            R.string.app_name4,
            R.string.app_name5,
        )

        appLocks = listOf(
            "main6",
            "main1",
            "main2",
            "main3",
            "main4",
            "main5"
        )
        checkedBox = mutableListOf(false, false, false, false, false, false)

        when(sharedPref.getString("appIcon", "AppLock")) {
            getString(titles[0]) -> checkedBox[0] = true
            getString(titles[1]) -> checkedBox[1] = true
            getString(titles[2]) -> checkedBox[2] = true
            getString(titles[3]) -> checkedBox[3] = true
            getString(titles[4]) -> checkedBox[4] = true
            getString(titles[5]) -> checkedBox[5] = true
        }

        val adapter = IconPagerAdapter(checkedBox, titles, icons)
        adapter.setOnCardClickListener(this)
        
        iconsViewPager.adapter = adapter
        iconsViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        iconPageIndicator.setViewPager(iconsViewPager)

        back.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onCheckIconClick(view: View, position: Int) {
        itemClick(getString(titles[position]), position)
    }

    private fun itemClick(appIcon: String, position: Int) {
        lifecycleScope.launch {
            val isPremium = withContext(Dispatchers.IO) { database.premDao().getPrem() ?: Prem() }
            if (appIcon == "AppLock") {
                if (sharedPref.getString("appIcon", "AppLock") != "AppLock") {
                    sharedPref.edit().putString("appIcon", appIcon).apply()
                    setIcon(position)
                }
            } else {
                if (isPremium.prIsNeeded) {
                    startActivity(Intent(this@IconChangerActivity, PremActivity::class.java))
                } else {
                    sharedPref.edit().putString("appIcon", appIcon).apply()
                    setIcon(position)
                }
            }
        }
    }


    private fun setIcon(position: Int) {
        iconsViewPager.isUserInputEnabled = false
        for (value in appLocks) {
            val action = if (value == appLocks[position]) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            try {
                packageManager.setComponentEnabledSetting(
                    ComponentName(
                        BuildConfig.APPLICATION_ID,
                        "com.aviapp.app.security.applocker.${value}"
                    ),
                    action, PackageManager.DONT_KILL_APP
                )
            } catch (e: Throwable) {
                Log.d("SDFSGESGSG", "!!!!! ${e.message}")
            }
        }
    }


    /*private fun clickAnimation(icon: ImageView, iconBack: ImageView) {

        if (icon.translationY == 0f) {
            icon.animate().translationY(-20f).setInterpolator(AccelerateDecelerateInterpolator()).setDuration(
                100
            ).start()
            iconBack.animate().alpha(0f).scaleY(0.8f).scaleX(0.8f).setDuration(50).setInterpolator(
                AccelerateInterpolator()
            ).withEndAction {
                iconBack.setImageResource(R.drawable.selected_icon_back)
                iconBack.animate().alpha(1f).scaleY(1f).scaleX(1f).setDuration(100).setInterpolator(
                    DecelerateInterpolator()
                ).start()
            }.start()
        } else {
            icon.animate().translationY(0f).setInterpolator(AccelerateDecelerateInterpolator()).setDuration(100).start()
            iconBack.animate().alpha(0f).scaleY(0.8f).scaleX(0.8f).setDuration(50).setInterpolator(AccelerateInterpolator()).withEndAction {
                iconBack.setImageResource(R.drawable.back6)
                iconBack.animate().alpha(1f).scaleY(1f).scaleX(1f).setDuration(100).setInterpolator(
                    DecelerateInterpolator()
                ).start()
            }.start()
        }
    }*/


    private fun initViews() {
        back = findViewById(R.id.goBack)
        iconsViewPager = findViewById(R.id.icons_viewpager)
        iconPageIndicator = findViewById(R.id.icon_change_indicator)
    }

    override fun getViewModel(): Class<MainViewModel> = MainViewModel::class.java
}