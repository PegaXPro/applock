package com.aviapp.app.security.applocker.data

import android.content.Context
import javax.inject.Inject

class AppLockerPreferences @Inject constructor(context: Context) {

    private val sharedPref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun setHiddenDrawingMode(hiddenDrawingPatternMode: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_IS_PATTERN_HIDDEN, hiddenDrawingPatternMode)
            apply()
        }
    }


    fun getLaunchCount():Int {
        val p0 = sharedPref.getInt("launchCount", 0)
        sharedPref.edit().putInt("launchCount", p0 + 1).apply()
        return p0
    }

    fun isRated(): Boolean {
        val p0 = sharedPref.getBoolean("rated", false)
        return p0
    }

    fun getHiddenDrawingMode(): Boolean = sharedPref.getBoolean(KEY_IS_PATTERN_HIDDEN, false)

    fun setFingerPrintEnable(fingerPrintEnabled: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_IS_FINGERPRINT_ENABLE, fingerPrintEnabled)
            apply()
        }
    }

    fun getFingerPrintEnabled(): Boolean = sharedPref.getBoolean(KEY_IS_FINGERPRINT_ENABLE, false)

    fun setIntrudersCatcherEnable(intrudersCatcherEnabled: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(KEY_IS_INTRUDERS_CATCHER_ENABLE, intrudersCatcherEnabled)
            apply()
        }
    }

    fun getIntrudersCatcherEnabled(): Boolean = sharedPref.getBoolean(
        KEY_IS_INTRUDERS_CATCHER_ENABLE, false
    )

    fun setUserRateUs() {
        with(sharedPref.edit()) {
            putBoolean(KEY_RATE_US, true)
            apply()
        }
    }

    fun isUserRateUs(): Boolean {
        return sharedPref.getBoolean(KEY_RATE_US, false)
    }

    fun setSelectedBackgroundId(backgroundId: Int) {
        with(sharedPref.edit()) {
            putInt(KEY_BACKGROUND_ID, backgroundId)
            apply()
        }
    }

    fun getSelectedBackgroundId(): Int {
        return sharedPref.getInt(KEY_BACKGROUND_ID, 0)
    }

    fun acceptPrivacyPolicy() {
        with(sharedPref.edit()) {
            putBoolean(KEY_ACCEPT_PRIVACY_POLICY, true)
            apply()
        }
    }

    fun isPrivacyPolicyAccepted(): Boolean {
        return sharedPref.getBoolean(KEY_ACCEPT_PRIVACY_POLICY, false)
    }

    fun setMasterPin(pin: String) {
        with(sharedPref.edit()) {
            putString(MASTER_PIN, pin)
            apply()
        }
    }

    fun setPatternEnabled() {
        with(sharedPref.edit()) {
            putBoolean(IS_PATTERN_ENABLED, true)
            apply()
        }
    }


    fun setPinEnabled() {
        with(sharedPref.edit()) {
            putBoolean(IS_PIN_ENABLED, true)
            apply()
        }
    }

    fun isPinEnabled(): Boolean {
        return sharedPref.getBoolean(IS_PIN_ENABLED, false)
    }

    fun isPatternEnabled(): Boolean {
        return sharedPref.getBoolean(IS_PATTERN_ENABLED, false)
    }

    fun getMasterPin(): String {
        return sharedPref.getString(MASTER_PIN, "").toString()
    }

    fun isMasterPinCreated(): Boolean {
        return sharedPref.contains(MASTER_PIN)
    }

    companion object {

        const val PREFERENCES_NAME = "AppLockerPreferences"

        private const val KEY_IS_PATTERN_HIDDEN = "KEY_IS_PATTERN_HIDDEN"

        private const val KEY_IS_FINGERPRINT_ENABLE = "KEY_IS_FINGERPRINT_ENABLE"

        private const val KEY_IS_INTRUDERS_CATCHER_ENABLE = "KEY_IS_INTRUDERS_CATCHER_ENABLE"

        private const val KEY_RATE_US = "KEY_RATE_US"

        private const val KEY_ACCEPT_PRIVACY_POLICY = "KEY_ACCEPT_PRIVACY_POLICY"

        private const val KEY_BACKGROUND_ID = "KEY_BACKGROUND_ID"

        private const val MASTER_PIN = "MASTER_PIN"

        private const val IS_PIN_ENABLED = "IS_PIN_ENABLED"

        private const val IS_PATTERN_ENABLED = "IS_PATTERN_ENABLED"

    }
}