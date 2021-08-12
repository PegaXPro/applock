package com.aviapp.app.security.applocker.ui.intruders

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.database.prem.Prem
import com.aviapp.app.security.applocker.databinding.ActivityIntrudersPhotosBinding
import com.aviapp.app.security.applocker.ui.BaseActivity
import com.aviapp.app.security.applocker.ui.prem.PremActivity
import com.aviapp.app.security.applocker.util.delegate.contentView
import com.aviapp.app.security.applocker.util.getToggleState
import com.aviapp.app.security.applocker.util.permissions.CheckWriteStorage
import com.aviapp.app.security.applocker.util.setToggleState
import com.aviapp.app.security.applocker.util.setToggleStateAnimated
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IntrudersPhotosActivity : BaseActivity<IntrudersPhotosViewModel>() {

    @Inject
    lateinit var intrudersListAdapter: IntrudersListAdapter
    private val binding: ActivityIntrudersPhotosBinding by contentView(R.layout.activity_intruders_photos)

    override fun getViewModel(): Class<IntrudersPhotosViewModel> =
        IntrudersPhotosViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initAd()

        intrudersListAdapter.viewModel = viewModel
        binding.recyclerViewIntrudersPhotosList.adapter = intrudersListAdapter
        binding.goBack.setOnClickListener { onBackPressed() }


        viewModel.getIntruderListViewState().observe(this, {
            intrudersListAdapter.updateIntruderList(it.intruderPhotoItemViewStateList)
            binding.viewState = it
            binding.executePendingBindings()
        })

        viewModel.settingsViewStateLiveData.value?.isIntrudersCatcherEnabled ?: false

        viewModel.settingsViewStateLiveData.observe(this) {
            val p0 = it.isIntrudersCatcherEnabled
            if (p0 != binding.intruderSwitch.getToggleState()) {
                binding.intruderSwitch.setToggleStateAnimated(p0)
            }
        }

        binding.intruderSwitch.setOnClickListener {
            val p0 = binding.intruderSwitch.getToggleState()
            if (!p0) {

                lifecycleScope.launch {
                    val p1 = withContext(Dispatchers.IO) {database.premDao().getPrem()?: Prem()}
                    if (p1.prIsNeeded) {
                        startActivity(Intent(this@IntrudersPhotosActivity, PremActivity::class.java))
                    } else {
                        CheckWriteStorage.check(this@IntrudersPhotosActivity) {
                            if (it)
                                enableIntrudersCatcher(true)
                            else
                                enableIntrudersCatcher(false)
                        }
                    }
                }

            } else {
                enableIntrudersCatcher(false)
            }
        }


    }


    @SuppressLint("CheckResult")
    private fun enableIntrudersCatcher(isChecked: Boolean) {
        if (isChecked) {
            try {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_CONTACTS
                    ) != PackageManager.PERMISSION_GRANTED -> {
                        RxPermissions(this)
                            .request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe { granted ->
                                viewModel.setEnableIntrudersCatchers(granted)
                            }
                    } else -> {  viewModel.setEnableIntrudersCatchers(true) }
                }

            } catch (e: Throwable) {

            }

        } else {
            viewModel.setEnableIntrudersCatchers(false)
        }
    }


    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, IntrudersPhotosActivity::class.java)
        }
    }
}