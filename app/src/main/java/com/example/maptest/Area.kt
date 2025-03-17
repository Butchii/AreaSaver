package com.example.maptest

import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.util.Date
import java.util.UUID

data class Area(
    var name: String,
    var size: String,
    var type: String,
    var geoPoints: ArrayList<GeoPoint>,
    var createdOn: String = Date().toString(),
    var areaID: String = UUID.randomUUID().toString(),
    var geoPointsToConvert: ArrayList<HashMap<String,String>> = ArrayList(),
)