package com.aviapp.app.security.applocker.ui.overlay.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.andrognito.patternlockview.PatternLockView
import com.aviapp.app.security.applocker.BuildConfig
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.AppLockerPreferences
import com.aviapp.app.security.applocker.databinding.ActivityOverlayValidationBinding
import com.aviapp.app.security.applocker.ui.BaseActivity
import com.aviapp.app.security.applocker.ui.background.GradientItemViewState
import com.aviapp.app.security.applocker.ui.intruders.camera.FrontPictureLiveData
import com.aviapp.app.security.applocker.ui.intruders.camera.FrontPictureState
import com.aviapp.app.security.applocker.ui.main.NewMainActivity
import com.aviapp.app.security.applocker.ui.newpattern.SimplePatternListener
import com.aviapp.app.security.applocker.ui.overlay.analytics.OverlayAnalytics
import com.aviapp.app.security.applocker.util.SaveFileUtil
import com.aviapp.app.security.applocker.util.VibrateUtils
import com.aviapp.app.security.applocker.util.extensions.convertToPatternDot
import com.aviapp.app.security.applocker.util.helper.file.FileManager
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.android.synthetic.main.activity_overlay_validation.*
import kotlinx.android.synthetic.main.layout_pin_input_view.*
import javax.inject.Inject


class OverlayValidationActivity : BaseActivity<OverlayValidationViewModel>(), View.OnClickListener {

    @Inject
    lateinit var fileManager: FileManager
    private lateinit var frontPictureLiveData: FrontPictureLiveData
    private lateinit var binding: ActivityOverlayValidationBinding
    private lateinit var appLockerPreferences: AppLockerPreferences
    private var isPattherVisible = true

    override fun getViewModel(): Class<OverlayValidationViewModel> =
        OverlayValidationViewModel::class.java

