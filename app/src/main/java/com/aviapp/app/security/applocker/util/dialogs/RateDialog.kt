package com.aviapp.app.security.applocker.util.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.BuildConfig
import com.aviapp.app.security.applocker.data.AppLockerPreferences
import com.aviapp.app.security.applocker.ui.main.FeedbackActivity
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*

class RateDialog(val activity: Activity) {

    var rateUs: Boolean? = null
    var starCount = 0
    private var dialog: Dialog = Dialog(activity)
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    var star1: ImageView
    var star2: ImageView
    var star3: ImageView
    var star4: ImageView
    var star5: ImageView

    private val sharedPref = activity.getSharedPreferences(AppLockerPreferences.PREFERENCES_NAME, Context.MODE_PRIVATE)

    init {

        dialog.setContentView(R.layout.rate_us_dialog)
        val laterButton = dialog.findViewById<TextView>(R.id.noBt)

        star1 = dialog.findViewById(R.id.premstar1);
        star2 = dialog.findViewById(R.id.premstar2);
        star3 = dialog.findViewById(R.id.premstar3);
        star4 = dialog.findViewById(R.id.premstar4);
        star5 = dialog.findViewById(R.id.premstar5);

        star1.setOnClickListener { rateStart(1) }
        star2.setOnClickListener { rateStart(2) }
        star3.setOnClickListener { rateStart(3) }
        star4.setOnClickListener { rateStart(4) }
        star5.setOnClickListener { rateStart(5) }

        laterButton.setOnClickListener {
            dialog.dismiss()
        }

        val positiveButton = dialog.findViewById<TextView>(R.id.yesBt)

        val close = dialog.findViewById<View>(R.id.closeBt)
        close?.setOnClickListener {
            dialog.dismiss()
        }

        positiveButton.setOnClickListener {

            sharedPref.edit().putBoolean("rated", true).apply()
            dialog.dismiss()
            //FeedbackActivity.start(activity)
            rate()

//            if (starCount == 0) {
//                animateAllStart()
//                return@setOnClickListener
//            }
//
//            if (starCount > 3) {
//                dialog.dismiss()
//                rate()
//            }
//
//            else {
//                dialog.dismiss()
//                FeedbackActivity.start(activity)
//                //feedback(activity)
//            }
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        dialog.setOnDismissListener {
            scope.cancel()
        }

        rateStart(5)
        animateAllStart()
    }

    private fun rateStart(count: Int) {

        starCount = count
        when (count) {

            0 -> {}

            1 -> {
                star1.setImageResource(R.drawable.ic_star_on)
                star2.setImageResource(R.drawable.star_not_fill)
                star3.setImageResource(R.drawable.star_not_fill)
                star4.setImageResource(R.drawable.star_not_fill)
                star5.setImageResource(R.drawable.star_not_fill)
            }

            2 -> {
                star1.setImageResource(R.drawable.ic_star_on)
                star2.setImageResource(R.drawable.ic_star_on)
                star3.setImageResource(R.drawable.star_not_fill)
                star4.setImageResource(R.drawable.star_not_fill)
                star5.setImageResource(R.drawable.star_not_fill)
            }

            3 -> {
                star1.setImageResource(R.drawable.ic_star_on)
                star2.setImageResource(R.drawable.ic_star_on)
                star3.setImageResource(R.drawable.ic_star_on)
                star4.setImageResource(R.drawable.star_not_fill)
                star5.setImageResource(R.drawable.star_not_fill)
            }

            4 -> {
                star1.setImageResource(R.drawable.ic_star_on)
                star2.setImageResource(R.drawable.ic_star_on)
                star3.setImageResource(R.drawable.ic_star_on)
                star4.setImageResource(R.drawable.ic_star_on)
                star5.setImageResource(R.drawable.star_not_fill)
            }

            5 -> {
                star1.setImageResource(R.drawable.ic_star_on)
                star2.setImageResource(R.drawable.ic_star_on)
                star3.setImageResource(R.drawable.ic_star_on)
                star4.setImageResource(R.drawable.ic_star_on)
                star5.setImageResource(R.drawable.ic_star_on)
            }
        }
    }

    private fun animateAllStart() {
        animateStars(star1)
        animateStars(star2)
        animateStars(star3)
        animateStars(star4)
        animateStars(star5)
    }
    
    
    private fun animateStars(imageView: ImageView) {
        YoYo.with(Techniques.Pulse)
            .duration(200)
            .repeat(1)
            .playOn(imageView)
    }

    private fun rate() {
        val sharedPreferences: SharedPreferences = activity.getSharedPreferences("Prayer_pref", 0)
        val editor = sharedPreferences.edit()
        editor.putBoolean("rated", true)
        editor.apply()
        dialog.dismiss()
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}"))
        activity.startActivity(i)
    }


    private fun animateView(view: View) {
        view.animate().setInterpolator(AccelerateDecelerateInterpolator()).setDuration(100).scaleX(1.1f).scaleY(1.1f).withEndAction {
            view.animate().setInterpolator(AccelerateDecelerateInterpolator()).setDuration(100).scaleX(1f).scaleY(1f)
        }
    }
}


class ModalBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.rate_start, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.yesBt).setOnClickListener {
            RateDialog(requireActivity())
            dismiss()
        }

        view.findViewById<View>(R.id.closeBt).setOnClickListener {
            FeedbackActivity.start(requireActivity())
            dismiss()
        }

    }

    companion object {
        const val TAG = "ModalBottomSheet"

        fun show(context: AppCompatActivity) {
            val modalBottomSheet = ModalBottomSheet()
            modalBottomSheet.show(context.supportFragmentManager, TAG)
        }

    }
}