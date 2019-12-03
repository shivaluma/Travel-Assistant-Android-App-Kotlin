package com.brogrammers.travel.view.stoppoint

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.brogrammers.travel.R
import com.brogrammers.travel.ResponseStopPointInfo
import com.brogrammers.travel.ResponseTourInfo
import com.brogrammers.travel.manager.doAsync
import com.brogrammers.travel.network.model.ApiServiceGetStopPointInfo
import com.brogrammers.travel.network.model.ApiServiceGetTourInfo
import com.brogrammers.travel.network.model.WebAccess
import com.brogrammers.travel.util.util
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.taufiqrahman.reviewratings.BarLabels

import com.taufiqrahman.reviewratings.RatingReviews
import kotlinx.android.synthetic.main.activity_stop_point_info.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class StopPointInfo : AppCompatActivity(), OnMapReadyCallback {

    lateinit var mGoogleMap : GoogleMap
    var token : String = ""
    var serviceId : Int = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_point_info)
        supportActionBar!!.hide()

        token = this.intent.extras!!.getString("token","notoken")!!
        serviceId = this.intent.extras!!.getInt("stpid",100)

        locateStopPoint.onCreate(savedInstanceState)

        locateStopPoint.getMapAsync(this)

        val ratingReviews = findViewById(R.id.rating_reviews) as RatingReviews

        val colors = intArrayOf(
            Color.parseColor("#0e9d58"),
            Color.parseColor("#bfd047"),
            Color.parseColor("#ffc105"),
            Color.parseColor("#ef7e14"),
            Color.parseColor("#d36259")
        )

        val raters = intArrayOf(
            24,46,13,87,56
        )

        ratingReviews.createRatingBars(100, BarLabels.STYPE1, colors, raters)

        ApiRequest()

    }

    override fun onMapReady(p0: GoogleMap?) {
        mGoogleMap = p0!!
    }



    fun ApiRequest() {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetStopPointInfo::class.java)
            var cusId = serviceId - 18
            val call = service.getTourInfo(token,cusId)
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
                        Log.d("abab", response.body().toString())
                    }
                }
            })
        }.execute()
    }
}
