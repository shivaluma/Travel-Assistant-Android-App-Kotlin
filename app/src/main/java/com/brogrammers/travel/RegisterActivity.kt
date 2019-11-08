package com.brogrammers.travel

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_login.*
import kotlinx.android.synthetic.main.layout_register.*
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken


import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.converter.gson.GsonConverterFactory

import retrofit2.*





class RegisterActivity : AppCompatActivity() {

    private val BASE_URL = "http://35.197.153.192:3000"
    private var responseCode = 404
    private var phone = ""
    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_register)
        btnRegister.setOnClickListener {
            phone = textregisterPhone.text.toString()
            email = textregisterEmail.text.toString()
            password = textregisterPassword.text.toString()

            if (phone.isEmpty()) {
                textregisterPhone.error = "Yêu cầu nhập số điện thoại."
                textregisterPhone.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                textregisterEmail.error = "Yêu cầu nhập email."
                textregisterEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                textregisterPassword.error = "Yêu cầu nhập số điện thoại."
                textregisterPassword.requestFocus()
                return@setOnClickListener
            }

            val jsonObject = JsonObject()
            jsonObject.addProperty("phone", phone)
            jsonObject.addProperty("email", email)
            jsonObject.addProperty("password", password)

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiService::class.java)

            val call = service.postData(jsonObject)

            call.enqueue(object : Callback<PostResponse> {
                override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                    Toast.makeText(getApplicationContext(), t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<PostResponse>,
                    response: Response<PostResponse>
                ) {
                    responseCode = response.code()
                    if (responseCode == 200) {
                        Toast.makeText(getApplicationContext(), "Đăng ký thành công. Hãy đăng nhập.", Toast.LENGTH_LONG).show()
                        val data = Intent()
                        data.putExtra("usrname", phone)
                        data.putExtra("passwrd", password)
                        setResult(Activity.RESULT_OK,data)
                        finish()
                    }
                    else if (responseCode == 400) {
                        Toast.makeText(
                            getApplicationContext(),
                            "Thông tin không hợp lệ.",
                            Toast.LENGTH_LONG
                        ).show()
                        val gson = Gson()
                        val type = object : TypeToken<PostResponse>() {}.type
                        val errorResponse: PostResponse? = gson.fromJson(response.errorBody()!!.charStream(), type)
                        val errorCount = errorResponse!!.error
                        val errorList : ArrayList<message> = errorResponse.message

                        if (errorCount > 0) {
                            for (msg in errorList) {


                                if (msg.msg.toString() == "email") {
                                    textregisterEmail.error = msg.msg.toString()
                                }
                                else if (msg.param.toString() == "phone") {

                                    textregisterPhone.error = msg.msg.toString()
                                }
                                else if (msg.param.toString() == "password") {

                                    textregisterPassword.error = msg.msg.toString()
                                }
                            }
                        }
                    }
                }
            })
        }


    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    fun alreadyHaveAccount(view: View) {
        finish()
    }

    private interface ApiService {
        @POST("/user/register")
        fun postData(
            @Body body: JsonObject
        ): Call<PostResponse>
    }

    inner class message {
        var location:String ?= null
        var param:String ?= null
        var msg:String ?= null
        var value:String ?= null
    }

    data class PostResponse(val error:Int, val message:ArrayList<message>)
}
