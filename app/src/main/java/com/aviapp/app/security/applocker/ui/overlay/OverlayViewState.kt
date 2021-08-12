package com.aviapp.app.security.applocker.ui.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.ui.overlay.OverlayValidateType.*
import com.aviapp.app.security.applocker.ui.overlay.activity.FingerPrintResult.*
import com.aviapp.app.security.applocker.ui.overlay.activity.FingerPrintResultData

data class OverlayViewState(
    val overlayValidateType: OverlayValidateType? = null,
    val isDrawnCorrect: Boolean? = null,
    val fingerPrintResultData: FingerPrintResultData? = null,
    val isHiddenDrawingMode: Boolean = false,
    val isFingerPrintMode: Boolean = false,
    val isIntrudersCatcherMode: Boolean = false
) {

    @SuppressLint("StringFormatInvalid")
    fun getPromptMessage(context: Context): String {
        return when (overlayValidateType) {
            TYPE_PATTERN -> {
                when (isDrawnCorrect) {
                    true -> context.getString(R.string.overlay_prompt_pattern_title_correct)
                    false -> context.getString(R.string.overlay_prompt_pattern_title_wrong)
                    null -> context.getString(R.string.overlay_prompt_pattern_title)
                }
            }
            TYPE_FINGERPRINT -> {
                when (fingerPrintResultData?.fingerPrintResult) {
                    SUCCESS -> {
                        if (isFingerPrintMode) {
                            context.getString(R.string.overlay_prompt_fingerprint_title_correct)
                        } else {
                            context.getString(R.string.overlay_prompt_pattern_title)
                        }

                    }
                    NOT_MATCHED -> context.getString(
                        R.string.overlay_prompt_fingerprint_title_wrong,
                        fingerPrintResultData.availableTimes.toString()
                    )
                    ERROR -> context.getString(R.string.overlay_prompt_fingerprint_title_error)
                    else -> context.getString(R.string.overlay_prompt_fingerprint_title)
                }
            }
            else -> context.getString(R.string.overlay_prompt_pattern_title)
        }
    }

    fun getFingerPrintIconVisibility(): Int =
        if (isFingerPrintMode) View.VISIBLE else View.INVISIBLE

}