package com.example.maptest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

class MainActivity : AppCompatActivity() {

    private lateinit var areaMetresTV: TextView
    private lateinit var areaArTV: TextView
    private lateinit var areaAcresTV: TextView

    private lateinit var map: MapView
    private var menuIsOpen: Boolean = false

    private var areaGeoPoints: ArrayList<GeoPoint> = ArrayList()
    private var latLngList: ArrayList<LatLng> = ArrayList()

    private var newArea: Area = Area(
        "test", "", "agriculture", areaGeoPoints
    )

    private var areaList: ArrayList<Area> = ArrayList()

    private var areaSize: Double = 0.0

    private lateinit var menuLayout: LinearLayout
    private lateinit var subMenuLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        areaMetresTV = findViewById(R.id.areaInMetres)
        areaArTV = findViewById(R.id.areaInAr)
        areaAcresTV = findViewById(R.id.areaInAcres)

        areaGeoPoints = ArrayList()
        latLngList = ArrayList()
        initializeMap()
        connectMenuLayouts()
        initializeMenuButtons()
        initializeSaveAreaButton()

        var mPolygon = Polygon(map)

        val tapOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                areaGeoPoints.add(point)
                createMarker(areaGeoPoints, map)
                latLngList.add(LatLng(point.latitude, point.longitude))
                if (areaGeoPoints.size > 1) {
                    map.overlays.remove(mPolygon)
                    mPolygon = Polygon(map)
                    mPolygon.fillColor = R.color.black

                    for (gPoint: GeoPoint in areaGeoPoints) {
                        mPolygon.addPoint(gPoint)
                    }
                    map.overlays.add(mPolygon)
                }
                if (areaGeoPoints.size > 2) {
                    areaSize = SphericalUtil.computeArea(latLngList)
                    newArea.size = areaSize.toString()
                    newArea.geoPoints = areaGeoPoints
                    createAreaInformationMarker()
                    updateAreaTv(areaSize)
                }
                reloadMap(map)
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                // Do whatever
                return true
            }
        })
        map.overlays.add(tapOverlay)
        FireStore.getAreas(areaList, this)
    }

    fun addAreasToMap() {
        for (area: Area in areaList) {
            val newArea = Polygon(map)
            for (gPoint: HashMap<String, String> in area.geoPointsToConvert) {
                val latitude = gPoint["latitude"].toString().toDouble()
                val longitude = gPoint["longitude"].toString().toDouble()
                newArea.addPoint(GeoPoint(latitude, longitude))
            }
            Utility.setAreaColor(area.type, newArea, applicationContext)

            map.overlays.add(newArea)
        }
        Log.d("myTag", areaList.toString())
    }

    private fun updateAreaTv(area: Double) {
        areaMetresTV.text = String.format("%.0f m²", area)
        areaArTV.text = String.format("%.2f ar", area / 100)
        areaAcresTV.text = String.format("%.2f ha", area / 10000)
    }

    private fun createAreaInformationMarker() {
        val areaMarker = Marker(map)
        areaMarker.title = String.format("%.0f m²", areaSize)

        var latitude = 0.0
        var longitude = 0.0

        for (point: GeoPoint in areaGeoPoints) {
            latitude += point.latitude
            longitude += point.longitude
        }

        latitude /= areaGeoPoints.size
        longitude /= areaGeoPoints.size

        areaMarker.position = GeoPoint(latitude, longitude)
        map.overlays.add(areaMarker)
        areaMarker.showInfoWindow()
    }

    fun createMarker(points: ArrayList<GeoPoint>, map: MapView) {
        for (gPoint: GeoPoint in points) {
            val newMarker = Marker(map)
            newMarker.position = gPoint
            newMarker.setAnchor(0.175f, 0.35f)
            newMarker.icon = ContextCompat.getDrawable(applicationContext, R.drawable.map_pin)
            map.overlays.add(newMarker)
        }
    }

    fun reloadMap(map: MapView) {
        map.invalidate()
    }

    private fun initializeMap() {
        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        map = findViewById(R.id.mapview)

        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        map.setMultiTouchControls(true)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        map.controller.setZoom(15)
        map.controller.animateTo(GeoPoint(50.5532715, 7.1045565))
    }

    private fun initializeMenuButtons() {
        initializeOpenMenuBtn()
        initializeCloseMenuBtn()
        val menuBtn = findViewById<ImageButton>(R.id.menuBtn)
        val subMenuLayout = findViewById<LinearLayout>(R.id.subMenuLayout)
        val menuLayout = findViewById<LinearLayout>(R.id.menuLayout)

        menuBtn.setOnClickListener {
            if (!menuIsOpen) {
                subMenuLayout.visibility = View.VISIBLE
                menuLayout.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
                map.layoutParams = LayoutParams(0, 0)
            } else {
                subMenuLayout.visibility = View.GONE
                map.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
                menuLayout.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 180, 0f)
            }
            menuIsOpen = !menuIsOpen
        }
    }

    private fun initializeOpenMenuBtn() {
        val openMenuBtn = findViewById<ImageButton>(R.id.menuBtn)

        openMenuBtn.setOnClickListener {
            if (!menuIsOpen) {
                subMenuLayout.visibility = View.VISIBLE
                menuLayout.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
                map.layoutParams = LayoutParams(0, 0)
            } else {
                closeSubMenu()
            }
            menuIsOpen = !menuIsOpen
        }
    }

    private fun closeSubMenu() {
        subMenuLayout.visibility = View.GONE
        map.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        menuLayout.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 180, 0f)
    }

    private fun initializeCloseMenuBtn() {
        val closeMenuBtn = findViewById<ImageButton>(R.id.closeMenuBtn)

        closeMenuBtn.setOnClickListener {
            closeSubMenu()
            menuIsOpen = !menuIsOpen
        }
    }

    private fun connectMenuLayouts() {
        subMenuLayout = findViewById(R.id.subMenuLayout)
        menuLayout = findViewById(R.id.menuLayout)
    }

    private fun initializeSaveAreaButton() {
        val saveAreaBtn = findViewById<ImageButton>(R.id.saveBtn)
        saveAreaBtn.setOnClickListener {
            FireStore.uploadArea(newArea)
            areaGeoPoints.clear()
            latLngList.clear()
        }
    }
}