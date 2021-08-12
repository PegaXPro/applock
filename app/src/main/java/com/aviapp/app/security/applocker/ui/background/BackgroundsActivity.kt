package com.aviapp.app.security.applocker.ui.background

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.databinding.ActivityBackgroundsBinding
import com.aviapp.app.security.applocker.ui.BaseActivity

class BackgroundsActivity : BaseActivity<BackgroundsActivityViewModel>() {

    private lateinit var binding: ActivityBackgroundsBinding

    override fun getViewModel(): Class<BackgroundsActivityViewModel> = BackgroundsActivityViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_backgrounds)

        initAd()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.containerBackgrounds, BackgroundsFragment.newInstance())
                .commitAllowingStateLoss()
        }

        binding.goBack.setOnClickListener {
            onBackPressed()
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, BackgroundsActivity::class.java)
        }
    }
}