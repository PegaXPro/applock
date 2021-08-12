package com.aviapp.app.security.applocker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class StartupIntentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ServiceStarter.startService(context)
    }
}