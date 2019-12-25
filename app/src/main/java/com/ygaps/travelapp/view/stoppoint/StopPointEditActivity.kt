package com.ygaps.travelapp.view.stoppoint

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ygaps.travelapp.*
import com.ygaps.travelapp.manager.Constant
import com.ygaps.travelapp.manager.doAsync
import com.ygaps.travelapp.model.StopPoint
import com.ygaps.travelapp.network.model.ApiServiceAddStopPointToTour
import com.ygaps.travelapp.network.model.ApiServiceGetNotificationOnRoad
import com.ygaps.travelapp.network.model.ApiServiceUpdateStopPoint
import com.ygaps.travelapp.network.model.WebAccess
import com.ygaps.travelapp.util.util
import kotlinx.android.synthetic.main.activity_stop_point_edit.*
import kotlinx.android.synthetic.main.activity_stop_point_info.*
import kotlinx.android.synthetic.main.popup_map_choose_stoppoint_location.view.*
import kotlinx.android.synthetic.main.stoppoint.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class StopPointEditActivity : AppCompatActivity() {

    var mStopPointId = 0
    var mTourId = 0
    lateinit var mGoogleMap : GoogleMap
    var hasSetMarker = false


    lateinit var currentPointLatLng : LatLng
    lateinit var currentPointMarker : Marker
    var currentAddress : String = ""
    var mToken : String = ""

    lateinit var mapFragment : SupportMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_point_edit)
        supportActionBar?.hide()

        mTourId = intent.extras!!.getInt("tourId")
        mStopPointId = intent.extras!!.getInt("id")
        var mServiceid = intent.extras!!.getInt("serviceId")
        var mName = intent.extras!!.getString("name")
        var mLat = intent.extras!!.getDouble("lat")
        var mLong = intent.extras!!.getDouble("long")
        var mArriveAt = intent.extras!!.getLong("arriveAt")
        var mLeaveAt = intent.extras!!.getLong("leaveAt")
        var mMinCost = intent.extras!!.getLong("minCost")
        var mMaxCost = intent.extras!!.getLong("maxCost")
        var mProvinceId = intent.extras!!.getInt("provinceId")
        var mIndex = intent.extras!!.getInt("index")
        var mMaxIndex = intent.extras!!.getInt("maxindex")
        var mAddress = intent.extras!!.getString("address")
        var mServiceTypeId = intent.extras!!.getInt("serviceTypeId")
        mToken = intent.extras!!.getString("token")!!
        currentAddress = mAddress!!
        currentPointLatLng = LatLng(mLat,mLong)

        editChangeStopPointName.setText(mName)
        
        editChangeMinCostStopPoint.setText(mMinCost.toString())
        editChangeMaxCostStopPoint.setText(mMaxCost.toString())
        val dateTimeArrive = util.longToDateTime(mArriveAt).split(" ")
        val dateTimeLeave = util.longToDateTime(mLeaveAt).split(" ")


        editStopPointTimeArrive.setOnClickListener {
            util.setOnClickTime(editStopPointTimeArrive,this)
        }

        editStopPointTimeLeave.setOnClickListener {
            util.setOnClickTime(editStopPointTimeLeave, this)
        }

        editStopPointDateArrive.setOnClickListener {
            util.setOnClickDate(editStopPointDateArrive,this)
        }

        editStopPointDateLeave.setOnClickListener {
            util.setOnClickDate(editStopPointDateLeave, this)
        }

        saveStopPointInfo.setOnClickListener {
            val timeArrive = util.datetimeToLong(editStopPointTimeArrive.text.toString() +  " " + editStopPointDateArrive.text.toString())
            val timeLeave = util.datetimeToLong(editStopPointTimeLeave.text.toString() +  " " + editStopPointDateLeave.text.toString())
            val name = editChangeStopPointName.text.toString()
            val type = spinnerStopPointType.selectedIndex+1
            val min = editChangeMinCostStopPoint.text.toString().toLong()
            val max = editChangeMaxCostStopPoint.text.toString().toLong()
            //val index = spinnerStopPointIndex.selectedIndex
            val addr = editStopPointAddress.text.toString()
            val provinceId = spinnerStopPointProvince.selectedIndex+1

            Log.d("abab", timeArrive.toString() + " - " + timeLeave.toString())
            ApiRequestUpdateStopPoint(mTourId, name, currentPointLatLng, timeArrive, timeLeave,type,min,max,provinceId)
        }

        editStopPointTimeArrive.setText(dateTimeArrive[0])
        editStopPointDateArrive.setText(dateTimeArrive[1])
        editStopPointTimeLeave.setText(dateTimeLeave[0])
        editStopPointDateLeave.setText(dateTimeLeave[1])
        editStopPointAddress.setText(mAddress)
        val data = ArrayList<String>()
        for (i in 1..mMaxIndex) {
            data.add(i.toString())
        }
