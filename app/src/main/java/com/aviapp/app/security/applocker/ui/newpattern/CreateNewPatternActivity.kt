package com.aviapp.app.security.applocker.ui.newpattern

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.andrognito.patternlockview.PatternLockView
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.AppLockerPreferences
import com.aviapp.app.security.applocker.databinding.ActivityCreateNewPatternBinding
import com.aviapp.app.security.applocker.ui.BaseActivity
import com.aviapp.app.security.applocker.ui.main.NewMainActivity
import com.aviapp.app.security.applocker.util.FirebaseEvents
import kotlinx.android.synthetic.main.activity_overlay_validation.*
import kotlinx.android.synthetic.main.layout_pin_input_view.*

class CreateNewPatternActivity : BaseActivity<CreateNewPatternViewModel>(), View.OnClickListener {

    private lateinit var binding: ActivityCreateNewPatternBinding
    private lateinit var appLockerPreferences: AppLockerPreferences
    private var isFirstTimeSet = true
    private var firstInput: String? = null
    private var isPattherVisible = true

    private var TAG = "CreateNewPatternActivity"

    override fun getViewModel(): Class<CreateNewPatternViewModel> =
        CreateNewPatternViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_new_pattern)
        Log.d(TAG, "onCreate")

        init()

        initPatternView()

        val lockType = intent.getStringExtra(LOCK_TYPE)
        if (lockType == TYPE_PATTERN) {
            updateView(binding.patternLockView, binding.layoutPinView)
        } else {
            updateView(binding.layoutPinView, binding.patternLockView)
            binding.textViewPrompt.visibility = View.GONE
        }

    }

    private fun updateView(toShow: View, toHide: View) {
        toShow.visibility = View.VISIBLE
        toHide.visibility = View.GONE
    }

    private fun initPatternView() {
        binding.patternLockView.addPatternLockListener(object : SimplePatternListener() {
            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                if (viewModel.isFirstPattern()) {
                    viewModel.setFirstDrawedPattern(pattern)
                } else {
                    viewModel.setRedrawnPattern(pattern)
                }
                binding.patternLockView.clearPattern()
            }
        })

        viewModel.getPatternEventLiveData().observe(this, Observer { viewState ->
            binding.viewState = viewState
            binding.executePendingBindings()

            if (viewState.isCreatedNewPattern()) {
                onPatternCreateCompleted()
            }
        })
    }

    private fun onPatternCreateCompleted() {
        setResult(Activity.RESULT_OK)
        FirebaseEvents.pattern_sucess()
        NewMainActivity.start(this)
        //finish()
    }

    private fun input(number: Int) {
        var input = texViewInput.text.toString()
        input = input.plus(number.toString())

        texViewInput.text = input

        if (input.length == 4) {
            if (isFirstTimeSet) {
                texViewInput.text = ""
                textViewPromptPin.text = getString(R.string.confirm_pin)
                firstInput = input
                isFirstTimeSet = false
            } else {
                if (firstInput == input) {
                    textViewPromptPin.text = getString(R.string.pin_chahnged)
                    appLockerPreferences.setMasterPin(input)
                    if (!appLockerPreferences.isPinEnabled()) {
                        appLockerPreferences.setPinEnabled()
                    }
                    setResult(Activity.RESULT_OK)
                    FirebaseEvents.pattern_sucess()
                    NewMainActivity.start(this)
                    //finish()
                } else {
                    textViewPromptPin.text = getString(R.string.pin_not_matched)
                    isFirstTimeSet = true
                    texViewInput.text = ""
                }
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
            R.id.imgBtnBackspace -> backPress()
            R.id.imgBtnClear -> texViewInput.text = ""
        }
    }

    private fun backPress() {
        if (texViewInput.text.isEmpty()) {
            return
        }

        var input = texViewInput.text.toString()
        input = input.substring(0, (input.length - 1))
        texViewInput.text = input
    }

    fun init() {
//        imgChangeLock.setOnClickListener(getChangeLockListener())
        appLockerPreferences = AppLockerPreferences(this)
        if (appLockerPreferences.isPinEnabled()) {
            textViewPromptPin.text = getString(R.string.create_new_pin)
        }
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

    private fun getChangeLockListener(): View.OnClickListener? {
        return View.OnClickListener {
            if (patternLockView.visibility == View.VISIBLE) {
                patternLockView.visibility = View.GONE
                textViewPrompt.visibility = View.GONE
                layoutPinView.visibility = View.VISIBLE
//                imgChangeLock.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        this,
//                        R.drawable.ic_pattern_lock
//                    )
//                )
                isPattherVisible = false
            } else {
                patternLockView.visibility = View.VISIBLE
                textViewPrompt.visibility = View.VISIBLE
                layoutPinView.visibility = View.GONE
//                imgChangeLock.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        this,
//                        R.drawable.ic_pin
//                    )
//                )
            }
            isPattherVisible = true
        }
    }

    companion object {
        const val LOCK_TYPE = "TYPE"
        const val TYPE_PATTERN = "TYPE_PATTERN"
        const val TYPE_PIN = "TYPE_PIN"

        @JvmStatic
        fun newIntent(context: Context, type: String): Intent {
            val intent = Intent(context, CreateNewPatternActivity::class.java)
            intent.putExtra(LOCK_TYPE, type)
            return intent
        }

    }
}