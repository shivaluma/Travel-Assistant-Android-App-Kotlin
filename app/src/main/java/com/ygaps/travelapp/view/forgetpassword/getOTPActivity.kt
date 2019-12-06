package com.ygaps.travelapp.view.forgetpassword

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.ygaps.travelapp.ErrorResponse
import com.ygaps.travelapp.R
import com.ygaps.travelapp.ResponseGetOTP
import com.ygaps.travelapp.ResponseTourInfo
import com.ygaps.travelapp.manager.doAsync
import com.ygaps.travelapp.network.model.ApiServiceGetOTP
import com.ygaps.travelapp.network.model.ApiServiceGetTourInfo
import com.ygaps.travelapp.network.model.WebAccess
import com.ygaps.travelapp.util.util
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.layout_forgetpassword.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class getOTPActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_otp)
        supportActionBar!!.hide()

        typeSpinner.setItems("Email", "Phone")

        btnSubmit.setOnClickListener {
            var type = typeSpinner.text.toString()
            var value = forgetEmailPhone.text.toString()


            ApiRequest(type,value)

        }
    }



    fun ApiRequest(type : String, value : String) {

            val service = WebAccess.retrofit.create(ApiServiceGetOTP::class.java)
            val body = JsonObject()
            body.addProperty("type", type)
            body.addProperty("value", value)

            val call = service.getOTP(body)
            call.enqueue(object : Callback<ResponseGetOTP> {
                override fun onFailure(call: Call<ResponseGetOTP>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseGetOTP>,
                    response: Response<ResponseGetOTP>
                ) {
                    if (response.code() != 200) {
                        val gson = Gson()
                        val type = object : TypeToken<ErrorResponse>() {}.type
                        var errorResponse: ErrorResponse? = gson.fromJson(response.errorBody()!!.charStream(), type)
                        Toast.makeText(applicationContext, errorResponse!!.message, Toast.LENGTH_LONG).show()
                    } else {
                        var type = response.body()!!.type
                        var expire = response.body()!!.expiredOn
                        var userId = response.body()!!.userId

                        var intent = Intent(applicationContext, CheckOTPActivity::class.java)
                        intent.putExtra("type", type)
                        intent.putExtra("expiredOn", expire)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                    }
                }
            })

    }
}