//        spinnerStopPointIndex.setItems(data)
//        spinnerStopPointIndex.selectedIndex = mIndex
        spinnerStopPointType.setItems("Restaurant", "Hotel", "Rest Station", "Others")
        spinnerStopPointType.selectedIndex = mServiceTypeId-1

        spinnerStopPointProvince.setItems(Constant.provinceList)
        spinnerStopPointProvince.selectedIndex = mProvinceId

        val inflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_map_choose_stoppoint_location, null)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map231) as SupportMapFragment
        mapFragment.getMapAsync {
            mGoogleMap = it
            mGoogleMap.setOnMapClickListener {
                currentPointMarker.remove()
                currentPointLatLng = it
                currentPointMarker = mGoogleMap.addMarker(MarkerOptions().title("Current").position(it))
                editStopPointAddress.setText(getAddressByLocation(it))
            }
        }







        mapStopPoint.setOnClickListener {

            if (!hasSetMarker) {
                val currentLatLng = LatLng(mLat,mLong)
                currentPointLatLng = currentLatLng
                currentPointMarker = mGoogleMap.addMarker(MarkerOptions().title("Current").position(currentLatLng))
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,15.0f))
                hasSetMarker = true
            }


            val popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
                LinearLayout.LayoutParams.WRAP_CONTENT, // Window height
                true
            )

            view.choose_cancel.setOnClickListener {
                editStopPointAddress.setText(currentAddress)
                currentPointLatLng = LatLng(mLat,mLong)
                popupWindow.dismiss()
            }

            view.choose_accept.setOnClickListener {
                popupWindow.dismiss()
            }


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


            // Set a dismiss listener for popup window
            popupWindow.setOnDismissListener {

            }

            popupWindow.showAsDropDown(editStopPointAddress)
        }



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

    fun ApiRequestUpdateStopPoint(tourId : Int, name : String, pos :LatLng, arri : Long, leav : Long, type : Int, min : Long, max : Long, provinceId : Int) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceAddStopPointToTour::class.java)

            val mainbody = JsonObject()
            val stopPointArray = JsonArray()

            val body = JsonObject()
            body.addProperty("id", mStopPointId)
            body.addProperty("name",name)
            body.addProperty("lat", pos.latitude)
            body.addProperty("long", pos.longitude)
            body.addProperty("provinceId", provinceId)
            body.addProperty("arrivalAt", arri)
            body.addProperty("leaveAt",leav)
            body.addProperty("serviceTypeId",type)
            body.addProperty("minCost",min)
            body.addProperty("maxCost",max)

            stopPointArray.add(body)

            mainbody.addProperty("tourId", tourId)
            mainbody.add("stopPoints", stopPointArray)
            //body.addProperty("address",addr)


            val call = service.postData(mToken, mainbody)
            call.enqueue(object : Callback<ResponseAddStopPoint> {
                override fun onFailure(call: Call<ResponseAddStopPoint>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseAddStopPoint>,
                    response: Response<ResponseAddStopPoint>
                ) {
                    if (response.code() != 200) {
                        val gson = Gson()
                        val type = object : TypeToken<ErrorResponse>() {}.type
                        var errorResponse: ErrorResponse? = gson.fromJson(response.errorBody()!!.charStream(), type)
                        Toast.makeText(applicationContext, errorResponse!!.message.toString(), Toast.LENGTH_LONG).show()
                        Log.d("abab",errorResponse!!.message.toString())
                    } else {
                        Toast.makeText(applicationContext, "Update successfully!", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            })
        }.execute()
    }
}
