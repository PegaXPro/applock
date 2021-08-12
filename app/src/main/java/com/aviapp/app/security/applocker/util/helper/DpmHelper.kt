package com.aviapp.app.security.applocker.util.helper

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.service.receiver.MyAdmin


/**
 * @author Muhammad Mehran
 * Date: 05/04/2020
 * Time: 8:50 AM
 * E-mail: imuhammadmehran@gmail.com
 */
object DpmHelper {

    fun isDeviceAdminActive(context: Context): Boolean {
        val compName =
            ComponentName(context, MyAdmin::class.java)
        val dpm: DevicePolicyManager =
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isAdminActive(compName)
    }

    fun removeDeviceAdmin(context: Context) {
        val compName =
            ComponentName(context, MyAdmin::class.java)
        val dpm: DevicePolicyManager =
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        dpm.removeActiveAdmin(compName)
    }

    fun activateDeviceAdmin(
        activity: Activity,
        requestCode: Int
    ) {

        val compName =
            ComponentName(activity, MyAdmin::class.java)
        val dpm: DevicePolicyManager =
            activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        val adminActive = dpm.isAdminActive(compName)
        if (!adminActive) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                activity.getString(R.string.device_admin_permission_description)
            )
            activity.startActivityForResult(intent, requestCode)
        }
    }
}