    var start = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_overlay_validation)

        init()
        adId = if (!BuildConfig.DEBUG) "ca-app-pub-4875591253190011/9906627997" else "ca-app-pub-3940256099942544/1044960115"
        initAd()

        try {
            updateLaunchingAppIcon(intent.getStringExtra(KEY_PACKAGE_NAME)!!)
        } catch (e: Throwable) { }


        start = intent.getBooleanExtra(START, false)

        frontPictureLiveData =
            FrontPictureLiveData(application, viewModel.getIntruderPictureImageFile())

        viewModel.getViewStateObservable().observe(this) {
            binding.patternLockView.clearPattern()
            binding.viewState = it
            binding.executePendingBindings()

            if (appLockerPreferences.getFingerPrintEnabled()) {
                if (it.isDrawnCorrect == true || it.fingerPrintResultData?.isSucces() == true) {
                    setResult(Activity.RESULT_OK)
                    okStat()
                }
            } else {
                if (it.isDrawnCorrect == true /*|| it.fingerPrintResultData?.isSucces() == true*/) {
                    setResult(Activity.RESULT_OK)
                    okStat()
                }
            }

            if (it.isDrawnCorrect == false || it.fingerPrintResultData?.isNotSucces() == true) {

                try {
                    YoYo.with(Techniques.Shake)
                        .duration(500)
                        .playOn(binding.textViewPrompt)
                } catch (e: Throwable) {
                }

                if (it.isIntrudersCatcherMode) {
                    try {
                        frontPictureLiveData.takePicture()
                        VibrateUtils.vibrate(this)
                    } catch (e: Throwable) {}
                }
            }
        }

        viewModel.getBackgroundDrawableLiveData().observe(this) {

            binding.mainBack.setImageDrawable(it.getGradiendDrawable(this))

            if (it.firebaseRef != null) {
                val p2 = SaveFileUtil.getBitmap(it.firebaseRef, this)
                if (p2 != null) {
                    binding.mainBack.setImageBitmap(p2)
                }
            }
        }

        binding.patternLockView.addPatternLockListener(object : SimplePatternListener() {
            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                super.onComplete(pattern)
                pattern?.let { viewModel.onPatternDrawn(it.convertToPatternDot()) }
            }
        })

        frontPictureLiveData.observe(this) {
            when (it) {
                is FrontPictureState.Taken -> OverlayAnalytics.sendIntrudersPhotoTakenEvent(this)
                is FrontPictureState.Error -> OverlayAnalytics.sendIntrudersCameraFailedEvent(this)
                else -> Log.e("FAFW", "FAW")
            }
        }
    }


    private fun okStat() {
        if (start)
            NewMainActivity.start(this)
        else
            finish()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent("android.intent.action.MAIN")
        intent.addCategory("android.intent.category.HOME")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }


    private fun updateLaunchingAppIcon(appPackageName: String) {
        try {
/*            val icon = packageManager.getApplicationIcon(appPackageName)
            binding.avatarLock.setImageDrawable(icon)*/
        } catch (e: PackageManager.NameNotFoundException) {
/*            binding.avatarLock.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_round_lock_24px
                )
            )*/
            e.printStackTrace()
        }
    }

    fun init() {
        appLockerPreferences = AppLockerPreferences(this)
        textViewPromptPin.text = getString(R.string.enter_pin)
        if (appLockerPreferences.isPinEnabled()) {
            imgChangeLock.visibility = View.VISIBLE
        }
        imgChangeLock.setOnClickListener(getChangeLockListener())
        textPad0.setOnClickListener(this)
        textPad1.setOnClickListener(this)
        textPad2.setOnClickListener(this)
        textPad3.setOnClickListener(this)
        textPad4.setOnClickListener(this)
        textPad5.setOnClickListener(this)
        textPad6.setOnClickListener(this)
        textPad7.setOnClickListener(this)
        textPad8.setOnClickListener(this)
        textPad9.setOnClickListener(this)
        imgBtnBackspace.setOnClickListener(this)
        imgBtnClear.setOnClickListener(this)
    }

    private fun getChangeLockListener(): View.OnClickListener {
        return View.OnClickListener {
            if (patternLockView.visibility == View.VISIBLE) {
                patternLockView.visibility = View.GONE
                textViewPrompt.visibility = View.GONE
                layoutPinView.visibility = View.VISIBLE
                imgChangeLock.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_pattern_lock
                    )
                )
                isPattherVisible = false
            } else {
                patternLockView.visibility = View.VISIBLE
                textViewPrompt.visibility = View.VISIBLE
                layoutPinView.visibility = View.GONE
                imgChangeLock.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_pin
                    )
                )
            }
            isPattherVisible = true
        }
    }

    private fun input(number: Int) {
        var input = texViewInput.text.toString()
        input = input.plus(number.toString())
        texViewInput.text = input
        if (input.length == 4) {
            val pin = appLockerPreferences.getMasterPin()
            if (pin == input) {
                textViewPromptPin.text = getString(R.string.pin_matched)
                setResult(Activity.RESULT_OK)
                okStat()
            } else {
                textViewPromptPin.text = getString(R.string.incorrect_pin)
                texViewInput.text = ""
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.textPad0 -> input(0)
            R.id.textPad1 -> input(1)
            R.id.textPad2 -> input(2)
            R.id.textPad3 -> input(3)
            R.id.textPad4 -> input(4)
            R.id.textPad5 -> input(5)
            R.id.textPad6 -> input(6)
            R.id.textPad7 -> input(7)
            R.id.textPad8 -> input(8)
            R.id.textPad9 -> input(9)
            R.id.imgBtnBackspace -> backpress()
            R.id.imgBtnClear -> texViewInput.text = ""
        }
    }

    private fun backpress() {
        if (texViewInput.text.isEmpty()) {
            return
        }

        var input = texViewInput.text.toString()
        input = input.substring(0, (input.length - 1))
        texViewInput.text = input
    }

    companion object {

        private const val KEY_PACKAGE_NAME = "KEY_PACKAGE_NAME"
        private const val START = "START"

        fun newIntent(context: Context, packageName: String): Intent {
            val intent = Intent(context, OverlayValidationActivity::class.java)
            intent.putExtra(KEY_PACKAGE_NAME, packageName)
            return intent
        }

        fun appStart(context: Context, packageName: String) {
            val intent = Intent(context, OverlayValidationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(KEY_PACKAGE_NAME, packageName)
            intent.putExtra(START, true)
            context.startActivity(intent)
        }
    }

}