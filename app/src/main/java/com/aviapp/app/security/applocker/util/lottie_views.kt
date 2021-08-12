package com.aviapp.app.security.applocker.util

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import com.airbnb.lottie.LottieAnimationView
import com.aviapp.ads.AdHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


fun LottieAnimationView.setAnimatedClick(action: () -> Unit) {
    
    this.setOnClickListener {
        if (this.isAnimating) return@setOnClickListener
        this.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                this@setAnimatedClick.removeAllAnimatorListeners()
                action.invoke()
            }
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationRepeat(p0: Animator?) {}
        })
        this.playAnimation()
    }
}


fun LottieAnimationView.setAnimatedClickWithAd(scope: CoroutineScope, adHelper: AdHelper, action: () -> Unit) {

    this.setAnimatedClick {
        scope.launch {
            adHelper.showInterstitial(this@setAnimatedClickWithAd.context as Activity) {
                action.invoke()
            }
        }
    }
}


//toggle
fun LottieAnimationView.setToggleState(isChecker: Boolean) {

    if (isChecker) {
        this.progress = 0.5f
        this.tag = "on"
    } else {
        this.progress = 0.0f
        this.tag = "off"
    }
}

fun LottieAnimationView.setToggleStateAnimated(isChecker: Boolean) {

    val animator =  when {
        isChecker && !getToggleState() -> {
            this.tag = "on"
            ValueAnimator.ofFloat(0f, 0.5f)
        }
        !isChecker && getToggleState() -> {
            this.tag = "off"
            ValueAnimator.ofFloat(0.5f, 1f)
        }

        isChecker && getToggleState() -> {null}
        !isChecker && !getToggleState() -> {null}
        else -> {null}
    }
    animator?: return
    animator.addUpdateListener {
        this.progress = it.animatedValue as Float
    }
    animator.duration = (this.duration/2)/2
    animator.start()
}


fun LottieAnimationView.getToggleState(): Boolean {
    return when (this.tag) {
        "on" -> true
        "off" -> false
        else -> false
    }
}