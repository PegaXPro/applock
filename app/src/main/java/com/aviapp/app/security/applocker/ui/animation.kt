package com.aviapp.app.security.applocker.ui

import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.aviapp.ads.setOnClickListenerWIthAnimation


fun View.setOnClickListenerWIthAnimation(action: () -> Unit) {

    val p0 = this.scaleX
    val p1 = this.scaleY

    this.setOnClickListener {
        this.animate().setInterpolator(AccelerateInterpolator()).scaleX(p0*0.9f).scaleY(p1*0.9f).setDuration(50).withEndAction {
            this.animate().setInterpolator(DecelerateInterpolator()).setDuration(70).scaleY(p1).scaleX(p0).withEndAction { action.invoke() }.start()
        }.start()
    }
}
