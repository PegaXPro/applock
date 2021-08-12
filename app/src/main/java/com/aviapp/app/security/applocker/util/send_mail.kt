package com.aviapp.app.security.applocker.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.aviapp.app.security.applocker.ui.main.FeedbackActivity

fun feedback(activity: Activity) {


        FeedbackActivity.start(activity)

//    val email = Intent(Intent.ACTION_SENDTO)
//    email.data = Uri.parse("mailto:help.everydayapps@gmail.com")
//    activity.startActivity(email)
}
