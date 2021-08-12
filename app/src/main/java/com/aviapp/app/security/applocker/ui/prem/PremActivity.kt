package com.aviapp.app.security.applocker.ui.prem

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.ui.BaseActivity
import com.aviapp.app.security.applocker.ui.main.MainViewModel

class PremActivity: BaseActivity<MainViewModel>() {

    override fun getViewModel() = MainViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)

        prService.priseLiveData.observe(this) {
            findViewById<TextView>(R.id.subSum).text = "for $it"
        }

        findViewById<View>(R.id.subButton).setOnClickListener {
            prService.createPurchase(this)
        }

        findViewById<View>(R.id.goBack).setOnClickListener {
            finish()
        }
    }

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, PremActivity::class.java)
            activity.startActivity(intent)
        }
    }

}