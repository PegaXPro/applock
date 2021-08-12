package com.aviapp.app.security.applocker.ui.image_viewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.aviapp.app.security.applocker.R
import com.aviapp.app.security.applocker.ui.BaseActivity
import com.aviapp.app.security.applocker.ui.vault.vaultlist.VaultListViewModel
import java.io.File

class ImageScrActivity: BaseActivity<VaultListViewModel>() {

    lateinit var image:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_scr)

        image = findViewById(R.id.mainImage)

        findViewById<View>(R.id.backImage).setOnClickListener {
            onBackPressed()
        }


        val imageUri = intent.getStringExtra("uri")
        imageUri?:return

        val imgFile = File(imageUri)

        if (imgFile.exists()) {
            val myBitmap: Bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            image.setImageBitmap(myBitmap)
        }

/*        if (imageUri.endsWith(".jpeg")) {

        }*/

    }

    override fun getViewModel(): Class<VaultListViewModel> = VaultListViewModel::class.java
}