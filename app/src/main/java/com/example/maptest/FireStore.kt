package com.example.maptest

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FireStore {
    companion object {
        fun getAreas() {
            val db = Firebase.firestore

            val areas = db.collection("areas")
        }

        fun uploadArea(area: Area) {
            val db = Firebase.firestore

            db.collection("areas").document(area.areaID).set(area).addOnSuccessListener {
                Log.d("myTag", "New area uploaded to database.")
            }.addOnFailureListener {
                Log.d("myTag", "Failed to upload new area.")
            }
        }
    }
}