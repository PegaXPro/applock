package com.aviapp.app.security.applocker.ui.security

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aviapp.ads.AdHelper
import com.aviapp.app.security.applocker.ui.security.viewholder.AdHolder
import com.aviapp.app.security.applocker.ui.security.viewholder.AppLockItemViewHolder
import com.aviapp.app.security.applocker.ui.security.viewholder.HeaderViewHolder
import com.aviapp.app.security.applocker.util.FirebaseEvents
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject


class AppLockListAdapter(private val adHelper: AdHelper, private val adapterScope: CoroutineScope) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), KoinComponent {

    var appItemClicked: ((AppLockItemItemViewState) -> Unit)? = null
    lateinit var cm: ConnectivityManager

    private val itemViewStateList: ArrayList<AppLockItemBaseViewState> = arrayListOf()


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        cm = recyclerView.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @SuppressLint("CheckResult")
    fun setAppDataList(itemViewStateList: List<AppLockItemBaseViewState>) = adapterScope.launch {

        val list = mutableListOf<AppLockItemBaseViewState>()
        list.addAll(itemViewStateList)

        var count = 0

        if (adHelper.showAd()) {
            if (list.size > 4) {
                val newSize = list.size + list.size/4
                (1..newSize).forEach {num ->
                    if (num%4 == 0 && count < 8) {
                        count += 1
                        if (num%8 == 0) {
                            list.add(num, AdItemSm(num))
                        } else {
                            list.add(num, AdItem(num))
                        }
                    }
                }
                //list.removeAt(0)
            }
        }

        Single.create<DiffUtil.DiffResult> {
                val diffResult = DiffUtil.calculateDiff(AppLockListDiffUtil(this@AppLockListAdapter.itemViewStateList, list))
                it.onSuccess(diffResult)
            }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    FirebaseEvents.lock_screen_success()
                    this@AppLockListAdapter.itemViewStateList.clear()
                    this@AppLockListAdapter.itemViewStateList.addAll(list)
                    it.dispatchUpdatesTo(this@AppLockListAdapter)
                },
                { error ->
                    FirebaseEvents.lock_screen_failed()
                    this@AppLockListAdapter.itemViewStateList.clear()
                    this@AppLockListAdapter.itemViewStateList.addAll(list)
                    notifyDataSetChanged()
                })

    }

    override fun getItemCount(): Int = itemViewStateList.size

    override fun getItemViewType(position: Int): Int {
        return when (itemViewStateList[position]) {
            is AppLockItemHeaderViewState -> TYPE_HEADER
            is AppLockItemItemViewState -> TYPE_APP_ITEM
            is AdItem -> TYPE_APP_AD
            is AdItemSm -> TYPE_APP_AD_SM
            else -> throw IllegalArgumentException("No type found")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_APP_ITEM -> AppLockItemViewHolder.create(parent,adHelper, appItemClicked, adapterScope)
            TYPE_HEADER -> HeaderViewHolder.create(parent)
            TYPE_APP_AD -> AdHolder.create(parent, false, adapterScope, adHelper)
            TYPE_APP_AD_SM -> AdHolder.create(parent, true, adapterScope, adHelper)
            else -> throw IllegalStateException("No type found")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AppLockItemViewHolder -> holder.bind(itemViewStateList[position] as AppLockItemItemViewState)
            is HeaderViewHolder -> holder.bind(itemViewStateList[position] as AppLockItemHeaderViewState)
            is AdHolder -> holder.bind(position)
        }
    }

    @Suppress("DEPRECATION")
    private fun isOnline(): Boolean {
        return try {
            cm.activeNetworkInfo?.isConnectedOrConnecting?: false
        } catch (unused: Exception) {
            false
        }
    }

    companion object {

        private const val TYPE_HEADER = 0
        private const val TYPE_APP_ITEM = 1
        private const val TYPE_APP_AD = 2
        private const val TYPE_APP_AD_SM = 3
    }
}

data class AdItem(val id: Int) : AppLockItemBaseViewState()
data class AdItemSm(val id: Int) : AppLockItemBaseViewState()