package com.aviapp.app.security.applocker.ui.vault.removingvaultdialog

import android.content.Context
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.ui.vault.addingvaultdialog.ProcessState

data class RemoveFromVaultViewState(val progress: Int, val processState: ProcessState) {

    fun getPercentText(context: Context): String {
        return context.getString(R.string.dialog_action_process, progress)
    }
}