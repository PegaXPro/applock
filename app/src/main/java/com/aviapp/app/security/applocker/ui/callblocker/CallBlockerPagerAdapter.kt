package com.aviapp.app.security.applocker.ui.callblocker

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aviapp.app.security.applocker.ui.callblocker.blacklist.BlackListFragment
import com.aviapp.app.security.applocker.ui.callblocker.log.CallLogFragment
import com.aviapp.app.security.applocker.ui.security.SecurityFragment

class CallBlockerPagerAdapter(manager: FragmentActivity) :
    FragmentStateAdapter(manager) {

    override fun getItemCount() =  2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BlackListFragment.newInstance()
            1 -> CallLogFragment.newInstance()
            else -> BlackListFragment.newInstance()
        }
    }
}