package com.aviapp.app.security.applocker.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase


object FirebaseEvents {

    val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    fun lock_screen_success() { firebaseAnalytics.logEvent("lock_screen_success", null) }
    fun lock_screen_failed() { firebaseAnalytics.logEvent("lock_screen_failed", null) }
    fun pattern_sucess() { firebaseAnalytics.logEvent("pattern_sucess", null) }
    fun preland_screen_opened() { firebaseAnalytics.logEvent("preland_screen_opened", null) }
    fun lock_apps_opened() { firebaseAnalytics.logEvent("lock_apps_opened", null) }
    fun get_autostart_permission() { firebaseAnalytics.logEvent("get_autostart_permission", null) }
    fun get_data_permission() { firebaseAnalytics.logEvent("get_data_permission", null) }
    fun get_over_permission() { firebaseAnalytics.logEvent("get_over_permission", null) }
    fun get_all_permission_success() { firebaseAnalytics.logEvent("get_all_permission_success", null) }
    fun lock_above_another_app() { firebaseAnalytics.logEvent("lock_above_another_app", null) }
    fun lock_all_app_success() { firebaseAnalytics.logEvent("lock_all_app_success", null) }
    fun purchase() { firebaseAnalytics.logEvent("purchase", null) }


}