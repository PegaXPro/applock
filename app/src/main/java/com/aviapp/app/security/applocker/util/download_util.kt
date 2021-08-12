package com.aviapp.app.security.applocker.util

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class FirebaseDownloader {

    private val storage = Firebase.storage
    private val storageRef = storage.reference

    fun getImage(path: String) {
        val pathReference = storageRef.child("theme1.jpg")

        val ONE_MEGABYTE: Long = 1024 * 1024
        pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener {

        }.addOnFailureListener {

        }
    }

}