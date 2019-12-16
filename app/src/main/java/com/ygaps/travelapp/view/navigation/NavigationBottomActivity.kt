package com.ygaps.travelapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ygaps.travelapp.manager.doAsync
import com.ygaps.travelapp.network.model.ApiServiceGetUserInfo
import com.ygaps.travelapp.network.model.ApiServicePutFcmToken
import com.ygaps.travelapp.network.model.WebAccess
import com.ygaps.travelapp.service.FirebaseMessagingService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NavigationBottomActivity : AppCompatActivity() {

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_history,
                R.id.navigation_notifications,
                R.id.navigation_explorer,
                R.id.navigation_user
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        var token = intent.extras!!.getString("userToken")!!




        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val fcmtoken = task.result?.token!!
                // Log and toast
                ApiRequestPutFcmToken(token, fcmtoken)
            })

        ApiRequestGetUserId(token)
    }


    fun ApiRequestGetUserId(token : String) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetUserInfo::class.java)
            val call = service.getInfo(token)
            call.enqueue(object : Callback<ResponseUserInfo> {
                override fun onFailure(call: Call<ResponseUserInfo>, t: Throwable) {

                }
                override fun onResponse(
                    call: Call<ResponseUserInfo>,
                    response: Response<ResponseUserInfo>
                ) {
                    if (response.code() != 200) {

                    } else {
                        var item = response!!.body()
                        val sharePref : SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
                        var editor = sharePref.edit()
                        editor.putInt("userId", item!!.id)
                        editor.apply()
                    }
                }
            })
        }.execute()
    }


    fun ApiRequestPutFcmToken(logintoken : String, fcmToken : String) {
        doAsync {

            val service = WebAccess.retrofit.create(ApiServicePutFcmToken::class.java)
            val jsonObject = JsonObject()
            var uniqueId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)


            jsonObject.addProperty("fcmToken", fcmToken)
            jsonObject.addProperty("deviceId", uniqueId)
            jsonObject.addProperty("platform", 1)
            jsonObject.addProperty("appVersion", "1.0")
            val call = service.putToken(logintoken,jsonObject)
            call.enqueue(object : Callback<ResponsePutFcmToken> {
                override fun onFailure(call: Call<ResponsePutFcmToken>, t: Throwable) {
                    Toast.makeText(applicationContext,"Fcmtoken : " + t.message, Toast.LENGTH_LONG).show()
                }
                override fun onResponse(
                    call: Call<ResponsePutFcmToken>,
                    response: Response<ResponsePutFcmToken>
                ) {
                    if (response.code() != 200) {
                        val gson = Gson()
                        val type = object : TypeToken<ErrorResponse>() {}.type
                        var errorResponse: ErrorResponse? = gson.fromJson(response.errorBody()!!.charStream(), type)
                        Toast.makeText(applicationContext, "Fcmtoken : " + errorResponse!!.message, Toast.LENGTH_LONG).show()
                    } else {

                    }
                }
            })
        }.execute()
    }
}
