package com.ygaps.travelapp.view.createtour

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.ygaps.travelapp.*
import com.ygaps.travelapp.manager.doAsync
import com.ygaps.travelapp.network.model.ApiServiceGetTourInfo
import com.ygaps.travelapp.network.model.ApiServiceUpdateTour
import com.ygaps.travelapp.network.model.WebAccess
import com.ygaps.travelapp.util.util
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_change_tour_info.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.app.Activity
import android.content.Intent




class ChangeTourInfo : AppCompatActivity() {

    var token : String = ""
    var tourId : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_change_tour_info)
        token = intent.extras!!.getString("token", "notoken")
        tourId = intent.extras!!.getInt("userId", 128)
        spinnerTourStatus.setItems("Cancel", "Open", "Started" , "Closed")
        ApiRequest()

        editChangeDateStart.setOnClickListener {
            util.setOnClickDate(editChangeDateStart,this@ChangeTourInfo)
        }

        editChangeDateEnd.setOnClickListener {
            util.setOnClickDate(editChangeDateEnd,this@ChangeTourInfo)
        }


        saveInfo.setOnClickListener {
            ApiRequestSaveUpdate()
        }

    }



    fun ApiRequest() {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetTourInfo::class.java)
            val call = service.getTourInfo(token,tourId)
            call.enqueue(object : Callback<ResponseTourInfo> {
                override fun onFailure(call: Call<ResponseTourInfo>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseTourInfo>,
                    response: Response<ResponseTourInfo>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(applicationContext, response.errorBody().toString(), Toast.LENGTH_LONG).show()
                    } else {
                        val data = response.body()!!
                        editChangeTourName.setText(data.name)
                        editChangeDateStart.setText(util.longToDate(data.startDate))
                        editChangeDateEnd.setText(util.longToDate(data.endDate))
                        editChangeChildNum.setText(data.childs.toString())
                        editChangeAdultNum.setText(data.adults.toString())
                        editChangeMinCostTour.setText(data.minCost.toString())
                        editChangeMaxCostTour.setText(data.maxCost.toString())
                        spinnerTourStatus.selectedIndex = data.status + 1
                        isPrivateCheckbox.isChecked = data.isPrivate
                    }
                }
            })
        }.execute()
    }

    fun ApiRequestSaveUpdate() {
            val service = WebAccess.retrofit.create(ApiServiceUpdateTour::class.java)
            val body = JsonObject()
            body.addProperty("id", tourId)
            body.addProperty("status", spinnerTourStatus.selectedIndex-1)
            body.addProperty("name", editChangeTourName.text.toString())
            body.addProperty("minCost", editChangeMinCostTour.text.toString().toLong())
            body.addProperty("maxCost", editChangeMaxCostTour.text.toString().toLong())
            body.addProperty("startDate", util.dateToLong(editChangeDateStart.text.toString()))
            body.addProperty("endDate", util.dateToLong(editChangeDateEnd.text.toString()))
            body.addProperty("adults", editChangeAdultNum.text.toString().toInt())
            body.addProperty("childs", editChangeChildNum.text.toString().toInt())
            body.addProperty("isPrivate", isPrivateCheckbox.isChecked)

            val call = service.update(token,body)
            call.enqueue(object : Callback<ResponseUpdateTourInfo> {
                override fun onFailure(call: Call<ResponseUpdateTourInfo>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseUpdateTourInfo>,
                    response: Response<ResponseUpdateTourInfo>
                ) {
                    if (response.code() != 200) {
                        val gson = Gson()
                        val type = object : TypeToken<ErrorResponse>() {}.type
                        var errorResponse: ErrorResponse? = gson.fromJson(response.errorBody()!!.charStream(), type)
                        Toast.makeText(applicationContext, errorResponse!!.message, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, "Success", Toast.LENGTH_LONG).show()
                        val returnIntent = Intent()
                        returnIntent.putExtra("id", tourId)
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    }
                }
            })
    }
}
