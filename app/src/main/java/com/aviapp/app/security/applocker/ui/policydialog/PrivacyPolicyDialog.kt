package com.aviapp.app.security.applocker.ui.policydialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.aviapp.ads.AdHelper
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.databinding.DialogPrivacyPolicyBinding
import com.aviapp.app.security.applocker.ui.BaseBottomSheetDialog
import com.aviapp.app.security.applocker.ui.permissions.IntentHelper
import com.aviapp.app.security.applocker.ui.policydialog.analytics.PrivacyPolicyAnalytics
import com.aviapp.app.security.applocker.util.delegate.inflate
import kotlinx.coroutines.launch
import javax.inject.Inject

class PrivacyPolicyDialog : BaseBottomSheetDialog<PrivacyPolicyViewModel>() {

    private val binding: DialogPrivacyPolicyBinding by inflate(R.layout.dialog_privacy_policy)

    @Inject
    lateinit var adHandler: AdHelper

    override fun getViewModel(): Class<PrivacyPolicyViewModel> = PrivacyPolicyViewModel::class.java

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.buttonAccept.setOnClickListener {
            activity?.let { PrivacyPolicyAnalytics.sendPrivacyPolicyAccept(it) }
            viewModel.acceptPrivacyPolicy()
            dismiss()
            lifecycleScope.launch {
                adHandler.showInterstitial(requireActivity()) {}
            }
        }

        binding.textViewPrivacyPolicy.setOnClickListener {
            startActivity(IntentHelper.privacyPolicyWebIntent("https://docs.google.com/document/d/1iFdc5aWTrKoH0dOlhkgA0aWXbmr_HEP9XyALIzjXnUw/edit"))
        }

        return binding.root
    }

    companion object {
        fun newInstance(): DialogFragment {
            val dialog = PrivacyPolicyDialog()
            dialog.isCancelable = false
            return dialog
        }
    }

}