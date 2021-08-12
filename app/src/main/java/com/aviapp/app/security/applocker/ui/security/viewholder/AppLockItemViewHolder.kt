package com.aviapp.app.security.applocker.ui.security.viewholder

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.aviapp.ads.AdHelper
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.databinding.ItemAppLockListBinding
import com.aviapp.app.security.applocker.ui.permissions.PermissionChecker
import com.aviapp.app.security.applocker.ui.permissions.PermissionsActivity
import com.aviapp.app.security.applocker.ui.security.AppLockItemItemViewState
import com.aviapp.app.security.applocker.util.setAnimatedClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AppLockItemViewHolder(
    private val binding: ItemAppLockListBinding,
    private val appItemClicked: ((AppLockItemItemViewState) -> Unit)?,
    private val adHelper: AdHelper,
    private val coroutineScope: CoroutineScope
) : RecyclerView.ViewHolder(binding.root) {


    private var showInter = true

    init {

        binding.imageViewLock.setAnimatedClick {
            val context = binding.root.context
            if (PermissionChecker.isAllPermissionChecked(context).not()) {
                val intent = Intent(context, PermissionsActivity::class.java)
                intent.putExtra("lockAppOrApps", 0)
                context.startActivity(intent)
            } else {
                coroutineScope.launch {

                    if (showInter) {
                        adHelper.showInterstitial(context as Activity, 2) {
                            showInter = false
                            appItemClicked?.invoke(binding.viewState!!)
                        }
                    } else {
                        showInter = true
                        appItemClicked?.invoke(binding.viewState!!)
                    }

                }
            }
        }

    }

    fun bind(appLockItemViewState: AppLockItemItemViewState) {

        if (appLockItemViewState.isLocked) {
            binding.imageViewLock.setAnimation(R.raw.unlock)
        } else {
            binding.imageViewLock.setAnimation(R.raw.lock)
        }

        binding.viewState = appLockItemViewState
        binding.executePendingBindings()
    }

    companion object {
        fun create(
            parent: ViewGroup,
            adHandler: AdHelper,
            appItemClicked: ((AppLockItemItemViewState) -> Unit)?,
            coroutineScope: CoroutineScope
        ): AppLockItemViewHolder {
            val binding = DataBindingUtil.inflate<ItemAppLockListBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_app_lock_list,
                parent,
                false
            )

            return AppLockItemViewHolder(binding, appItemClicked, adHelper = adHandler, coroutineScope)
        }
    }

}