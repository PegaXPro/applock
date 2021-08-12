package com.aviapp.app.security.applocker.ui.main

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.databinding.ActivityFeedbackBinding
import com.aviapp.app.security.applocker.ui.BaseActivity
import com.aviapp.app.security.applocker.util.GMailSender
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import javax.inject.Inject

class FeedbackActivity : BaseActivity<MainViewModel>() {

    lateinit var binding: ActivityFeedbackBinding
    var message = ""
    lateinit var firebase: FirebaseAnalytics
    @Inject
    lateinit var gmailSender: GMailSender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebase = FirebaseAnalytics.getInstance(this)

        binding.hint1.setOnClickListener {
            selectItem(binding.hint1)
            setMessageFb("Too much ads")
        }

        binding.hint2.setOnClickListener {
            selectItem(binding.hint2)
            setMessageFb("Can’t lock my apps")
        }

        binding.hint3.setOnClickListener {
            selectItem(binding.hint3)
            setMessageFb("App crashed")
        }

        binding.hint4.setOnClickListener {
            selectItem(binding.hint4)
            setMessageFb("Difficult to use")
        }

        binding.hint5.setOnClickListener {
            selectItem(binding.hint5)
            setMessageFb("App doesn’t work")
        }

        binding.hint6.setOnClickListener {
            selectItem(binding.hint6)
            setMessageFb("Other")
        }

        binding.goBack.setOnClickListener {
            onBackPressed()
        }

        binding.send.setOnClickListener {

            messageList.forEach {
                firebase.logEvent(it, null)
            }

            val text = binding.editText1.text.toString()

            when {

//                text == "" -> {
//                    Toast.makeText(this, "fill the form", Toast.LENGTH_SHORT).show()
//                    YoYo.with(Techniques.Pulse)
//                        .duration(300)
//                        .repeat(1)
//                        .playOn(binding.editText1)
//                }

                !adHelper.isOnline() -> {
                    Toast.makeText(this, "check your internet connection", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    gmailSender.sendMail("---", "message: $text")
                    Toast.makeText(this, "Thank you for your letter", Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch {
                        delay(Toast.LENGTH_SHORT.toLong())
                        onBackPressed()
                    }
                }
            }
        }

    }

    private val messageList = mutableListOf<String>()

    private fun setMessageFb(message: String) {
      if (messageList.contains(message)) {
         val index = messageList.indexOf(message)
         messageList.removeAt(index)
      } else {
          messageList.add(message)
      }
    }

    private fun selectItem(backView: TextView) {

        val isSelected = backView.tag == "selected"
        if (isSelected) {
            backView.tag = "unselected"
        } else {
            backView.tag = "selected"
        }

        val textColor = if (isSelected) Color.parseColor("#000000") else Color.parseColor("#ffffff")
        val itemBack = if (isSelected) R.drawable.hints_back else R.drawable.hints_back_select

        backView.setTextColor(textColor)
        backView.setBackgroundResource(itemBack)
    }

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, FeedbackActivity::class.java)
            activity.startActivity(intent)
        }
    }


    override fun getViewModel(): Class<MainViewModel> = MainViewModel::class.java

}