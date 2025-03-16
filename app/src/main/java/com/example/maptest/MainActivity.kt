package com.example.maptest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
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

    private lateinit var areaTV: TextView

    private lateinit var map: MapView
    private var menuIsOpen: Boolean = false

    private lateinit var menuLayout: LinearLayout
    private lateinit var subMenuLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        areaTV = findViewById(R.id.polygonArea)

        val points = ArrayList<GeoPoint>()
        val geoPoints = ArrayList<LatLng>()
        initializeMap()
        connectMenuLayouts()
        initializeMenuButtons()

        var mPolygon = Polygon(map)

        val tapOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                points.add(point)
                createMarker(points, map)
                geoPoints.add(LatLng(point.latitude, point.longitude))
                if (points.size > 1) {
                    map.overlays.remove(mPolygon)
                    mPolygon = Polygon(map)
                    mPolygon.fillColor = R.color.black

                    for (gPoint: GeoPoint in points) {
                        mPolygon.addPoint(gPoint)
                    }
                    map.overlays.add(mPolygon)
                }
                if (points.size > 2) {
                    val area = SphericalUtil.computeArea(geoPoints)

                    val areaMarker = Marker(map)
                    areaMarker.title =  String.format("%.0f m²", area)
                    areaMarker.position = points[0]
                    map.overlays.add(areaMarker)
                    areaMarker.showInfoWindow()

                    updateAreaTv(area)
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


        FireStore.getAreas()
    }

    private fun updateAreaTv(area: Double) {
        areaTV.text = String.format("Area: %.0f m²", area)
    }

    fun createMarker(points: ArrayList<GeoPoint>, map: MapView) {
        for (gPoint: GeoPoint in points) {
            val newMarker = Marker(map)
            newMarker.position = gPoint
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

    private fun closeSubMenu(){
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
}