package com.brogrammers.travel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.layout_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class LoginActivity : AppCompatActivity() {

    val BASE_URL = "http://35.197.153.192:3000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        btnLogin.setOnClickListener {
            val emailPhone = editTextEmailPhone.text.toString()
            val password = editTextPassword.text.toString()
            if (emailPhone.isEmpty()) {
                editTextEmailPhone.error = "Yêu cầu nhập email/phone."
                editTextEmailPhone.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                editTextPassword.error = "Yêu cầu nhập mật khẩu."
                editTextPassword.requestFocus()
                return@setOnClickListener
            }


            val jsonObject = JsonObject()
            jsonObject.addProperty("emailPhone", emailPhone)
            jsonObject.addProperty("password", password)

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiServiceLogin::class.java)

            val call = service.postData(jsonObject)

            call.enqueue(object : Callback<PostResponseLogin> {
                override fun onFailure(call: Call<PostResponseLogin>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<PostResponseLogin>,
                    response: Response<PostResponseLogin>
                ) {
                    if (response.message() == "Not Found") {
                        Toast.makeText(applicationContext, "Đăng nhập thất bại!", Toast.LENGTH_LONG).show()
                    }
                    else {
                        Toast.makeText(applicationContext, "Đăng nhập thành công!", Toast.LENGTH_LONG).show()
                        updateTokenToStorage(response.body()!!.token)
                        startActivity(Intent(applicationContext,NavigationBottomActivity::class.java))
                        finish()
                    }
                }
            })
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 6969) {
            if (resultCode == Activity.RESULT_OK) {
                editTextEmailPhone.setText(data?.getStringExtra("usrname"))
                editTextPassword.setText(data?.getStringExtra("passwrd"))
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun viewRegisterClicked(view: View) {
        startActivityForResult(Intent(this,RegisterActivity::class.java),6969)
    }

    fun updateTokenToStorage(token: String) {
        val sharePref : SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
        val editor = sharePref.edit()
        Log.d("Token", token)
        editor.putString("token", token)
    }

    private interface ApiServiceLogin {
        @POST("/user/login")
        fun postData(
            @Body body: JsonObject
        ): Call<PostResponseLogin>
    }

    data class PostResponseLogin(val message:String, val userId:Int, val token:String)


}
