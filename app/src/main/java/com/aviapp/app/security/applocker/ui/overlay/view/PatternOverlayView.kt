package com.aviapp.app.security.applocker.ui.overlay.view

import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.andrognito.patternlockview.PatternLockView
import com.aviapp.ads.AdHelper
import com.aviapp.app.security.applocker.BuildConfig
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.AppLockerPreferences
import com.aviapp.app.security.applocker.databinding.ViewPatternOverlayBinding
import com.aviapp.app.security.applocker.ui.background.GradientBackgroundDataProvider
import com.aviapp.app.security.applocker.ui.newpattern.SimplePatternListener
import com.aviapp.app.security.applocker.ui.overlay.OverlayValidateType
import com.aviapp.app.security.applocker.ui.overlay.OverlayViewState
import com.aviapp.app.security.applocker.util.FirebaseEvents
import com.aviapp.app.security.applocker.util.SaveFileUtil
import com.aviapp.app.security.applocker.util.VibrateUtils
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import kotlinx.android.synthetic.main.layout_pin_input_view.view.*
import kotlinx.android.synthetic.main.view_pattern_overlay.view.*
import kotlinx.coroutines.*

class PatternOverlayView @JvmOverloads constructor(
    context: Context,
    val listener: PinMatchedListener,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val fingerprintIdentify: FingerprintIdentify?,
    private val  adHelper: AdHelper
) : RelativeLayout(context, attrs, defStyleAttr), OnClickListener {

    private var onPatternCompleted: ((List<PatternLockView.Dot>) -> Unit)? = null
    private var appLockerPreferences = AppLockerPreferences(context)
    private var isPattherVisible = true

    val binding: ViewPatternOverlayBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.view_pattern_overlay,
            this,
            true
        )

    init {

        binding.patternLockView.addPatternLockListener(object : SimplePatternListener() {
            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                super.onComplete(pattern)
                pattern?.let { onPatternCompleted?.invoke(it) }
            }
        })

//        textViewPromptPin.text = context.getString(R.string.enter_pin)
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

//        Thread {
//            run {
//                if (appLockerPreferences.isPinEnabled()) {
//                    imgChangeLock.visibility = View.VISIBLE
//                }
//            }
//        }.start()
    }

    private fun getChangeLockListener(): OnClickListener {

        return OnClickListener {
            if (patternLockView.visibility == View.VISIBLE) {
                patternLockView.visibility = View.INVISIBLE
                textViewPrompt.visibility = View.INVISIBLE
                layoutPinView.visibility = View.VISIBLE
                imgChangeLock.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_pattern_lock
                    )
                )
                isPattherVisible = false
            } else {
                patternLockView.visibility = View.VISIBLE
                textViewPrompt.visibility = View.VISIBLE
                layoutPinView.visibility = View.INVISIBLE
                imgChangeLock.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
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
                textViewPromptPin.text = context.getString(R.string.pin_matched)
                texViewInput.text = ""
                listener.onPinMatched()
//                setResult(Activity.RESULT_OK)
            } else {
                textViewPromptPin.text = context.getString(R.string.incorrect_pin)
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

    lateinit var scope: CoroutineScope

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateSelectedBackground()

        FirebaseEvents.lock_above_another_app()

        binding.patternLockView.clearPattern()
        binding.viewState = OverlayViewState()

        textViewPromptPin.text = context.getString(R.string.enter_pin)
        if (appLockerPreferences.isPinEnabled()) {
            imgChangeLock.visibility = View.VISIBLE
        }

        if (appLockerPreferences.getFingerPrintEnabled()) {
            binding.appCompatImageView.visibility = View.VISIBLE
        }

        binding.appCompatImageView.visibility = if (appLockerPreferences.getFingerPrintEnabled()) View.VISIBLE else View.INVISIBLE

        scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            val adId = if (!BuildConfig.DEBUG) "ca-app-pub-4875591253190011/9906627997" else "ca-app-pub-3940256099942544/1044960115"
            adHelper.showNative(adId, null, binding.root, null, false)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope.cancel()
    }

    fun observePattern(onPatternCompleted: (List<PatternLockView.Dot>) -> Unit) {
        this.onPatternCompleted = onPatternCompleted
    }

    fun notifyDrawnWrong() {
        binding.patternLockView.clearPattern()
        binding.viewState =
            OverlayViewState(
                overlayValidateType = OverlayValidateType.TYPE_PATTERN,
                isDrawnCorrect = false
            )

        try {
            YoYo.with(Techniques.Shake)
                .duration(500)
                .playOn(binding.textViewPrompt)

            VibrateUtils.vibrate(context)
        } catch (e: Throwable) { }
    }

    fun notifyDrawnCorrect() {
        binding.patternLockView.clearPattern()
        binding.viewState = OverlayViewState(
            overlayValidateType = OverlayValidateType.TYPE_PATTERN,
            isDrawnCorrect = true
        )
    }

    fun setHiddenDrawingMode(isHiddenDrawingMode: Boolean) {
        binding.patternLockView.isInStealthMode = isHiddenDrawingMode
    }

    fun setAppPackageName(appPackageName: String) {
        try {
            val icon = context.packageManager.getApplicationIcon(appPackageName)
            //binding.avatarLock.setImageDrawable(icon)
        } catch (e: PackageManager.NameNotFoundException) {
            /*binding.avatarLock.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_round_lock_24px
                )
            )*/
            e.printStackTrace()
        }
    }

    private fun updateSelectedBackground() {
        val selectedBackgroundId = appLockerPreferences.getSelectedBackgroundId()
        GradientBackgroundDataProvider.gradientViewStateList.forEach {
            if (it.id == selectedBackgroundId) {
                binding.layoutOverlayMain.background = it.getGradiendDrawable(context)
                if (it.firebaseRef != null) {
                    val p2 = SaveFileUtil.getBitmap(it.firebaseRef, context)
                    if (p2 != null) {
                        binding.mainBack.setImageBitmap(p2)
                    }
                }
            }
        }
    }

    interface PinMatchedListener {
        fun onPinMatched()
    }

}