package com.aviapp.app.security.applocker.ui.security.viewholder

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aviapp.ads.AdHelper
import com.aviapp.app.security.applocker.BuildConfig
import com.aviapp.app.security.applocker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.text.FieldPosition
import kotlin.math.log

class AdHolder(val view: View, val small:Boolean = false, val scope: CoroutineScope, val adHelper: AdHelper): RecyclerView.ViewHolder(view), KoinComponent {

    var job: Job? = null

    init {
        /*job?.cancel()
        job = scope.launch {
            adHelper.getAdEventFlow().collect {
                adHelper.showNative(view.context as Activity, view, it, false)
            }
        }*/
    }

    fun bind(position: Int) {

        job?.cancel()
        job = scope.launch {
            adHelper.getAdEventFlow().collect {
                val size = position % 8 == 0
                val adId = if (!BuildConfig.DEBUG) "ca-app-pub-4875591253190011/8785118014" else "ca-app-pub-3940256099942544/1044960115"
                adHelper.showNative(adId, view.context as Activity, view, it, size)
            }
        }
    }

    companion object {

        fun create(parent: ViewGroup, sm: Boolean, scope: CoroutineScope, adHelper: AdHelper): AdHolder {
            val view = if (sm) {
                LayoutInflater.from(parent.context).inflate(
                    R.layout.rv_ad_holder,
                    parent,
                    false
                )
            } else {
                LayoutInflater.from(parent.context).inflate(
                    R.layout.rv_ad_holder,
                    parent,
                    false
                )
            }

            return AdHolder(view, scope = scope, adHelper = adHelper)
        }
    }

}