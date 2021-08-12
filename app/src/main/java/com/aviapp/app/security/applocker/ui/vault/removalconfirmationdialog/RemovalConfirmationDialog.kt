package com.aviapp.app.security.applocker.ui.vault.removalconfirmationdialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.aviapp.ads.AdHelper
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.database.vault.VaultMediaEntity
import com.aviapp.app.security.applocker.databinding.DialogRemovalConfirmationBinding
import com.aviapp.app.security.applocker.ui.vault.removingvaultdialog.RemoveFromVaultDialog
import com.aviapp.app.security.applocker.util.delegate.inflate

class RemovalConfirmationDialog : DialogFragment() {

    private val binding: DialogRemovalConfirmationBinding by inflate(R.layout.dialog_removal_confirmation)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {


        binding.buttonAccept.setOnClickListener {
            arguments?.getParcelable<VaultMediaEntity>(KEY_VAULT_MEDIA_ENTITY)?.let { vaultMediaEntity ->
                activity?.let {
                    dismiss()
                    RemoveFromVaultDialog.newInstance(vaultMediaEntity).show(it.supportFragmentManager, "")
                }
            }
        }

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE);
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    companion object {
        private const val KEY_VAULT_MEDIA_ENTITY = "KEY_VAULT_MEDIA_ENTITY"

        fun newInstance(vaultMediaEntity: VaultMediaEntity): DialogFragment {
            return RemovalConfirmationDialog().apply {

                arguments = Bundle().apply {
                    putParcelable(KEY_VAULT_MEDIA_ENTITY, vaultMediaEntity)
                }
            }
        }
    }
}