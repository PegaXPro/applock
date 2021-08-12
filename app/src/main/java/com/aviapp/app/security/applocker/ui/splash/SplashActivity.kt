package com.aviapp.app.security.applocker.ui.splash

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.ui.BaseActivity
import com.aviapp.app.security.applocker.ui.icon_changer.IconChangerActivity
import com.aviapp.app.security.applocker.ui.main.MainViewModel
import com.aviapp.app.security.applocker.ui.main.NewMainActivity
import com.aviapp.app.security.applocker.ui.newpattern.CreateNewPatternActivity
import com.aviapp.app.security.applocker.ui.overlay.activity.OverlayValidationActivity
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity<MainViewModel>() {


    override fun getViewModel(): Class<MainViewModel> = MainViewModel::class.java
    lateinit var sharedPref: SharedPreferences
    lateinit var mainIcon: ImageView
    lateinit var appName: TextView
    lateinit var someText: TextView
    val animation = CompletableDeferred<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        mainIcon = findViewById(R.id.mainIcon)
        appName = findViewById(R.id.app_name)
        someText = findViewById(R.id.someText)

        animateView(mainIcon)
        animateView(appName, delay = 20)
        animateView(someText, delay = 30) {
            animation.complete(true)
        }

        navigate()
    }



    private fun animateView(view: View, delay: Long = 0, endAction: (() -> Unit)? = null) = lifecycleScope.launch {
        delay(delay)
        view.animate().scaleY(1f).scaleX(1f).translationY(0f).alpha(1f).setDuration(400).setInterpolator(DecelerateInterpolator()).withEndAction { endAction?.invoke() }.start()
    }

    private fun navigate() = lifecycleScope.launch {

        viewModel.getPatternCreationNeedLiveData().observe(this@SplashActivity, { isPatternCreateNeed ->

            when {
                !isPatternCreateNeed -> {
                    lifecycleScope.launch {
                        animation.await()
                        splashProgress.visibility = View.VISIBLE
                        delay(100)
                        adHelper.showInterWhenLoaded(this@SplashActivity) {
                            OverlayValidationActivity.appStart(
                                this@SplashActivity,
                                "com.aviapp.app.security.applocker"
                            )
                        }
                    }
                }

                else -> {
                    lifecycleScope.launch {
                        animation.await()
                        delay(150)
                        startActivityForResult(
                            CreateNewPatternActivity.newIntent(
                                this@SplashActivity,
                                CreateNewPatternActivity.TYPE_PATTERN
                            ),
                            NewMainActivity.RC_CREATE_PATTERN
                        )
                    }

                }
            }
        })
    }


    /*private fun showIcon() {

        lifecycleScope.launch {
            delay(350)
            when (sharedPref.getString("appIcon", IconChangerActivity.AppIcon.main6.name)) {
                IconChangerActivity.AppIcon.main1.name -> {
                    mainIcon.setImageResource(R.drawable.ic_main_logo1)
                    appName.setText(R.string.app_name1)
                }
                IconChangerActivity.AppIcon.main2.name -> {
                    mainIcon.setImageResource(R.drawable.ic_main_logo2)
                    appName.setText(R.string.app_name2)
                }
                IconChangerActivity.AppIcon.main3.name -> {
                    mainIcon.setImageResource(R.drawable.ic_main_logo3)
                    appName.setText(R.string.app_name3)
                }
                IconChangerActivity.AppIcon.main4.name -> {
                    mainIcon.setImageResource(R.drawable.ic_main_logo4)
                    appName.setText(R.string.app_name4)
                }
                IconChangerActivity.AppIcon.main5.name -> {
                    mainIcon.setImageResource(R.drawable.ic_main_logo5)
                    appName.setText(R.string.app_name5)
                }
                IconChangerActivity.AppIcon.main6.name -> {
                    mainIcon.setImageResource(R.drawable.ic_main_logo8)
                    appName.setText(R.string.app_name)
                }
            }
        }
    }*/


}