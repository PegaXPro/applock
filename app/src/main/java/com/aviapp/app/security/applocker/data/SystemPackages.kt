package com.aviapp.app.security.applocker.data

object SystemPackages {
    fun getSystemPackages(): List<String> {
        return arrayListOf<String>().apply {
            add("com.android.packageinstaller")
        }
    }
}