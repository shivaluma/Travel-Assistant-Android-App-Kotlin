package com.ygaps.travelapp.view.tour

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.GoogleMap
import com.ygaps.travelapp.R

import com.google.android.gms.maps.SupportMapFragment


import android.location.Location

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory

import android.widget.Toast
import android.Manifest.permission
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.ygaps.travelapp.manager.Constant
import com.ygaps.travelapp.manager.doAsync
import org.json.JSONObject


class TourFollowActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var mGoogleMap: GoogleMap
    lateinit var myLocation: Location
    lateinit var destinationLatLng: LatLng
    lateinit var polyLineToDestination : Polyline
    lateinit var fusedLocationProviderClient : FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_tour_follow)
        fusedLocationProviderClient = FusedLocationProviderClient(this.applicationContext)

        destinationLatLng = LatLng(intent.extras!!.getDouble("destinationLat", 10.7629)
            ,intent.extras!!.getDouble("destinationLng", 106.6822))



        fetchLocation()


    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onMapReady(p0: GoogleMap?) {

        Log.d("abab", "map nhu cc")
        mGoogleMap = p0!!
        mGoogleMap.isMyLocationEnabled = true
        mGoogleMap.uiSettings.isMyLocationButtonEnabled = true

        var src = LatLng(myLocation.latitude,myLocation.longitude)
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(src, 15.0f))

        drawPath(src, destinationLatLng)

    }


    fun drawPolylineFromCurrentLocationToDestination(src : LatLng, dest : LatLng) {

        addMarker(mGoogleMap,src,"My location",R.drawable.ic_startpoint)
        addMarker(mGoogleMap,dest,"Destination",R.drawable.ic_endpoint)
        mGoogleMap.addPolyline(
            PolylineOptions()
                .add(
                    src,
                    dest
                )
                .width(3.0f)
                .color(Color.RED))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                myLocation = location
                val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
        }
    }

    fun addMarker(ggMap: GoogleMap, pos: LatLng, name: String, drawable: Int): Marker {
        return ggMap.addMarker(
            MarkerOptions()
                .position(pos)
                .icon(bitmapDescriptorFromVector(applicationContext, drawable))
                .title(name)
        )
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }


    fun drawPath(src: LatLng, dest: LatLng) {
        addMarker(mGoogleMap,src,"My location",R.drawable.ic_startpoint)
        addMarker(mGoogleMap,dest,"Destination",R.drawable.ic_endpoint)
        doAsync {
            val path: MutableList<List<LatLng>> = ArrayList()
            val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=${src.latitude},${src.longitude}&destination=${dest.latitude},${dest.longitude}&key=${Constant.ggMapApiKey}"
            val directionsRequest = object : StringRequest(Request.Method.GET, urlDirections, Response.Listener<String> {
                    response ->
                val jsonResponse = JSONObject(response)
                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val steps = legs.getJSONObject(0).getJSONArray("steps")
                for (i in 0 until steps.length()) {
                    val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    path.add(PolyUtil.decode(points))
                }
                for (i in 0 until path.size) {
                    mGoogleMap.addPolyline(PolylineOptions().addAll(path[i]).color(Color.BLUE).width(5.0f))
                }
            }, Response.ErrorListener {
                    _ ->
            }){}
            val requestQueue = Volley.newRequestQueue(this)
            requestQueue.add(directionsRequest)
        }.execute()
    }
}


