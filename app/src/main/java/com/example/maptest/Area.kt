package com.example.maptest

import org.osmdroid.util.GeoPoint
import java.util.UUID

data class Area(
    var areaID: String = UUID.randomUUID().toString(),
    var name: String,
    var size: String,
    var geoPoints: ArrayList<GeoPoint>,
    var createdOn: String
)