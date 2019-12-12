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

import com.google.android.gms.maps.CameraUpdateFactory

import android.Manifest.permission
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.maps.android.PolyUtil
import com.ygaps.travelapp.manager.Constant
import com.ygaps.travelapp.manager.doAsync
import kotlinx.android.synthetic.main.activity_tour_follow.*
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


        showLocationPrompt()


        fetchLocation()

        tourFollowChatBtn.setOnClickListener {
            popupChat()
        }



    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val PERMISSION_ID = 12
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LocationRequest.PRIORITY_HIGH_ACCURACY -> {
                if (resultCode == Activity.RESULT_OK) {

                } else {
                    Log.e("Status: ","Off")
                }
            }
        }
    }

    private fun fetchLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                val task = fusedLocationProviderClient.lastLocation
                task.addOnSuccessListener { location ->
                    if (location != null) {
                        myLocation = location
                        val mapFragment =
                            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                        mapFragment.getMapAsync(this)
                    }
                    else {
                        requestNewLocationData()
                    }
                }
            }

        }
        else {
            requestPermissions()
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


    fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            myLocation = locationResult.lastLocation
            val mapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this@TourFollowActivity)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }


        private fun showLocationPrompt() {
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

            val result: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())

            result.addOnCompleteListener { task ->
                try {
                    val response = task.getResult(ApiException::class.java)
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (exception: ApiException) {
                    when (exception.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            try {
                                // Cast to a resolvable exception.
                                val resolvable: ResolvableApiException = exception as ResolvableApiException
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                    this, LocationRequest.PRIORITY_HIGH_ACCURACY
                                )
                            } catch (e: IntentSender.SendIntentException) {
                                // Ignore the error.
                            } catch (e: ClassCastException) {
                                // Ignore, should be an impossible error.
                            }
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.

                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                        }
                    }
                }
            }
        }


    fun popupChat() {
        val inflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_chat, null)

//        var commentView = view.findViewById<RecyclerView>(R.id.commentRecyclerView)
//        val layoutManager = LinearLayoutManager(applicationContext)
//        layoutManager.orientation = LinearLayoutManager.VERTICAL
//        commentView.layoutManager = layoutManager
//        commentView.adapter = CommentAdt
//
//
//        var commentNumField = view.findViewById<TextView>(R.id.commentNum)
//        CommentNumCountView = commentNumField
//        hasInitCommentNumCountView = true
//        commentNumField.text = listComment.size.toString() + " comments"



        val displayMetrics =getResources().getDisplayMetrics()
        val screenWidthInDp = displayMetrics.heightPixels / displayMetrics.density

        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            (screenWidthInDp.toInt() - 100)*2, // Window height
            true
        )

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }


        // If API level 23 or higher then execute the code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Create a new slide animation for popup window enter transition
            val slideIn = Slide()
            slideIn.slideEdge = Gravity.TOP
            popupWindow.enterTransition = slideIn

            // Slide animation for popup window exit transition
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.TOP
            popupWindow.exitTransition = slideOut

        }

        // Get the widgets reference from custom view
        //val tv = view.findViewById<TextView>(R.id.text_view)

//        val sendbtn = view.findViewById<ImageView>(R.id.send)
//        val contentCM = view.findViewById<EditText>(R.id.commenttext)
//
//        sendbtn.setOnClickListener {
//            val data:String = contentCM.text.toString()
//            ApiRequestNewComment(tourId.toString(),"126",data)
//            contentCM.setText("")
//            contentCM.clearFocus()
//        }

        // Set a dismiss listener for popup window
        popupWindow.setOnDismissListener {
            Toast.makeText(applicationContext, "Popup closed", Toast.LENGTH_SHORT).show()
        }


        // Finally, show the popup window on app
        popupWindow.showAsDropDown(tourFollowChatBtn,0,20)
    }



}


