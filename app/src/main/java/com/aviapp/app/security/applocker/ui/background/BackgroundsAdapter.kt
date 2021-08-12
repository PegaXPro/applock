package com.aviapp.app.security.applocker.ui.background

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.aviapp.ads.AdHelper
import com.aviapp.ads.adModule
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.data.AppData
import com.aviapp.app.security.applocker.data.database.AppLockerDatabase
import com.aviapp.app.security.applocker.data.database.prem.Prem
import com.aviapp.app.security.applocker.databinding.ItemBackgroundGradientBinding
import com.aviapp.app.security.applocker.ui.prem.PremActivity
import com.aviapp.app.security.applocker.util.SaveFileUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.ext.scope

class BackgroundsAdapter(val database: AppLockerDatabase, private val adapterScope: CoroutineScope, private val adHandler: AdHelper, val context: Context) : RecyclerView.Adapter<BackgroundsAdapter.BackgroundGradientItemViewHolder>() {

    var onItemSelected: ((item: GradientItemViewState) -> Unit)? = null
    private val gradientItemViewStateList = arrayListOf<GradientItemViewState>()
    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun setViewStateList(gradientViewStateList: List<GradientItemViewState>) {
        this.gradientItemViewStateList.clear()
        this.gradientItemViewStateList.addAll(gradientViewStateList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BackgroundGradientItemViewHolder.create(parent, onItemSelected, adapterScope, database, adHandler, pref)

    override fun getItemCount(): Int = gradientItemViewStateList.size

    override fun onBindViewHolder(holder: BackgroundGradientItemViewHolder, position: Int) =
        holder.bind(gradientItemViewStateList[position])

    class BackgroundGradientItemViewHolder(
        val binding: ItemBackgroundGradientBinding,
        private val onItemSelected: ((item: GradientItemViewState) -> Unit)?,
        private val adapterScope: CoroutineScope,
        val database: AppLockerDatabase,
        private val adHandler: AdHelper,
        private val pref: SharedPreferences
    ) : RecyclerView.ViewHolder(binding.root) {

        var job: Job? = null

        fun bind(gradientItemViewState: GradientItemViewState) {

            binding.premBack.visibility = View.INVISIBLE
            val reward = pref.getBoolean("rewarderShow", true)

            job?.cancel()
            job = adapterScope.launch {

                database.premDao().getPremFlow().flowOn(Dispatchers.IO).collect {

                    val p0 = it?: Prem()
                    if (p0.prIsNeeded && gradientItemViewState.prem) {

                        if (gradientItemViewState.id == 3 && reward) {
                            binding.rewardAd.visibility = View.VISIBLE
                        } else {
                            binding.rewardAd.visibility = View.INVISIBLE
                        }

                        binding.root.setOnClickListener {

                            if (gradientItemViewState.id == 3 && reward) {
                                adapterScope.launch {
                                    adHandler.requestReward(itemView.context as Activity) {
                                        if (it) {
                                            pref.edit().putBoolean("rewarderShow", false).apply()
                                            onItemSelected?.invoke(binding.viewState!!)
                                            adapterScope.launch {
                                                SaveFileUtil.saveFileWithRequest(itemView.context as Activity, gradientItemViewState.firebaseRef!!, adapterScope).flowOn(Dispatchers.IO).collect {
                                                    if (it is SaveFileUtil.StateBack.Downloading) {
                                                        binding.progressTheme.visibility = View.VISIBLE
                                                    } else {
                                                        binding.progressTheme.visibility = View.INVISIBLE
                                                    }
                                                }
                                            }

                                        } else {
                                            val p1 = itemView.context
                                            p1.startActivity(Intent(p1, PremActivity::class.java))
                                        }
                                    }
                                }
                            } else if(gradientItemViewState.id == 3 && !reward) {
                                onItemSelected?.invoke(binding.viewState!!)
                            }

                            if (gradientItemViewState.id > 3) {
                                PremActivity.start(itemView.context as Activity)
                            }

                        }

                        if (gradientItemViewState.id > 3) {
                            binding.premBack.visibility = View.VISIBLE
                        }

                    } else {

                        binding.root.setOnClickListener {
                            val p0 = gradientItemViewState.firebaseRef
                            if (p0 != null) {
                                adapterScope.launch {
                                    SaveFileUtil.saveImage(itemView.context, p0, adapterScope).flowOn(Dispatchers.IO).collect {
                                        if (it is SaveFileUtil.StateBack.Downloading) {
                                            binding.progressTheme.visibility = View.VISIBLE
                                        } else {
                                            binding.progressTheme.visibility = View.INVISIBLE
                                        }
                                    }
                                }
                            }
                            onItemSelected?.invoke(binding.viewState!!)
                        }

                        binding.premBack.visibility = View.INVISIBLE
                    }
                }


            }

            if (gradientItemViewState.prem) {
                binding.premBack.visibility = View.VISIBLE
            } else {
                binding.premBack.visibility = View.INVISIBLE
            }

            binding.viewState = gradientItemViewState
            binding.executePendingBindings()
        }

        companion object {
            fun create(
                parent: ViewGroup,
                onItemSelected: ((item: GradientItemViewState) -> Unit)?,
                adapterScope: CoroutineScope,
                database: AppLockerDatabase,
                adHandler: AdHelper,
                pref: SharedPreferences
            ): BackgroundGradientItemViewHolder {
                val binding: ItemBackgroundGradientBinding = DataBindingUtil
                    .inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_background_gradient,
                        parent,
                        false
                    )
                return BackgroundGradientItemViewHolder(binding, onItemSelected, adapterScope, database, adHandler, pref)
            }
        }

    }
}