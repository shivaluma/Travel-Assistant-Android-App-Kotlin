package com.ygaps.travelapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ygaps.travelapp.manager.Constant
import com.ygaps.travelapp.manager.doAsync
import com.ygaps.travelapp.model.StopPoint
import com.ygaps.travelapp.network.model.*
import com.ygaps.travelapp.util.util
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.jaredrummler.materialspinner.MaterialSpinner
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import kotlinx.android.synthetic.main.activity_get_coordinate.*
import kotlinx.android.synthetic.main.activity_stop_point_info.view.*
import kotlinx.android.synthetic.main.fragment_explorer.view.*
import kotlinx.android.synthetic.main.popup_stoppoint_suggest.view.*
import kotlinx.android.synthetic.main.popup_suggest_point_onclick.view.*
import kotlinx.android.synthetic.main.stoppoint.*
import kotlinx.android.synthetic.main.stoppoint.view.*
import kotlinx.android.synthetic.main.stoppointinfo.view.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class GetCoordinateActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    lateinit var mPlacesClient: PlacesClient
    lateinit var mListAutoCompletePredition: List<AutocompletePrediction>
    lateinit var mLastKnowLocation: Location
    lateinit var locationCallback: LocationCallback
    lateinit var mLocationRequest: LocationRequest

    var mStopPointArrayList = ArrayList<stopPoint>()
    var mStopPointMarkerArrayList = ArrayList<Marker>()
    var mPolyLineArrayList = ArrayList<Polyline>()
    internal var mGoogleApiClient: GoogleApiClient? = null
    internal var mCurrLocationMarker: Marker? = null
    lateinit var autocompletetoken: AutocompleteSessionToken
    lateinit var LastStartPointLatLng: LatLng
    lateinit var LastEndPointLatLng: LatLng
    lateinit var LastStartMarker: Marker
    lateinit var LastEndMarker: Marker
    lateinit var token : String

    var getSuggestPointMode = false

    var listSuggestPoint = ArrayList<LatLng>()
    var listSuggestPointMarker = ArrayList<Marker>()
    var listStopPointSearch = ArrayList<StopPoint>()
    var listStopPointSuggest = ArrayList<StopPoint>()
    var listStopPointSuggestMarker= ArrayList<Marker>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_coordinate)
        supportActionBar!!.hide()
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        Places.initialize(this, Constant.ggMapApiKey)
        mPlacesClient = Places.createClient(this)
        autocompletetoken = AutocompleteSessionToken.newInstance()
        val sharePref : SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
        token = sharePref.getString("token", "notoken")!!



        btnFloatFinish.setOnClickListener {
            if (checkNoStartEndPoint()) {
                Toast.makeText(
                    applicationContext,
                    "No Start/End Point is defined",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val sharePref: SharedPreferences =
                    this.getSharedPreferences("logintoken", Context.MODE_PRIVATE)
                var logintoken = sharePref.getString("token", "nnn")!!

                var extras: Bundle = intent.extras!!
                val jsonObject = JsonObject()
                jsonObject.addProperty("name", extras.getString("iTourName"))
                jsonObject.addProperty("startDate", extras.getLong("iStartDate"))
                jsonObject.addProperty("endDate", extras.getLong("iEndDate"))
                jsonObject.addProperty("sourceLat", LastStartPointLatLng.latitude)
                jsonObject.addProperty("sourceLong", LastStartPointLatLng.longitude)
                jsonObject.addProperty("destLat", LastEndPointLatLng.latitude)
                jsonObject.addProperty("destLong", LastEndPointLatLng.longitude)
                jsonObject.addProperty("isPrivate", extras.getBoolean("iIsPrivate"))
                jsonObject.addProperty("adults", extras.getInt("iAdultNum"))
                jsonObject.addProperty("childs", extras.getInt("iChildNum"))
                jsonObject.addProperty("minCost", extras.getLong("iMinCost"))
                jsonObject.addProperty("maxCost", extras.getLong("iMaxCost"))
                if (!extras.getString("iImage").isNullOrEmpty()) {
                    jsonObject.addProperty("avatar", extras.getString("iImage"))
                }


                val service = WebAccess.retrofit.create(ApiServiceCreateTour::class.java)

                val call = service.postData(logintoken, jsonObject)

                call.enqueue(object : Callback<ResponseCreateTour> {
                    override fun onFailure(call: Call<ResponseCreateTour>, t: Throwable) {
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(
                        call: Call<ResponseCreateTour>,
                        response: Response<ResponseCreateTour>
                    ) {
                        if (response.code() == 200) {
                            Log.d("resres", "Add tour")
                            Log.d("resres", response.code().toString() + response.message())
                            Log.d("resres", response.body().toString())
                            Toast.makeText(
                                applicationContext,
                                "Create Tour Successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            val newTourId = response.body()!!.id
                            deleteStartEndPoint(mStopPointArrayList)
                            val stpJsonObj = JsonObject()
                            stpJsonObj.addProperty("tourId", response.body()!!.id.toString())
                            val arrlistJson = Gson().toJson(mStopPointArrayList)
                            val parser = JsonParser()
                            val jsonStopPint = parser.parse(arrlistJson)
                            stpJsonObj.add("stopPoints", jsonStopPint)
                            val servicestp =
                                WebAccess.retrofit.create(ApiServiceAddStopPointToTour::class.java)
                            val callstp = servicestp.postData(logintoken, stpJsonObj)
                            callstp.enqueue(object : Callback<ResponseAddStopPoint> {
                                override fun onFailure(
                                    call: Call<ResponseAddStopPoint>,
                                    t: Throwable
                                ) {
                                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG)
                                        .show()
                                }

                                override fun onResponse(
                                    call: Call<ResponseAddStopPoint>,
                                    response: Response<ResponseAddStopPoint>
                                ) {
                                    if (response.code() == 200) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Add Stop Point Successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.d("resres", "Add stop point")
                                        Log.d(
                                            "resres",
                                            response.code().toString() + response.message()
                                        )
                                        Log.d("resres", stpJsonObj.toString())
                                        Log.d("resres", response.body().toString())
                                        val intent = Intent(
                                            applicationContext,
                                            TourInfoActivity::class.java
                                        )
                                        intent.putExtra("token", token)
                                        intent.putExtra("tourID", newTourId)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            applicationContext,
                                            response.message().toString(),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            })
                        } else {
                            val gson = Gson()
                            val type = object : TypeToken<ErrorResponse>() {}.type
                            var errorResponse: ErrorResponse? = gson.fromJson(response.errorBody()!!.charStream(), type)
                            Toast.makeText(applicationContext, errorResponse!!.message, Toast.LENGTH_LONG).show()
                            Log.d("resres", errorResponse!!.message)
                            Log.d("resres", response.errorBody()!!.charStream().toString())
                        }
                    }
                })
            }
        }


        btnFloatGetListPoint.setOnClickListener {
            val inflater: LayoutInflater =
                getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.stoppointlist, null)
            val lv = view.findViewById<ListView>(R.id.stplv)
            val stoppointadapter = stopPointAdapter(mStopPointArrayList, this)
            lv.adapter = stoppointadapter


            val popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                LinearLayout.LayoutParams.WRAP_CONTENT // Window height
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
                slideOut.slideEdge = Gravity.RIGHT
                popupWindow.exitTransition = slideOut

            }

            // Get the widgets reference from custom view
            //val tv = view.findViewById<TextView>(R.id.text_view)
            val buttonPopup = view.findViewById<ImageButton>(R.id.btnCloseStopPointList)


            // Set a click listener for popup's button widget
            buttonPopup.setOnClickListener {
                // Dismiss the popup window
                popupWindow.dismiss()
            }

            // Set a dismiss listener for popup window
            popupWindow.setOnDismissListener {
                Toast.makeText(applicationContext, "Popup closed", Toast.LENGTH_SHORT).show()
            }


            // Finally, show the popup window on app
            TransitionManager.beginDelayedTransition(root_layout)
            popupWindow.showAtLocation(
                root_layout, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )


            lv.setOnItemClickListener { _, view, position, id ->
                popupWindow.dismiss()
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            mStopPointArrayList[position].lat!!,
                            mStopPointArrayList[position].long!!
                        ),
                        15.0f
                    )
                )
            }
        }

        //searchMapBar.setCustomSuggestionAdapter()

        searchMapBar.setOnSearchActionListener(
            object : MaterialSearchBar.OnSearchActionListener {
                override fun onButtonClicked(buttonCode: Int) {
                    if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                        //opening or closing a navigation drawer
                    } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                        searchMapBar.disableSearch()
                    }
                    searchMapBar.clearSuggestions()
                }

                override fun onSearchStateChanged(enabled: Boolean) {

                }

                override fun onSearchConfirmed(text: CharSequence?) {
                    searchLocation(text.toString())
                    searchMapBar.clearSuggestions()
//                    ApiRequestSearchDestination(text.toString())

                }
            }
        )


        searchMapBar.setSuggestionsClickListener(object : SuggestionsAdapter.OnItemViewClickListener {
            override fun OnItemDeleteListener(position: Int, v: View?) {

            }

            override fun OnItemClickListener(position: Int, v: View?) {
                popupSuggestPointInfoFromSearch(position)
            }
        })



        searchMapBar.addTextChangeListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    ApiRequestSearchDestination(s.toString())
                }
                else {
                    searchMapBar.clearSuggestions()
                }
            }
        })
    }

    override fun onMapReady(mgoogleMap: GoogleMap) {
        googleMap = mgoogleMap
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(10.762913, 106.6821717),
                15.0f
            )
        )
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                buildGoogleApiClient()
                googleMap.isMyLocationEnabled = true
            }
        } else {
            buildGoogleApiClient()
            googleMap.isMyLocationEnabled = true
        }

        googleMap.setOnMapClickListener {
            val latlng = it

            val view = inflateAddStopPointView()

            val addressField = view.findViewById<EditText>(R.id.editAddress)
            addressField.setText(getAddressByLocation(latlng!!))



            val popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                LinearLayout.LayoutParams.WRAP_CONTENT, // Window height
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
                slideOut.slideEdge = Gravity.RIGHT
                popupWindow.exitTransition = slideOut

            }

            // Get the widgets reference from custom view
            //val tv = view.findViewById<TextView>(R.id.text_view)
            val buttonPopup = view.findViewById<ImageButton>(R.id.btnCloseStopPoint)

            // Set a dismiss listener for popup window
            popupWindow.setOnDismissListener {
                Toast.makeText(applicationContext, "Popup closed", Toast.LENGTH_SHORT).show()
            }

            // marker onclick


            // Set a click listener for popup's button widget
            buttonPopup.setOnClickListener {
                saveStopPointToList(view,latlng,popupWindow)
            }

            // Finally, show the popup window on app
            TransitionManager.beginDelayedTransition(root_layout)
            popupWindow.showAtLocation(
                root_layout, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )

        }

        googleMap.setOnMapLongClickListener {
            if (!getSuggestPointMode) {
                getSuggestPoint.visibility = View.VISIBLE
                ExitMode.visibility = View.VISIBLE
                getSuggestPoint.setOnClickListener {
                    if (listSuggestPoint.size == 0) {
                        Toast.makeText(
                            applicationContext,
                            "Chưa có Suggest Point",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else if (listSuggestPoint.size == 1) {
                        var body = JsonObject()
                        var coor = JsonObject()
                        coor.addProperty("lat", listSuggestPoint[0].latitude)
                        coor.addProperty("long", listSuggestPoint[0].longitude)
                        body.addProperty("hasOneCoordinate", true)
                        body.add("coordList", coor)
                        ApiRequestGetNearbyPoint(body)
                    }

                    else if (listSuggestPoint.size > 1) {

                        var body = JsonObject()
                        var coorlist = JsonArray()
                        var array = JsonArray()
                        for (i in 0..listSuggestPoint.size - 2) {
                            var coor = JsonObject()
                            coor.addProperty("lat", listSuggestPoint[i].latitude)
                            coor.addProperty("long", listSuggestPoint[i].longitude)
                            var coor2 = JsonObject()
                            coor2.addProperty("lat", listSuggestPoint[i+1].latitude)
                            coor2.addProperty("long", listSuggestPoint[i+1].longitude)
                            array.add(coor)
                            array.add(coor2)
                            var coorListObject = JsonObject()
                            coorListObject.add("coordinateSet", array)
                            array = JsonArray()
                            coorlist.add(coorListObject)
                        }
                        body.addProperty("hasOneCoordinate", false)
                        body.add("coordList", coorlist)
                        ApiRequestGetNearbyPoint(body)
                    }
                }

                ExitMode.setOnClickListener {
                    getSuggestPointMode = false
                    getSuggestPoint.visibility = View.GONE
                    ExitMode.visibility = View.GONE
                    listSuggestPoint.clear()
                    for (i in listSuggestPointMarker) i.remove()
                    listSuggestPointMarker.clear()
                    clearSuggestPointOnMap()
                }
            }

            var marker = googleMap.addMarker(MarkerOptions().position(it).title((listSuggestPoint.size + 1).toString()))
            listSuggestPoint.add(it)
            marker.tag = "suggestpoint"
            listSuggestPointMarker.add(marker)
        }

        googleMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(p0: Marker?): Boolean {
                 if (p0!!.tag.toString().contains("suggeststoppoint")) {
                     var splitList = p0.tag.toString().split(" ")
                     var serviceId : String = splitList[1]
                     Log.d("abab",serviceId)
                    popupSuggestPointInfo(serviceId.toInt())
                }
                else if (p0.tag.toString() == "suggestpoint") {

                 }
                else {
                     val view = layoutInflater.inflate(R.layout.bottomsheet_add_point, null)
                     val dialog = BottomSheetDialog(this@GetCoordinateActivity)
                     dialog.setContentView(view)
                     dialog.show()
                     val remove = view.findViewById<LinearLayout>(R.id.deleteSuggestBtn)
                     remove.setOnClickListener {
                         var index = findIndexByTag(p0.tag.toString())
                         Log.d("indexd", index.toString())
                         if (index > -1) {
                             mStopPointArrayList.removeAt(index)
                         }
                         p0.remove()
                         dialog.dismiss()
                         drawThePath()
                         Toast.makeText(applicationContext, "Deleted", Toast.LENGTH_LONG)
                             .show()
                     }

                     view.editSuggestBtn.setOnClickListener {
                         dialog.dismiss()

                         var index = findIndexByTag(p0.tag.toString())
                         Log.d("indexd", index.toString())
                         p0.remove()
                         val popup = inflateAddStopPointView()
                         val item = mStopPointArrayList[index]
                         var latlng = LatLng(item.lat!!, item.long!!)


                             popup.editStopPointName.setText(item.name)
                             popup.editAddress.setText(item.address)
                             popup.editMinCost.setText(item.minCost.toString())
                             popup.editMaxCost.setText(item.maxCost.toString())
                             val splitArrive = util.longToDateTime(item.arrivalAt!!).split(" ")
                             popup.editTimeArrive.setText(splitArrive[0])
                             popup.editDateArrive.setText(splitArrive[1])

                             val splitLeave = util.longToDateTime(item.leaveAt!!).split(" ")
                             popup.editTimeLeave.setText(splitLeave[0])
                             popup.editDateLeave.setText(splitLeave[1])

                             popup.spinnerProvince.selectedIndex = item.provinceId!!

                             Log.d("ababab", item.serviceTypeId.toString())
                             if (item.type == "Start Point") {
                                 popup.spinnerType.selectedIndex = 0
                             }
                             else if (item.type == "End Point") {
                                 popup.spinnerType.selectedIndex = 1
                             }
                             else {
                                 popup.spinnerType.selectedIndex = item.serviceTypeId!! + 1
                             }




                         val popupAddSuggest = PopupWindow(
                             popup, // Custom view to show in popup window
                             LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                             LinearLayout.LayoutParams.WRAP_CONTENT, // Window height
                             true
                         )

                         popup.btnCloseStopPoint.setOnClickListener {
                             saveStopPointToList(popup,latlng,popupAddSuggest,item.serviceId,index)
                         }

                         popupAddSuggest.showAtLocation(
                             root_layout, // Location to display popup window
                             Gravity.CENTER, // Exact position of layout to display popup
                             0, // X offset
                             0 // Y offset
                         )
                     }
                 }
                return false
            }
        })


    }

    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
        mGoogleApiClient!!.connect()
    }

    override fun onLocationChanged(location: Location?) {
//        mLastKnowLocation = location!!
//        if (mCurrLocationMarker != null) {
//            mCurrLocationMarker!!.remove()
//        }
//        //Place current location marker
//        val latLng = LatLng(location.latitude, location.longitude)
//        val markerOptions = MarkerOptions()
//        markerOptions.position(latLng)
//        markerOptions.title("Current Position")
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
//        mCurrLocationMarker = googleMap.addMarker(markerOptions)
//
//        //move map camera
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
//        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
//
//        //stop location updates
//        if (mGoogleApiClient != null) {
//            LocationServices.getFusedLocationProviderClient(this)
//        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String?) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(provider: String?) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnected(p0: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.getFusedLocationProviderClient(this)
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        //To change body of created functions use File | Settings | File Templates.
    }

    fun searchLocation(location: String) {
        var addressList: List<Address>? = null

        if (location == null || location == "") {
            Toast.makeText(applicationContext, "provide location", Toast.LENGTH_SHORT).show()
        } else {
            val geoCoder = Geocoder(this)
            try {
                addressList = geoCoder.getFromLocationName(location, 1)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (addressList == null || addressList.isEmpty()) {
                Toast.makeText(applicationContext, "Khong tim thay dia diem nay", Toast.LENGTH_LONG)
                    .show()
            } else {
                val address = addressList[0]
                val latLng = LatLng(address.latitude, address.longitude)
                googleMap.addMarker(MarkerOptions().position(latLng).title(location))
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f))
                Toast.makeText(
                    applicationContext,
                    address.latitude.toString() + " " + address.longitude,
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }


    inner class stopPointAdapter : BaseAdapter {

        var listTourArr = ArrayList<stopPoint>()
        var context: Context? = null

        constructor(listTourArr: ArrayList<stopPoint>, context: Context) : super() {
            this.listTourArr = listTourArr
            this.context = context
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            //dua item vao
            var myView = layoutInflater.inflate(R.layout.stoppointinfo, null)
            var myStopPint = listTourArr[position]
            myView.showSTPName.text = myStopPint.name
            myView.showSTPType.text = myStopPint.type
            myView.showSTPAddr.text = myStopPint.address
            myView.showProvince.text = Constant.provinceList.get(myStopPint.provinceId!!)
            if (!myStopPint.arrivalAt.toString().isNullOrEmpty()) {
                myView.showArrive.text = util.longToDateTime(myStopPint.arrivalAt!!)
            }
            if (!myStopPint.leaveAt.toString().isNullOrEmpty()) {
                myView.showLeave.text = util.longToDateTime(myStopPint.leaveAt!!)
            }
            return myView
        }

        override fun getItem(position: Int): Any {
            return listTourArr[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return listTourArr.size
        }

    }

    inner class stopPoint {
        var id : Int ?= null
        var serviceId : Int ?= null
        var name: String = ""
        var type: String = ""
        var address: String = ""
        var provinceId: Int? = null
        var lat: Double? = null
        var long: Double? = null
        var minCost: Int? = null
        var maxCost: Int? = null
        var arrivalAt: Long? = null
        var leaveAt: Long? = null
        var serviceTypeId: Int? = null
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


    fun addMarker(ggMap: GoogleMap, pos: LatLng, name: String, drawable: Int): Marker {
        return ggMap.addMarker(
            MarkerOptions()
                .position(pos)
                .icon(bitmapDescriptorFromVector(this, drawable))
                .title(name)
        )
    }


    fun deleteStartEndPoint(arr: ArrayList<stopPoint>) {
        arr.removeIf { item -> (item.type == "Start Point" || item.type == "End Point") }
    }


    fun sortTheListStopPoint() {
        var startPoint = stopPoint()
        var endPoint = stopPoint()
        var hasStart = false
        var hasEnd = false
        for (i in mStopPointArrayList) {
            if (i.type == "Start Point") {
                startPoint = i
                hasStart = true
            }
            if (i.type == "End Point") {
                endPoint = i
                hasEnd = true
            }
        }

        deleteStartEndPoint(mStopPointArrayList)
        if (hasStart) mStopPointArrayList.add(0, startPoint)
        if (hasEnd) mStopPointArrayList.add(endPoint)
    }

    fun checkNoStartEndPoint(): Boolean {
        sortTheListStopPoint()
        return (mStopPointArrayList.size < 2 || mStopPointArrayList[0].type != "Start Point" || mStopPointArrayList[mStopPointArrayList.size - 1].type != "End Point")
    }

    fun drawThePath() {
        sortTheListStopPoint()
        for (i in mPolyLineArrayList) {
            i.remove()
        }
        mPolyLineArrayList.clear()
        if (mStopPointArrayList.size >= 2) {
            for (i in 0..mStopPointArrayList.size - 2) {
                var line: Polyline = googleMap.addPolyline(
                    PolylineOptions()
                        .add(
                            LatLng(
                                mStopPointArrayList[i].lat!!,
                                mStopPointArrayList[i].long!!
                            ),
                            LatLng(
                                mStopPointArrayList[i + 1].lat!!,
                                mStopPointArrayList[i + 1].long!!
                            )
                        )
                        .width(5.0f)
                        .color(Color.RED)
                )
                mPolyLineArrayList.add(line)
            }
        }
    }

    fun findIndexByTag(tag: String): Int {
        for (i in 0..mStopPointArrayList.size - 1) {
            Log.d("tagtag", tag)
            Log.d(
                "tagtag",
                mStopPointArrayList[i].name + mStopPointArrayList[i].address + mStopPointArrayList[i].type
            )
            if (tag == (mStopPointArrayList[i].name + mStopPointArrayList[i].address + mStopPointArrayList[i].type)) {
                return i
            }
        }
        return -1
    }

    fun getAddressByLocation(p: LatLng): String {
        var geocoder = Geocoder(this)
        var addresses = ArrayList<Address>() as List<Address>
        try {
            addresses = geocoder.getFromLocation(p.latitude, p.longitude, 1)
        }
        catch (e : IOException) {
            when{
                e.message == "grpc failed" ->  {
                    Toast.makeText(
                        applicationContext,
                        "grpc failed",
                        Toast.LENGTH_LONG
                    ).show()
                    return ""
                }
                else -> throw e
            }
        }
        Log.d("addr",addresses.toString())
        val address = addresses.get(0)
        return address.getAddressLine(0)
    }

    fun ApiRequestSearchDestination(searchKey : String) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceSearchDestination::class.java)
            val call = service.search(token,searchKey,1,"9999")
            call.enqueue(object : Callback<ResponseSearchDestination> {
                override fun onFailure(call: Call<ResponseSearchDestination>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }
                override fun onResponse(
                    call: Call<ResponseSearchDestination>,
                    response: Response<ResponseSearchDestination>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(applicationContext, response.errorBody().toString(), Toast.LENGTH_LONG).show()
                    } else {
                        Log.d("abab",response.body().toString())
                        listStopPointSearch.clear()
                        listStopPointSearch.addAll(response.body()!!.stopPoints)
                        var suggestionsList = ArrayList<String>()
                        for (i in listStopPointSearch.indices) {
                            var line : String = "id : " + listStopPointSearch[i].id + " - " + listStopPointSearch[i].name + "\n" +
                                     "Address : "  + listStopPointSearch[i].address
                            suggestionsList.add(line)
                        }

                        searchMapBar.updateLastSuggestions(suggestionsList)
                        if (!searchMapBar.isSuggestionsVisible) {
                            searchMapBar.showSuggestionsList()
                        }

                    }
                }
            })
        }.execute()
    }

    fun popupSetting(num : String) {
        val inflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_suggest_point_onclick, null)



        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Window height
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
            slideIn.slideEdge = Gravity.BOTTOM
            popupWindow.enterTransition = slideIn

            // Slide animation for popup window exit transition
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.BOTTOM
            popupWindow.exitTransition = slideOut

        }




        // Set a dismiss listener for popup window
        popupWindow.setOnDismissListener {
            Toast.makeText(applicationContext, "Popup closed", Toast.LENGTH_SHORT).show()
        }


        // Finally, show the popup window on app
        popupWindow.showAtLocation(
            root_layout, // Location to display popup window
            Gravity.BOTTOM, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
    }


    fun ApiRequestGetNearbyPoint(body : JsonObject) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceSuggestDestination::class.java)

            val call = service.getSuggest(token,body)
            call.enqueue(object : Callback<ResponseSuggestDestination> {
                override fun onFailure(call: Call<ResponseSuggestDestination>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }
                override fun onResponse(
                    call: Call<ResponseSuggestDestination>,
                    response: Response<ResponseSuggestDestination>
                ) {
                    Log.d("abab",response.message())
                    if (response.code() != 200) {
                        Toast.makeText(applicationContext, response.errorBody().toString(), Toast.LENGTH_LONG).show()
                    } else {
                        Log.d("abab",response.body().toString())
                        listStopPointSuggest.clear()
                        listStopPointSuggest.addAll(response.body()!!.stopPoints)
                        showSuggestPointToMap()
                    }
                }
            })
        }.execute()
    }

    fun showSuggestPointToMap() {
        clearSuggestPointOnMap()
        var item : StopPoint
        for (i in 0..listStopPointSuggest.size-1) {
            item = listStopPointSuggest[i]
            var marker : Marker
            var latLng = LatLng(item.lat!!,item.long!!)
            if (item.serviceTypeId == 1) {
                 marker = addMarker(googleMap, latLng, item.name, R.drawable.ic_restaurant)
            }
            else if (item.serviceTypeId == 2) {
                marker = addMarker(googleMap, latLng, item.name, R.drawable.ic_hotel)
            }
            else if (item.serviceTypeId == 3) {
                marker = addMarker(googleMap, latLng, item.name, R.drawable.ic_bedtime)
            }
            else {
                marker = addMarker(googleMap, latLng, item.name, R.drawable.ic_pin)
            }
            marker.tag = "suggeststoppoint ${i}"
            listStopPointSuggestMarker.add(marker)
        }
    }

    fun clearSuggestPointOnMap() {
        for (i in listStopPointSuggestMarker) i.remove()
        listStopPointSuggestMarker.clear()
    }

    fun popupSuggestPointInfo(pos : Int) {
        val inflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_stoppoint_suggest, null)

        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Window height
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
            slideIn.slideEdge = Gravity.BOTTOM
            popupWindow.enterTransition = slideIn


        }


        view.stpSuggestName.text = listStopPointSuggest[pos].name
        view.stpSuggestAddress.text = listStopPointSuggest[pos].address
        view.stpSuggestContact.text = listStopPointSuggest[pos].contact
        var costString = listStopPointSuggest[pos].minCost.toString() + " - " + listStopPointSuggest[pos].maxCost.toString()
        view.stpSuggestCost.text = costString
        view.serviceSuggestTypeText.text = util.StopPointTypeToString(listStopPointSuggest[pos].serviceTypeId!!)

        view.addSuggestToTour.setOnClickListener {
            var item = listStopPointSuggest[pos]
            popupWindow.dismiss()
            val view = inflateAddStopPointView()
            var latlng = LatLng(item.lat!!, item.long!!)

            view.editStopPointName.setText(item.name)
            view.editAddress.setText(item.address)
            view.editMinCost.setText(item.minCost.toString())
            view.editMaxCost.setText(item.minCost.toString())
            view.spinnerType.selectedIndex = item.serviceTypeId!! + 1




            val popupAddSuggest = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                LinearLayout.LayoutParams.WRAP_CONTENT, // Window height
                true
            )

            view.btnCloseStopPoint.setOnClickListener {
                saveStopPointToList(view,latlng,popupAddSuggest,item.serviceId)
            }

            popupAddSuggest.showAtLocation(
                root_layout, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )
        }


        // Set a dismiss listener for popup window
        popupWindow.setOnDismissListener {



        }


        // Finally, show the popup window on app
        popupWindow.showAtLocation(
            root_layout, // Location to display popup window
            Gravity.BOTTOM, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
    }


    fun popupSuggestPointInfoFromSearch(pos : Int) {
        val inflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_stoppoint_suggest, null)

        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Window height
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
            slideIn.slideEdge = Gravity.BOTTOM
            popupWindow.enterTransition = slideIn


        }


        view.stpSuggestName.text = listStopPointSearch[pos].name
        view.stpSuggestAddress.text = listStopPointSearch[pos].address
        view.stpSuggestContact.text = listStopPointSearch[pos].contact
        var costString = listStopPointSearch[pos].minCost.toString() + " - " + listStopPointSearch[pos].maxCost.toString()
        view.stpSuggestCost.text = costString
        view.serviceSuggestTypeText.text = util.StopPointTypeToString(listStopPointSearch[pos].serviceTypeId!!)

        view.addSuggestToTour.setOnClickListener {
            var item = listStopPointSearch[pos]
            popupWindow.dismiss()
            val view = inflateAddStopPointView()
            var latlng = LatLng(item.lat!!, item.long!!)

            view.editStopPointName.setText(item.name)
            view.editAddress.setText(item.address)
            view.editMinCost.setText(item.minCost.toString())
            view.editMaxCost.setText(item.minCost.toString())
            view.spinnerType.selectedIndex = item.serviceTypeId!! + 1


            val popupAddSuggest = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                LinearLayout.LayoutParams.WRAP_CONTENT, // Window height
                true
            )

            view.btnCloseStopPoint.setOnClickListener {
                saveStopPointToList(view,latlng,popupAddSuggest,item.serviceId)
            }

            popupAddSuggest.showAtLocation(
                root_layout, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )
        }


        // Set a dismiss listener for popup window
        popupWindow.setOnDismissListener {

        }


        // Finally, show the popup window on app
        popupWindow.showAtLocation(
            root_layout, // Location to display popup window
            Gravity.BOTTOM, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
    }


    fun inflateAddStopPointView() : View {
        val inflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.stoppoint, null)
        val spinnertype = view.findViewById<MaterialSpinner>(R.id.spinnerType)
        val spinnerprovince = view.findViewById<MaterialSpinner>(R.id.spinnerProvince)
        spinnertype.setItems(
            "Start Point",
            "End Point",
            "Restaurant",
            "Hotel",
            "Rest Station",
            "Others"
        )
        spinnerprovince.setItems(
            "Hồ Chí Minh",
            "Hà Nội",
            "Đà Nẵng",
            "Bình Dương",
            "Đồng Nai",
            "Khánh Hòa",
            "Hải Phòng",
            "Long An",
            "Quảng Nam",
            "Bà Rịa Vũng Tàu",
            "Đắk Lắk",
            "Cần Thơ",
            "Bình Thuận  ",
            "Lâm Đồng",
            "Thừa Thiên Huế",
            "Kiên Giang",
            "Bắc Ninh",
            "Quảng Ninh",
            "Thanh Hóa",
            "Nghệ An",
            "Hải Dương",
            "Gia Lai",
            "Bình Phước",
            "Hưng Yên",
            "Bình Định",
            "Tiền Giang",
            "Thái Bình",
            "Bắc Giang",
            "Hòa Bình",
            "An Giang",
            "Vĩnh Phúc",
            "Tây Ninh",
            "Thái Nguyên",
            "Lào Cai",
            "Nam Định",
            "Quảng Ngãi",
            "Bến Tre",
            "Đắk Nông",
            "Cà Mau",
            "Vĩnh Long",
            "Ninh Bình",
            "Phú Thọ",
            "Ninh Thuận",
            "Phú Yên",
            "Hà Nam",
            "Hà Tĩnh",
            "Đồng Tháp",
            "Sóc Trăng",
            "Kon Tum",
            "Quảng Bình",
            "Quảng Trị",
            "Trà Vinh",
            "Hậu Giang",
            "Sơn La",
            "Bạc Liêu",
            "Yên Bái",
            "Tuyên Quang",
            "Điện Biên",
            "Lai Châu",
            "Lạng Sơn",
            "Hà Giang",
            "Bắc Kạn",
            "Cao Bằng"
        )

        var timeArrive = view.findViewById<EditText>(R.id.editTimeArrive)
        timeArrive.setOnClickListener {
            util.setOnClickTime(timeArrive, this)
        }

        var timeLeave = view.findViewById<EditText>(R.id.editTimeLeave)
        timeLeave.setOnClickListener {
            util.setOnClickTime(timeLeave, this)
        }

        var dateArrive = view.findViewById<EditText>(R.id.editDateArrive)
        var dateLeave = view.findViewById<EditText>(R.id.editDateLeave)



        dateArrive.setOnClickListener {
            util.setOnClickDate(dateArrive, this)
        }

        dateLeave.setOnClickListener {
            util.setOnClickDate(dateLeave, this)
        }


        return view
    }


    fun saveStopPointToList(view : View, latlng : LatLng, popupWindow: PopupWindow, id : Int? = -1, oldIndex : Int = -1) {
        var stoppint = stopPoint()
        var namefield = view.findViewById<EditText>(R.id.editStopPointName)
        var addressfield = view.findViewById<EditText>(R.id.editAddress)
        var timearrfield = view.findViewById<EditText>(R.id.editTimeArrive)
        var datearrfield = view.findViewById<EditText>(R.id.editDateArrive)
        var timeleavefield = view.findViewById<EditText>(R.id.editTimeLeave)
        var dateleavefield = view.findViewById<EditText>(R.id.editDateLeave)
        if (namefield.text.isNullOrEmpty()) {
            namefield.error = "Required*"
            namefield.requestFocus()
        } else if (addressfield.text.isNullOrEmpty()) {
            addressfield.requestFocus()
            addressfield.error = "Required*"
        } else if (timearrfield.text.isNullOrEmpty()) {
            timearrfield.error = "Required*"
            timearrfield.requestFocus()
        } else if (datearrfield.text.isNullOrEmpty()) {
            datearrfield.error = "Required*"
            datearrfield.requestFocus()
        } else if (timeleavefield.text.isNullOrEmpty()) {
            timeleavefield.error = "Required*"
            timeleavefield.requestFocus()
        } else if (dateleavefield.text.isNullOrEmpty()) {
            dateleavefield.error = "Required*"
            dateleavefield.requestFocus()
        } else {
            if (id != -1) stoppint.serviceId = id
            stoppint.name =
                view.findViewById<EditText>(R.id.editStopPointName).text.toString()
            stoppint.address = view.findViewById<EditText>(R.id.editAddress).text.toString()
            val type = view.findViewById<MaterialSpinner>(R.id.spinnerType).text.toString()
            stoppint.type = type
            stoppint.lat = latlng.latitude
            stoppint.long = latlng.longitude
            val timeArrive =
                view.findViewById<EditText>(R.id.editTimeArrive).text.toString()
            val dateArrive =
                view.findViewById<EditText>(R.id.editDateArrive).text.toString()
            val timeLeave = view.findViewById<EditText>(R.id.editTimeLeave).text.toString()
            val dateLeave = view.findViewById<EditText>(R.id.editDateLeave).text.toString()
            var arriveTime: Long = 0
            var leaveTime: Long = 0
            if (timeArrive.isNotEmpty() && dateArrive.isNotEmpty()) {
                arriveTime = util.datetimeToLong(timeArrive + " " + dateArrive)
                leaveTime = util.datetimeToLong(timeLeave + " " + dateLeave)
            }
            stoppint.arrivalAt = arriveTime
            stoppint.leaveAt = leaveTime

            var minCostStr = view.findViewById<EditText>(R.id.editMinCost).text.toString()
            var maxCostStr = view.findViewById<EditText>(R.id.editMaxCost).text.toString()

            if (minCostStr.isNotEmpty()) {
                stoppint.minCost = minCostStr.toInt()
            }
            if (maxCostStr.isNotEmpty()) {
                stoppint.maxCost = maxCostStr.toInt()
            }
            val province =
                view.findViewById<MaterialSpinner>(R.id.spinnerProvince).text.toString()
            stoppint.provinceId = util.getProvinceID(province)

            val curMarker: Marker

            var oldIndexType = "none"
            if (oldIndex >= 0) {
                oldIndexType = mStopPointArrayList[oldIndex].type
            }

            if (type == "Start Point") {
                if (mStopPointArrayList.size > 0 && mStopPointArrayList[0].type == "Start Point") {
                    if (oldIndex <= -1) {
                        mStopPointArrayList.removeAt(0)
                    }
                    LastStartMarker.remove()
                }
                LastStartMarker =
                    addMarker(googleMap, latlng, stoppint.name, R.drawable.ic_startpoint)
                LastStartPointLatLng = latlng
                curMarker = LastStartMarker
            } else if (type == "End Point") {
                if (mStopPointArrayList.size > 0 && mStopPointArrayList[mStopPointArrayList.size - 1].type == "End Point") {
                    if (oldIndex <= -1) {
                        mStopPointArrayList.removeAt(mStopPointArrayList.size - 1)
                    }
                    LastEndMarker.remove()
                }
                LastEndMarker =
                    addMarker(googleMap, latlng, stoppint.name, R.drawable.ic_endpoint)
                LastEndPointLatLng = latlng
                curMarker = LastEndMarker
            } else if (type == "Restaurant") {
                curMarker =
                    addMarker(googleMap, latlng, stoppint.name, R.drawable.ic_restaurant)
                stoppint.serviceTypeId = 1
            } else if (type == "Hotel") {
                curMarker = addMarker(googleMap, latlng, stoppint.name, R.drawable.ic_hotel)
                stoppint.serviceTypeId = 2
            } else if (type == "Rest Station") {
                curMarker =
                    addMarker(googleMap, latlng, stoppint.name, R.drawable.ic_bedtime)
                stoppint.serviceTypeId = 3
            } else if (type == "Others") {
                curMarker = addMarker(googleMap, latlng, stoppint.name, R.drawable.ic_pin)
                stoppint.serviceTypeId = 4
            } else {
                curMarker = addMarker(googleMap, latlng, stoppint.name, R.drawable.ic_pin)
                stoppint.serviceTypeId = 5
            }


            curMarker.tag = stoppint.name + stoppint.address + stoppint.type
            if (oldIndex > -1) {

                if (type == "Start Point") {
                    mStopPointArrayList.removeAt(oldIndex)
                    if (mStopPointArrayList.size > 0 && mStopPointArrayList[0].type == "Start Point") {
                        mStopPointArrayList.removeAt(0)
                    }
                    mStopPointArrayList.add(0,stoppint)
                    mStopPointMarkerArrayList.add(0,curMarker)


                }
                else if (type == "End Point") {
                    if (mStopPointArrayList.size > 0 && mStopPointArrayList[mStopPointArrayList.size-1].type == "End Point") {
                        mStopPointArrayList.removeAt(mStopPointArrayList.size-1)
                    }
                    mStopPointArrayList.removeAt(oldIndex)
                    mStopPointArrayList.add(stoppint)
                    mStopPointMarkerArrayList.add(curMarker)
                }
                else {
                    mStopPointArrayList[oldIndex] = stoppint
                    mStopPointMarkerArrayList[oldIndex] = curMarker
                }

            }
            else {
                mStopPointArrayList.add(stoppint)
                mStopPointMarkerArrayList.add(curMarker)
            }

            drawThePath()

            // Dismiss the popup window
            popupWindow.dismiss()
        }
    }
}
