package com.example.maptest

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

class FireStore {
    companion object {
        fun getAreas(areaList: ArrayList<Area>, activity: MainActivity) {
            val db = Firebase.firestore

            db.collection("areaList").get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val newArea = Area(
                        document.get("name") as String,
                        document.get("size") as String,
                        document.get("type") as String,
                        document.get("geoPoints") as ArrayList<GeoPoint>,
                        document.get("createdOn") as String,
                        document.get("areaID") as String
                    )
                    newArea.geoPointsToConvert.addAll(document.get("geoPoints") as ArrayList<HashMap<String, String>>)
                    areaList.add(newArea)
                }
                activity.addAreasToMap()
            }
        }

        fun uploadArea(area: Area) {
            val db = Firebase.firestore
            Log.d("myTag", area.toString())
            db.collection("areaList").document(area.areaID).set(area)
        }
    }
}
