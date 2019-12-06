package com.ygaps.travelapp.view.forgetpassword

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import com.ygaps.travelapp.*
import com.ygaps.travelapp.network.model.ApiServiceCheckOTP
import com.ygaps.travelapp.network.model.ApiServiceGetOTP
import com.ygaps.travelapp.network.model.WebAccess
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.layout_checkotp.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckOTPActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_otp)

        supportActionBar!!.hide()
        var expiredOn = intent.extras!!.getLong("expiredOn")
        var userId = intent.extras!!.getInt("userId")

        var currentTime = System.currentTimeMillis()

        var countDownTimer = expiredOn - currentTime

        var timer = object: CountDownTimer(countDownTimer, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                remainingTime.text = "Time Out!!"
            }
        }
        timer.start()


        btnSubmit.setOnClickListener {
            var password = inputNewPassword.text.toString()
            var otp = inputOTP.text.toString()
            ApiRequest(userId,password,otp)
        }

    }


    fun ApiRequest(userId : Int , newPassword : String, otp : String) {

        val service = WebAccess.retrofit.create(ApiServiceCheckOTP::class.java)
        val body = JsonObject()
        body.addProperty("userId", userId)
        body.addProperty("newPassword", newPassword)
        body.addProperty("verifyCode", otp)

        val call = service.checkOTP(body)
        call.enqueue(object : Callback<ResponseCheckOTP> {
            override fun onFailure(call: Call<ResponseCheckOTP>, t: Throwable) {
                Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<ResponseCheckOTP>,
                response: Response<ResponseCheckOTP>
            ) {
                if (response.code() != 200) {
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type
                    var errorResponse: ErrorResponse? = gson.fromJson(response.errorBody()!!.charStream(), type)
                    Toast.makeText(applicationContext, errorResponse!!.message, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(applicationContext, "Reset password successful!!", Toast.LENGTH_LONG).show()
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        })

    }
}
