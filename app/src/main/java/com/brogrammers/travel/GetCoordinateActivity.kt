package com.brogrammers.travel

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.icu.text.SimpleDateFormat
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.jaredrummler.materialspinner.MaterialSpinner
import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.android.synthetic.main.activity_get_coordinate.*
import kotlinx.android.synthetic.main.stoppoint.*
import kotlinx.android.synthetic.main.stoppoint.view.*
import kotlinx.android.synthetic.main.stoppointinfo.view.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
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

    internal var mGoogleApiClient: GoogleApiClient? = null
    internal var mCurrLocationMarker: Marker? = null

    lateinit var token: AutocompleteSessionToken

    var hasStartPoint = false
    var hasEndPoint = false
    var StartPointPos = -1
    var EndPointPos = -1
    lateinit var LastStartPointLatLng: LatLng
    lateinit var LastEndPointLatLng: LatLng
    lateinit var LastStartMarker: Marker
    lateinit var LastEndMarker: Marker
    val BASE_URL = "http://35.197.153.192:3000"
    var provinceArrayList = ArrayList<String>()
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_coordinate)

        supportActionBar!!.hide()



        provinceArrayList.addAll(
            arrayOf(
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
        )


        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        Places.initialize(this, "AIzaSyBoa_7qIh5KKyAt4oqVFcs7dTTH-Vc534E")
        mPlacesClient = Places.createClient(this)
        token = AutocompleteSessionToken.newInstance()

        btnFloatFinish.setOnClickListener {
            if (!hasStartPoint || !hasEndPoint) {
                Toast.makeText(
                    applicationContext,
                    "No Start/Stop Point is defined",
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
                jsonObject.addProperty("minCost", extras.getInt("iMinCost"))
                jsonObject.addProperty("maxCost", extras.getInt("iMaxCost"))
                if (!extras.getString("iImage").isNullOrEmpty()) {
                    jsonObject.addProperty("avatar", extras.getString("iImage"))
                }


                val service = retrofit.create(ApiServiceAddTour::class.java)

                val call = service.postData(logintoken, jsonObject)

                call.enqueue(object : Callback<PostResponseCreateTour> {
                    override fun onFailure(call: Call<PostResponseCreateTour>, t: Throwable) {
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(
                        call: Call<PostResponseCreateTour>,
                        response: Response<PostResponseCreateTour>
                    ) {
                        if (response.code() == 200) {
                            Toast.makeText(
                                applicationContext,
                                "Create Tour Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            deleteStartEndPoint(mStopPointArrayList)
                            val stpJsonObj = JsonObject()
                            stpJsonObj.addProperty("tourId", response.body()!!.id.toString())
                            val arrlistJson = Gson().toJson(mStopPointArrayList)
                            val parser = JsonParser()
                            val jsonStopPint = parser.parse(arrlistJson)
                            stpJsonObj.add("stopPoints", jsonStopPint)
                            val servicestp = retrofit.create(ApiServiceAddTourStopPoint::class.java)
                            val callstp = servicestp.postData(logintoken, stpJsonObj)
                            callstp.enqueue(object : Callback<tourList> {
                                override fun onFailure(call: Call<tourList>, t: Throwable) {
                                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG)
                                        .show()
                                }

                                override fun onResponse(
                                    call: Call<tourList>,
                                    response: Response<tourList>
                                ) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Add Stop Point Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.d("resres", response.message())
                                    Log.d("resres", stpJsonObj.toString())
                                }
                            })
                        } else {
                            Log.d("resres", response.message())
                            Log.d("resres", response.code().toString())
                            Log.d("resres", response.errorBody().toString())

                            try {
                                var temp = JSONObject(response.errorBody()!!.string())
                                Log.d("resres", temp.getString("message"))
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            Toast.makeText(
                                applicationContext,
                                "Create Tour Error",
                                Toast.LENGTH_LONG
                            ).show()
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
        }


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
                }
            }
        )



        searchMapBar.addTextChangeListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val predictionsRequest: FindAutocompletePredictionsRequest =
                    FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build()
                mPlacesClient.findAutocompletePredictions(predictionsRequest)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful()) {
                            var predictionsResponse: FindAutocompletePredictionsResponse =
                                task.getResult()!!
                            if (predictionsResponse != null) {
                                mListAutoCompletePredition =
                                    predictionsResponse.getAutocompletePredictions()
                                var suggestionsList = ArrayList<String>()
                                for (i in 0 until mListAutoCompletePredition.size - 1) {
                                    var prediction: AutocompletePrediction =
                                        mListAutoCompletePredition.get(i)
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                searchMapBar.updateLastSuggestions(suggestionsList)
                                if (!searchMapBar.isSuggestionsVisible()) {
                                    searchMapBar.showSuggestionsList()
                                }
                            }
                        } else {
                            Log.i("mytag", "prediction fetching task unsuccessful");
                        }
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
                googleMap!!.isMyLocationEnabled = true
            }
        } else {
            buildGoogleApiClient()
            googleMap!!.isMyLocationEnabled = true
        }

        googleMap.setOnMapClickListener {
            val latlng = it
            Toast.makeText(applicationContext, it.toString(), Toast.LENGTH_LONG).show()
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
                setOnClickTime(timeArrive)
            }

            var timeLeave = view.findViewById<EditText>(R.id.editTimeLeave)
            timeLeave.setOnClickListener {
                setOnClickTime(timeLeave)
            }

            var dateArrive = view.findViewById<EditText>(R.id.editDateArrive)
            var dateLeave = view.findViewById<EditText>(R.id.editDateLeave)


            dateArrive.setOnClickListener {
                setOnClickDate(dateArrive)
            }

            dateLeave.setOnClickListener {
                setOnClickDate(dateLeave)
            }


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


            // Set a click listener for popup's button widget
            buttonPopup.setOnClickListener {
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
                }
                else if (addressfield.text.isNullOrEmpty()) {
                    addressfield.requestFocus()
                    addressfield.error = "Required*"
                }
                else if (timearrfield.text.isNullOrEmpty()) {
                    timearrfield.error = "Required*"
                    timearrfield.requestFocus()
                }
                else if (datearrfield.text.isNullOrEmpty()) {
                    datearrfield.error = "Required*"
                    datearrfield.requestFocus()
                }
                else if (timeleavefield.text.isNullOrEmpty()) {
                    timeleavefield.error = "Required*"
                    timeleavefield.requestFocus()
                }
                else if (dateleavefield.text.isNullOrEmpty()) {
                    dateleavefield.error = "Required*"
                    dateleavefield.requestFocus()
                }
                else {
                    stoppint.name = view.findViewById<EditText>(R.id.editStopPointName).text.toString()
                    stoppint.address = view.findViewById<EditText>(R.id.editAddress).text.toString()
                    val type = view.findViewById<MaterialSpinner>(R.id.spinnerType).text.toString()
                    stoppint.type = type
                    stoppint.lat = latlng.latitude
                    stoppint.long = latlng.longitude
                    val timeArrive = view.findViewById<EditText>(R.id.editTimeArrive).text.toString()
                    val dateArrive = view.findViewById<EditText>(R.id.editDateArrive).text.toString()
                    val timeLeave = view.findViewById<EditText>(R.id.editTimeLeave).text.toString()
                    val dateLeave = view.findViewById<EditText>(R.id.editDateLeave).text.toString()
                    var arriveTime: Long = 0
                    var leaveTime: Long = 0
                    if (timeArrive.isNotEmpty() && dateArrive.isNotEmpty()) {
                        arriveTime = LocalDateTime.parse(
                            timeArrive + " " + dateArrive,
                            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
                        ).toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()
                        leaveTime = LocalDateTime.parse(
                            timeLeave + " " + dateLeave,
                            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
                        ).toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()
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
                    stoppint.provinceID = getProvinceID(province)
                    if (type == "Start Point") {
                        if (hasStartPoint) {
                            mStopPointArrayList.removeAt(StartPointPos)
                            LastStartMarker.remove()
                        }

                        LastStartMarker = googleMap.addMarker(
                            MarkerOptions()
                                .position(latlng)
                                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_startpoint))
                                .title(stoppint.name)
                        )
                        StartPointPos = mStopPointArrayList.size
                        LastStartPointLatLng = latlng
                        hasStartPoint = true
                    } else if (type == "End Point") {
                        if (hasEndPoint) {
                            mStopPointArrayList.removeAt(EndPointPos)
                            LastEndMarker.remove()
                        }
                        LastEndMarker = googleMap.addMarker(
                            MarkerOptions()
                                .position(latlng)
                                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_endpoint))
                                .title(stoppint.name)
                        )
                        EndPointPos = mStopPointArrayList.size
                        LastEndPointLatLng = latlng
                        hasEndPoint = true
                    } else if (type == "Restaurant") {
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(latlng)
                                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_food))
                                .title(stoppint.name)
                        )
                        stoppint.serviceTypeId = 1
                    } else if (type == "Hotel") {
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(latlng)
                                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_hotel))
                                .title(stoppint.name)
                        )
                        stoppint.serviceTypeId = 2
                    } else if (type == "Rest Station") {
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(latlng)
                                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_bedtime))
                                .title(stoppint.name)
                        )
                        stoppint.serviceTypeId = 3
                    } else if (type == "Others") {
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(latlng)
                                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_pin))
                                .title(stoppint.name)
                        )
                        stoppint.serviceTypeId = 4
                    } else {
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(latlng)
                                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_pin))
                                .title(stoppint.name)
                        )
                        stoppint.serviceTypeId = 4
                    }
                    mStopPointArrayList.add(stoppint)


                    // Dismiss the popup window
                    popupWindow.dismiss()
                }





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
        mLastKnowLocation = location!!
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove()
        }
        //Place current location marker
        val latLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Current Position")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        mCurrLocationMarker = googleMap.addMarker(markerOptions)

        //move map camera
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15f))

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.getFusedLocationProviderClient(this)
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
            if (addressList!!.isEmpty()) {
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



}
