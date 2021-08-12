package com.aviapp.app.security.applocker.ui.intruders

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import com.aviapp.app.security.applocker.R

class DeleteDialog(val context: Context, private val deleteAction: () -> Unit) {


    init {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_intruders_delete)
        dialog.findViewById<View>(R.id.deleteOk).setOnClickListener {
            dialog.dismiss()
            deleteAction.invoke()
        }

        dialog.findViewById<View>(R.id.deleteCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

}