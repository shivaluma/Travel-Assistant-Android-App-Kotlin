package com.ygaps.travelapp.view.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.ygaps.travelapp.ErrorResponse
import com.ygaps.travelapp.R
import com.ygaps.travelapp.ResponseChangePassword
import com.ygaps.travelapp.network.model.ApiServiceChangePassword
import com.ygaps.travelapp.network.model.WebAccess
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_update_password.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdatePassword : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_password)
        supportActionBar!!.hide()
        var userId = this.intent.extras!!.getInt("userId", 100)
        var token = this.intent.extras!!.getString("token", "notoken")

        btnUpdatePassWord.setOnClickListener {
            var curpw = editCurrentPassword.text.toString()
            var newpw = editNewPassword.text.toString()
            ApiRequest(token, userId, curpw, newpw)
        }
    }


    fun ApiRequest(token: String, userId : Int, newpw : String, oldpw: String) {

        val service = WebAccess.retrofit.create(ApiServiceChangePassword::class.java)
        val body = JsonObject()
        body.addProperty("userId", userId)
        body.addProperty("currentPassword", oldpw)
        body.addProperty("newPassword", newpw)

        val call = service.changepw(token, body)
        call.enqueue(object : Callback<ResponseChangePassword> {
            override fun onFailure(call: Call<ResponseChangePassword>, t: Throwable) {
                Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<ResponseChangePassword>,
                response: Response<ResponseChangePassword>
            ) {
                if (response.code() != 200) {
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type
                    var errorResponse: ErrorResponse? = gson.fromJson(response.errorBody()!!.charStream(), type)
                    Toast.makeText(applicationContext, errorResponse!!.message, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(applicationContext, "Change password successfully!", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        })

    }
}
