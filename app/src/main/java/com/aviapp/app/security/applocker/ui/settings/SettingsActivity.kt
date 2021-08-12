package com.aviapp.app.security.applocker.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.ui.BaseActivity
import com.aviapp.app.security.applocker.ui.main.MainViewModel

class SettingsActivity: BaseActivity<MainViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun getViewModel(): Class<MainViewModel> = MainViewModel::class.java

}