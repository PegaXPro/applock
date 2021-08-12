package com.aviapp.app.security.applocker.ui.vault

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.database.vault.VaultMediaType
import com.aviapp.app.security.applocker.ui.vault.vaultlist.VaultListFragment

class VaultPagerAdapter(context: Context, manager: FragmentActivity) : FragmentStateAdapter(manager) {

    private val tabs = context.resources.getStringArray(R.array.vault_tabs)

    companion object {
        private const val INDEX_IMAGES = 0
        private const val INDEX_VIDEOS = 1
    }

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            INDEX_IMAGES -> VaultListFragment.newInstance(VaultMediaType.TYPE_IMAGE)
            INDEX_VIDEOS -> VaultListFragment.newInstance(VaultMediaType.TYPE_VIDEO)
            else -> VaultListFragment.newInstance(VaultMediaType.TYPE_IMAGE)
        }
    }
}