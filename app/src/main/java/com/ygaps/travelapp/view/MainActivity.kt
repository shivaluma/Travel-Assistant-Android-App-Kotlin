package com.ygaps.travelapp

import android.content.Context
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ygaps.travelapp.manager.doAsync
import com.ygaps.travelapp.network.model.ApiServicePutFcmToken
import com.ygaps.travelapp.network.model.WebAccess
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharePref : SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
        val apptoken = sharePref.getString("token", "notoken")
        if (apptoken != "notoken") {
            startActivity(Intent(applicationContext,NavigationBottomActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra("userToken", apptoken))
            finish()
            return
        }
        else {
            startActivity(Intent(applicationContext,LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            finish()
            return
        }
    }




}
