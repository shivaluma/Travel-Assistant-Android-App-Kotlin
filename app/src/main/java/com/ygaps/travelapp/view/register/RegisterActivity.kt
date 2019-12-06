package com.ygaps.travelapp


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ygaps.travelapp.network.model.ApiServiceRegister
import com.ygaps.travelapp.network.model.WebAccess
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.layout_register.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegisterActivity : AppCompatActivity() {


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


            val service = WebAccess.retrofit.create(ApiServiceRegister::class.java)

            val call = service.postData(jsonObject)

            call.enqueue(object : Callback<ResponseRegister> {
                override fun onFailure(call: Call<ResponseRegister>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseRegister>,
                    response: Response<ResponseRegister>
                ) {
                    val responseCode = response.code()
                    if (responseCode == 200) {
                        Toast.makeText(
                            applicationContext,
                            "Đăng ký thành công. Hãy đăng nhập.",
                            Toast.LENGTH_LONG
                        ).show()
                        val data = Intent()
                        data.putExtra("usrname", phone)
                        data.putExtra("passwrd", password)
                        setResult(Activity.RESULT_OK, data)
                        finish()
                    } else if (responseCode == 400) {
                        Toast.makeText(
                            applicationContext,
                            "Thông tin không hợp lệ.",
                            Toast.LENGTH_LONG
                        ).show()
                        val gson = Gson()
                        val type = object : TypeToken<ResponseRegister>() {}.type
                        val errorResponse: ResponseRegister? =
                            gson.fromJson(response.errorBody()!!.charStream(), type)
                        val errorCount = errorResponse!!.error
                        val errorList: ArrayList<message> = errorResponse.message

                        if (errorCount > 0) {
                            for (msg in errorList) {
                                if (msg.param.toString() == "email") {
                                    textregisterEmail.error = msg.msg
                                } else if (msg.param.toString() == "phone") {

                                    textregisterPhone.error = msg.msg
                                } else if (msg.param.toString() == "password") {

                                    textregisterPassword.error = msg.msg
                                }
                            }
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Co loi xay ra tu API.",
                            Toast.LENGTH_LONG
                        ).show()
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
}
