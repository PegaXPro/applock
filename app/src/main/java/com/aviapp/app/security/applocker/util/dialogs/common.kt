package com.aviapp.app.security.applocker.util.dialogs

import android.app.Activity
import android.content.Intent
import android.net.Uri


fun feedback(activity: Activity) {
    val email = Intent(Intent.ACTION_SENDTO)
    email.data = Uri.parse("mailto:help.everydayapps@gmail.com")
    activity.startActivity(email)
}