package com.aviapp.app.security.applocker.data.database.callblocker.addtoblacklist


import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.Observer
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.databinding.DialogCallBlockerAddToBlacklistBinding
import com.aviapp.app.security.applocker.ui.BaseBottomSheetDialog
import com.aviapp.app.security.applocker.util.delegate.inflate
import android.util.Log
import android.view.Window
import android.widget.Toast
import kotlinx.android.synthetic.main.dialog_call_blocker_add_to_blacklist.*

class AddToBlackListDialog : BaseBottomSheetDialog<AddToBlackListViewModel>() {

    private var startTime = 0
    private var endTime = 24

    private var TAG = "AddToBlackListDialog"

    private val binding: DialogCallBlockerAddToBlacklistBinding by inflate(R.layout.dialog_call_blocker_add_to_blacklist)

    override fun getViewModel(): Class<AddToBlackListViewModel> =
        AddToBlackListViewModel::class.java

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        init()

        val name = arguments?.getString("name")
        val number = arguments?.getString("number")

        binding.editTextName.setText(name)
        binding.editTextPhoneNumber.setText(number)

        binding.buttonBlock.setOnClickListener {
            if (validateInputFields()) {
                if (startTime >= endTime) {
                    Toast.makeText(context, "Invalid Hour", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                Log.d(TAG,"startTime $startTime")
                Log.d(TAG,"startTime $endTime")

                viewModel.blockNumber(editTextName.text.toString(), editTextPhoneNumber.text.toString(), startTime.toString(), endTime.toString())

            }
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE);
        }

        return binding.root
    }

    private fun init() {

        binding.imgBtnStartPlus.setOnClickListener {
            if (startTime >= 24) return@setOnClickListener

            startTime++
            val hour = if (startTime > 9) startTime.toString() else "0$startTime"
            textViewStartTime.text = "$hour:00"
        }

        binding.imgBtnStartMinus.setOnClickListener {
            if (startTime <= 0) return@setOnClickListener

            startTime--
            val hour = if (startTime > 9) startTime.toString() else "0$startTime"
            textViewStartTime.text = "$hour:00"
        }

        binding.imgBtnEndPlus.setOnClickListener {
            if (endTime >= 24) return@setOnClickListener

            endTime++
            val hour = if (endTime > 9) endTime.toString() else "0$endTime"
            textViewEndTime.text = "$hour:00"
        }

        binding.imgBtnEndMinus.setOnClickListener {
            if (endTime <= 0) return@setOnClickListener

            endTime--
            val hour = if (endTime > 9) endTime.toString() else "0$endTime"
            textViewEndTime.text = "$hour:00"
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getViewStateLiveData()
            .observe(this, Observer {
                dismiss()
            })
    }

    private fun validateInputFields(): Boolean {
        return !binding.editTextPhoneNumber.text.isNullOrEmpty()
    }

    companion object {
        fun newInstance(): AddToBlackListDialog = AddToBlackListDialog()
    }

//    override fun onTimeRangeSelected(
//        startHour: Int,
//        startMinute: Int,
//        endHour: Int,
//        endMinute: Int
//    ) {
//        var startHourString = startHour.toString()
//        var startMinuteString = startMinute.toString()
//        var endHourString = endHour.toString()
//        var endMinuteString = endMinute.toString()
//        if (endHour < startHour) {
//            val alert = AlertDialog.Builder(context!!)
//            alert.setMessage("Time you select is invalid, Please select block time again")
//            alert.setPositiveButton("Got It") { dialog, which -> dialog.dismiss() }
//            alert.create().show()
//            return
//        }
//
//        val startTime = "$startHour:$startMinute"
//        val endTime = "$endHour:$endMinute"
//        viewModel.blockNumber(
//            editTextName.text.toString(),
//            editTextPhoneNumber.text.toString(),
//            startTime,
//            endTime
//        )
//    }
}