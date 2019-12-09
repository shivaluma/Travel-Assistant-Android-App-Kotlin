package com.ygaps.travelapp.view.stoppoint

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.ygaps.travelapp.*
import com.ygaps.travelapp.manager.doAsync
import com.ygaps.travelapp.util.util
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.taufiqrahman.reviewratings.BarLabels

import com.taufiqrahman.reviewratings.RatingReviews
import kotlinx.android.synthetic.main.activity_stop_point_info.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.gms.maps.CameraUpdateFactory

import com.ygaps.travelapp.model.StopPoint
import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ygaps.travelapp.network.model.*


class StopPointInfo : AppCompatActivity(){

    lateinit var mGoogleMap : GoogleMap
    var token : String = ""
    var serviceId : Int = 100
    val colors = intArrayOf(
        Color.parseColor("#0e9d58"),
        Color.parseColor("#bfd047"),
        Color.parseColor("#ffc105"),
        Color.parseColor("#ef7e14"),
        Color.parseColor("#d36259")
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_point_info)
        supportActionBar!!.hide()

        locateStopPoint.onCreate(savedInstanceState)
        locateStopPoint.getMapAsync {
            mGoogleMap = it
            locateStopPoint.onResume()
        }


        token = this.intent.extras!!.getString("token","notoken")!!
        serviceId = this.intent.extras!!.getInt("stpid",100)
        serviceRatingStarSelect.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (rating != 0f) {
                serviceFeedbackEditContent.visibility = View.VISIBLE
            }
        }
        btnFeedbackCancel.setOnClickListener {
            serviceFeedbackEditContent.visibility = View.GONE
            serviceRatingStarSelect.rating = 0f
        }

        btnFeedbackSubmit.setOnClickListener {
            var content = editserviceRatingContent.text.toString()
            ApiRequestSendFeedBack(serviceId, content, serviceRatingStarSelect.rating.toInt())
        }

        ApiRequest()
        ApiRequestGetPoints()

    }




    fun ApiRequest() {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetStopPointInfo::class.java)

            val call = service.getTourInfo(token,serviceId)
            call.enqueue(object : Callback<ResponseStopPointInfo> {
                override fun onFailure(call: Call<ResponseStopPointInfo>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseStopPointInfo>,
                    response: Response<ResponseStopPointInfo>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(applicationContext, response.message(), Toast.LENGTH_LONG).show()
                    } else {

                        // update map
                        val stoppointLatLng : LatLng = LatLng(response.body()!!.lat!!,response.body()!!.long!!)
                        val update = CameraUpdateFactory.newLatLng(stoppointLatLng)
                        val zoom = CameraUpdateFactory.zoomTo(15f)
                        mGoogleMap.moveCamera(update)
                        mGoogleMap.animateCamera(zoom)

                        // Creating a marker
                        val markerOptions = MarkerOptions()

                        // Setting the position for the marker
                        markerOptions.position(stoppointLatLng)

                        // Setting the title for the marker.
                        // This will be displayed on taping the marker
                        markerOptions.title(response.body()!!.name)

                        mGoogleMap.addMarker(markerOptions)
                        var type = util.StopPointTypeToString(response.body()!!.serviceTypeId!!)
                        serviceTypeText.text = type
                        stpInfoName.text = response.body()!!.name
                        stpInfoAddress.text = response.body()!!.address
                        stpInfoContact.text = response.body()!!.contact
                        var cost = response.body()!!.minCost.toString() + " - " + response.body()!!.maxCost.toString()
                        stpInfoCost.text = cost
                    }
                }
            })
        }.execute()
    }


    fun ApiRequestGetPoints() {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetStopPointPoints::class.java)
            val call = service.getPoints(token,serviceId)
            call.enqueue(object : Callback<ResponseStopPointRatingPoints> {
                override fun onFailure(call: Call<ResponseStopPointRatingPoints>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseStopPointRatingPoints>,
                    response: Response<ResponseStopPointRatingPoints>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(applicationContext, response.message(), Toast.LENGTH_LONG).show()
                    } else {


                        val ratingReviews = findViewById<RatingReviews>(R.id.rating_reviews)

                        val raters = intArrayOf(
                            response.body()!!.pointStats[0].total,
                            response.body()!!.pointStats[1].total,
                            response.body()!!.pointStats[2].total,
                            response.body()!!.pointStats[3].total,
                            response.body()!!.pointStats[4].total
                        )
                        var average = "%.1f".format(raters.average())
                        ratingAveragePoint.text = average.toString()
                        var maxValue = raters.max()
                        var sum = raters.sum()
                        textView2.text = sum.toString()
                        ratingBar.rating = average.toFloat()

                        ratingReviews.createRatingBars(maxValue!!, BarLabels.STYPE1, colors, raters)
                    }
                }
            })
        }.execute()
    }

    fun ApiRequestSendFeedBack(serviceId : Int, feedback: String, point : Int) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceSendServiceFeedback::class.java)
            val body = JsonObject()
            body.addProperty("serviceId", serviceId)
            body.addProperty("feedback", feedback)
            body.addProperty("point", point)

            val call = service.sendFeedback(token,body)
            call.enqueue(object : Callback<ResponseFeedbackService> {
                override fun onFailure(call: Call<ResponseFeedbackService>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseFeedbackService>,
                    response: Response<ResponseFeedbackService>
                ) {
                    if (response.code() != 200) {
                        val gson = Gson()
                        val type = object : TypeToken<ErrorResponse>() {}.type
                        var errorResponse: ErrorResponse? = gson.fromJson(response.errorBody()!!.charStream(), type)
                        Toast.makeText(applicationContext, errorResponse!!.message, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, response.message(), Toast.LENGTH_LONG).show()
                        ApiRequestGetPoints()
                    }
                }
            })
        }.execute()
    }

}
