package com.aviapp.app.security.applocker.ui.callblocker

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.database.callblocker.addtoblacklist.AddToBlackListDialog
import com.aviapp.app.security.applocker.databinding.ActivityCallBlockerBinding
import com.aviapp.app.security.applocker.ui.BaseActivity
import com.wafflecopter.multicontactpicker.ContactResult
import com.wafflecopter.multicontactpicker.LimitColumn
import com.wafflecopter.multicontactpicker.MultiContactPicker
import kotlinx.android.synthetic.main.activity_call_blocker.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CallBlockerActivity : BaseActivity<CallBlockerViewModel>() {

    private val CONTACT_PICKER_REQUEST: Int = 1234
    private lateinit var binding: ActivityCallBlockerBinding
    var start = true

    lateinit var pageCallBack: ViewPager2.OnPageChangeCallback

    override fun getViewModel(): Class<CallBlockerViewModel> = CallBlockerViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED -> ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                20
            )
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_call_blocker)
        binding.viewPager.adapter = CallBlockerPagerAdapter(this)

        pageCallBack =  object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                try {

                    if (start) {
                        start = false
                    } else {
                        handleScrollChangeListener(position, false)
                    }

                    when (position) {
                        0 -> {
                            binding.buttonBlockNumber.visibility = View.VISIBLE
                            binding.addManual.visibility = View.VISIBLE
                            binding.fromContact.visibility = View.VISIBLE
                        }

                        1 -> {
                            binding.buttonBlockNumber.visibility = View.GONE
                            binding.addManual.visibility = View.GONE
                            binding.fromContact.visibility = View.GONE
                        }

                        2 -> {
                            binding.buttonBlockNumber.visibility = View.GONE
                            binding.addManual.visibility = View.GONE
                            binding.fromContact.visibility = View.GONE
                        }

                    }
                } catch (e: Throwable) {

                }

            }
        }

        binding.viewPager.registerOnPageChangeCallback(pageCallBack)

        initAd()

        binding.addManual.setOnClickListener {
            showPopup()
            showAddToBlacklistDialog("", "")
        }

        binding.fromContact.setOnClickListener {
            showPopup()
            contactPicker()
        }

        binding.goBack.setOnClickListener{
            onBackPressed()
        }

        binding.buttonBlockNumber.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 20)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    setDefaulDialer(this)
                }
                else -> {
        //                showAddToBlacklistDialog()
                    showPopup()
                }
            }
        }

        binding.showAllApps.setOnClickListener { handleSelect(0) }
        binding.showLockApps.setOnClickListener { handleSelect(1) }
    }

    override fun onDestroy() {
        viewPager.unregisterOnPageChangeCallback(pageCallBack)
        super.onDestroy()
    }

    private fun handleSelect(position: Int) {
        handleScrollChangeListener(position, true)
        binding.viewPager.setCurrentItem(position, true)
    }

    fun handleScrollChangeListener(position: Int, click: Boolean = true) {

        if (binding.viewPager.currentItem == position && click) return

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

    private fun showPopup() {
        binding.buttonBlockNumber.animate().rotation(binding.buttonBlockNumber.rotation + 90).setInterpolator(OvershootInterpolator()).start()
        animateShowItem(binding.addManual, 40)
        animateShowItem(binding.fromContact, 0)
    }

    private fun animateShowItem(view: View, delayTime: Long = 0) {
        lifecycleScope.launch {
            delay(delayTime)
            if (view.alpha < 1f) {
                view.alpha = 0f
                view.scaleY = 1.4f
                view.translationY = 50f
                view.animate().setInterpolator(OvershootInterpolator()).alpha(1f).scaleY(1f).translationY(0f).start()
            } else {
                view.alpha = 1f
                view.scaleY = 1f
                view.translationY = 0f
                view.animate().setInterpolator(OvershootInterpolator()).alpha(0f).scaleY(1.4f).translationY(50f).start()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setDefaulDialer(context: Context) {

        if (getSystemService(TelecomManager::class.java).defaultDialerPackage != packageName) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val rm: RoleManager = context.getSystemService(RoleManager::class.java)
                if (rm.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                    if (rm.isRoleHeld(RoleManager.ROLE_DIALER)) {
                        Log.d("role", "role");
                    } else {
                        val roleRequestIntent: Intent = rm.createRequestRoleIntent(
                            RoleManager.ROLE_DIALER
                        )
                        startActivityForResult(
                            roleRequestIntent,
                            CHANGE_DIALER_REQUEST_CODE
                        )
                    }
                }
            } else {
                Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                    .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                    .also { startActivityForResult(it, CHANGE_DIALER_REQUEST_CODE) }
            }
        } else {
//            showAddToBlacklistDialog()
            showPopup()
        }
    }


    private fun contactPicker() {


        MultiContactPicker.Builder(this@CallBlockerActivity) //Activity/fragment context
            .theme(R.style.MyCustomPickerTheme) //Optional - default: MultiContactPicker.Azure
            .hideScrollbar(false) //Optional - default: false
            .showTrack(true) //Optional - default: true
            .bubbleColor(Color.WHITE)
            .bubbleTextColor(Color.WHITE)
            .handleColor(Color.WHITE)
            .searchIconColor(Color.WHITE) //Option - default: White
            .setChoiceMode(/*MultiContactPicker.CHOICE_MODE_MULTIPLE*/MultiContactPicker.CHOICE_MODE_SINGLE) //Optional - default: CHOICE_MODE_MULTIPLE
            .handleColor(
                ContextCompat.getColor(
                    this@CallBlockerActivity,
                    R.color.colorPrimary
                )
            ) //Optional - default: Azure Blue
            .bubbleColor(
                ContextCompat.getColor(
                    this@CallBlockerActivity,
                    R.color.colorPrimary
                )
            ) //Optional - default: Azure Blue
            .bubbleTextColor(Color.WHITE) //Optional - default: White
            .setTitleText("Select Contacts") //Optional - default: Select Contacts
            /*  .setSelectedContacts(
                  "10",
                  "5" // myList
              ) //Optional - will pre-select contacts of your choice. String... or List<ContactResult>*/
            .setLoadingType(MultiContactPicker.LOAD_ASYNC) //Optional - default LOAD_ASYNC (wait till all loaded vs stream results)
            .limitToColumn(LimitColumn.NONE) //Optional - default NONE (Include phone + email, limiting to one can improve loading time)
            .setActivityAnimations(
                android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            ) //Optional - default: No animation overrides
            .showPickerForResult(CONTACT_PICKER_REQUEST)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == CHANGE_DIALER_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                showPopup()
            }

            requestCode == CONTACT_PICKER_REQUEST && resultCode == Activity.RESULT_OK -> {

                val results: List<ContactResult> = MultiContactPicker.obtainResult(data)
                Log.d("MyTag", results[0].displayName)
                Log.d("MyTag", results[0].phoneNumbers.get(0).number)
                showAddToBlacklistDialog(
                    results[0].displayName,
                    results[0].phoneNumbers.get(0).number
                )


                /*if (resultCode === Activity.RESULT_OK) {

                } else if (resultCode === Activity.RESULT_CANCELED) {
                    println("User closed the picker without selecting items.")
                }*/
            }
        }
    }

    private fun showAddToBlacklistDialog(name: String, number: String) {
        val bottomSheetFragment = AddToBlackListDialog.newInstance()
        val bundle = Bundle()
        bundle.putString("name", name)
        bundle.putString("number", number)
        bottomSheetFragment.arguments = bundle
        bottomSheetFragment.show(supportFragmentManager, "")
    }

    companion object {
        private const val CHANGE_DIALER_REQUEST_CODE = 1001
        fun newIntent(context: Context): Intent {
            return Intent(context, CallBlockerActivity::class.java)
        }
    }


}