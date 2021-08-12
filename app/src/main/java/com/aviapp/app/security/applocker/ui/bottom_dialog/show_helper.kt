package com.aviapp.app.security.applocker.ui.bottom_dialog

import androidx.fragment.app.FragmentActivity
import com.aviapp.app.security.applocker.ui.permissions.PermissionChecker

fun FragmentActivity.showDialog() {


    val check = PermissionChecker.isAllPermissionChecked(this).not()
    val pr = true
/*    if(true) {
        Log.d("SKDJFLKSH", "!@FFFFFFF")
        BottomDialog().show(this.supportFragmentManager, "bottom_dialog")
    }*/
}