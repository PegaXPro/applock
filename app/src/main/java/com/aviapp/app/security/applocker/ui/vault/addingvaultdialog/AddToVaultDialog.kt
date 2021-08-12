package com.aviapp.app.security.applocker.ui.vault.addingvaultdialog

import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.core.content.edit
import androidx.lifecycle.Observer
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.database.vault.VaultMediaType
import com.aviapp.app.security.applocker.data.database.vault.VaultMediaType.TYPE_IMAGE
import com.aviapp.app.security.applocker.data.database.vault.VaultMediaType.TYPE_VIDEO
import com.aviapp.app.security.applocker.databinding.DialogAddToVaultBinding
import com.aviapp.app.security.applocker.ui.BaseBottomSheetDialog
import com.aviapp.app.security.applocker.ui.icon_changer.IconPagerAdapter
import com.aviapp.app.security.applocker.ui.vault.analytics.VaultAnalytics
import com.aviapp.app.security.applocker.util.delegate.inflate

class AddToVaultDialog : BaseBottomSheetDialog<AddToVaultViewModel>(), View.OnClickListener {

    var onDismissListener: (() -> Unit)? = null

    private val binding: DialogAddToVaultBinding by inflate(R.layout.dialog_add_to_vault)
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var mListener: OnClickListener

    override fun getViewModel(): Class<AddToVaultViewModel> = AddToVaultViewModel::class.java

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences = requireContext().getSharedPreferences("warningToGallery", 0)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val warning = sharedPreferences.getBoolean("warning", true)

        val filePath = arguments?.getStringArrayList(KEY_FILE_PATH)
        val mediaType = arguments?.getSerializable(KEY_MEDIA_TYPE) as VaultMediaType

        viewModel.getAddToVaultViewStateLiveData().observe(this, Observer { viewState ->
            binding.viewState = viewState
            binding.executePendingBindings()

            if (viewState.processState == ProcessState.COMPLETE) {
                onDismissListener?.invoke()
                dismiss()

                if (warning) {
                    showPopup()
                }

                when (mediaType) {
                    TYPE_IMAGE -> activity?.let { activity ->
                        filePath?.let { list ->
                            VaultAnalytics.addedImageVault(activity, list.size)
                        }
                    }
                    TYPE_VIDEO -> activity?.let { activity ->
                        filePath?.let { list ->
                            VaultAnalytics.addedVideoVault(activity, list.size)
                        }
                    }
                }
            }
        })

        filePath?.let { viewModel.setSelectedFilePath(it, mediaType) }
    }

    private fun showPopup() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_warning_upload_from_gallery)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val button = dialog.findViewById<Button>(R.id.okButton)
        val checkBox = dialog.findViewById<CheckBox>(R.id.checkbox)
        button.setOnClickListener {
            if (checkBox.isChecked) {
                sharedPreferences.edit {
                    putBoolean("warning", false)
                }
                dialog.dismiss()
            } else {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    companion object {

        private const val KEY_FILE_PATH = "KEY_FILE_PATH"

        private const val KEY_MEDIA_TYPE = "KEY_MEDIA_TYPE"

        fun newInstance(
            selectedFilePath: ArrayList<String?>,
            mediaType: VaultMediaType
        ): AddToVaultDialog {
            return AddToVaultDialog().apply {
                isCancelable = false
                arguments = Bundle().apply {
                    putStringArrayList(KEY_FILE_PATH, selectedFilePath)
                    putSerializable(KEY_MEDIA_TYPE, mediaType)
                }
            }
        }
    }

    fun setOnBtnClickListener(listener: OnClickListener) {
        mListener = listener
    }

    interface OnClickListener {
        fun onClick(view: View)
    }

    override fun onClick(v: View) {
        mListener.onClick(v)
    }
}