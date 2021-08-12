package com.aviapp.app.security.applocker.ui.permissions

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.aviapp.app.security.applocker.BuildConfig


object IntentHelper {

    fun overlayIntent(packageName: String): Intent {
        return Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
    }

    fun usageAccessIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    fun privacyPolicyWebIntent(string: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(string))
    }

    fun rateUsIntent(): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
        )
    }

    fun startStorePage(activity: Activity, publisherName: String) {
        try {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://search?q=pub:${publisherName}}")
                )
            )
        } catch (anfe: android.content.ActivityNotFoundException) {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/developer?id=${publisherName}")
                )
            )
        }
    }
}