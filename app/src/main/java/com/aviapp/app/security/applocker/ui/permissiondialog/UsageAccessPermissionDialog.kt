package com.aviapp.app.security.applocker.ui.permissiondialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.databinding.DialogUsagePermissionBinding
import com.aviapp.app.security.applocker.ui.BaseBottomSheetDialog
import com.aviapp.app.security.applocker.ui.permissiondialog.analytics.PermissionDialogAnayltics
import com.aviapp.app.security.applocker.ui.permissions.IntentHelper
import com.aviapp.app.security.applocker.util.delegate.inflate

class UsageAccessPermissionDialog : BaseBottomSheetDialog<UsageAccessPermissionViewModel>() {

    private val binding: DialogUsagePermissionBinding by inflate(R.layout.dialog_usage_permission)

    override fun getViewModel(): Class<UsageAccessPermissionViewModel> = UsageAccessPermissionViewModel::class.java

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding.buttonPermit.setOnClickListener {
            activity?.let { PermissionDialogAnayltics.usagePermissionPermitClicked(it) }
            onPermitClicked()
        }
        binding.buttonCancel.setOnClickListener {
            activity?.let { PermissionDialogAnayltics.usagePermissionCancelClicked(it) }
            dismiss()
        }
        return binding.root
    }

    private fun onPermitClicked() {
        startActivity(IntentHelper.usageAccessIntent())
        dismiss()
    }

    companion object {

        fun newInstance(): DialogFragment = UsageAccessPermissionDialog()
    }

}