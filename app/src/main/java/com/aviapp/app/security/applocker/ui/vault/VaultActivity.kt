package com.aviapp.app.security.applocker.ui.vault

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.CheckBox
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.database.vault.VaultMediaType
import com.aviapp.app.security.applocker.databinding.ActivityVaultBinding
import com.aviapp.app.security.applocker.ui.BaseActivity
import com.aviapp.app.security.applocker.ui.vault.addingvaultdialog.AddToVaultDialog
import com.aviapp.app.security.applocker.ui.vault.intent.VaultSelectorIntentHelper
import com.aviapp.app.security.applocker.util.helper.file.FilePathHelper
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class VaultActivity : BaseActivity<VaultViewModel>() {

    private lateinit var binding: ActivityVaultBinding
    var start = true

    override fun getViewModel(): Class<VaultViewModel> = VaultViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_vault)
        initAd()

        binding.viewPagerVault.adapter = VaultPagerAdapter(this, this)

        adHelper.setOnClickListenerWithInter(
            binding.floatingActionButton,
            lifecycleScope,
            3,
            true
        ) {
            addToVaultClicked()
        }

        binding.goBack.setOnClickListener {
            onBackPressed()
        }

        binding.viewPagerVault.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (start) {
                    start = false
                } else {
                    handleScrollChangeListener(position, false)
                }
            }
        })

        binding.showAllApps.setOnClickListener { handleSelect(0) }
        binding.showLockApps.setOnClickListener { handleSelect(1) }
    }


    fun handleScrollChangeListener(position: Int, click: Boolean = true) {

        if (binding.viewPagerVault.currentItem == position && click) return

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
                    binding.showLockApps.animate().setDuration(180).setInterpolator(
                        DecelerateInterpolator()
                    ).scaleY(1f).scaleX(1f).alpha(1f).start()
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
                    binding.showAllApps.animate().setDuration(180).setInterpolator(
                        DecelerateInterpolator()
                    ).scaleY(1f).scaleX(1f).alpha(1f).start()
                }.start()
        }
    }

    private fun handleSelect(position: Int) {
        handleScrollChangeListener(position, true)
        binding.viewPagerVault.setCurrentItem(position, true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                RC_VAULT_SELECTION_IMAGE -> {
                    val pathList = arrayListOf<String?>()

                    if (data?.data != null) {
                        pathList.add(FilePathHelper.getSafePath(this, data.data ?: Uri.EMPTY))
                    } else if (data?.clipData != null) {

                        val selectedItemCount = data.clipData?.itemCount ?: 0
                        for (i in 0 until selectedItemCount) {
                            val imageUri = data.clipData?.getItemAt(i)?.uri
                            pathList.add(FilePathHelper.getSafePath(this, imageUri ?: Uri.EMPTY))
                        }
                    }

                    if (pathList.isEmpty().not()) {
                        AddToVaultDialog
                            .newInstance(pathList, VaultMediaType.TYPE_IMAGE)
                            .apply { onDismissListener = { showRateUsDialog() } }
                            .also { it.show(supportFragmentManager, "") }
                    }
                }

                RC_VAULT_SELECTION_VIDEO -> {
                    val path = FilePathHelper.getSafePath(this, data?.data ?: Uri.EMPTY)
                    val paths = arrayListOf<String?>().also { it.add(path) }

                    path?.let {
                        AddToVaultDialog
                            .newInstance(paths, VaultMediaType.TYPE_VIDEO)
                            .apply { onDismissListener = { showRateUsDialog() } }
                            .also { it.show(supportFragmentManager, "") }
                    }
                }

            }
        }
    }


    @SuppressLint("CheckResult")
    private fun addToVaultClicked() {

        RxPermissions(this)
            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { granted ->
                if (granted) {
                    when (binding.viewPagerVault.currentItem) {
                        0 -> {
                            startImage()
                        }
                        1 -> {
                            startVideo()
                        }
                        else -> {
                            startImage()
                        }
                    }
                }
            }
    }

    private fun startImage() {
        startActivityForResult(
            VaultSelectorIntentHelper.selectMultipleImageIntent(),
            RC_VAULT_SELECTION_IMAGE
        )
    }

    private fun startVideo() {
        startActivityForResult(
            VaultSelectorIntentHelper.selectVideoIntent(),
            RC_VAULT_SELECTION_VIDEO
        )
    }


    private fun showRateUsDialog() {

        if (viewModel.shouldShowRateUs()) {
            //RateUsDialog.newInstance().show(supportFragmentManager, "")
            //viewModel.setRateUsAsked()
        }
    }


    companion object {

        private const val RC_VAULT_SELECTION_IMAGE = 101
        private const val RC_VAULT_SELECTION_VIDEO = 102

        fun newIntent(context: Context): Intent {
            return Intent(context, VaultActivity::class.java)
        }
    }

}